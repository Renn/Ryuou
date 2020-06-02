//
// Created by Shen on 2020/5/19.
//

#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include "player.h"
#include <pthread.h>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
#include <libswresample/swresample.h>
}



struct PlayerInfo {
  const char *path = NULL;
  AVFormatContext *format_context = NULL;
  double total_length;
  /**
   * For video.
   */
  AVCodecContext *video_codec_context = NULL;
  AVCodec *video_codec = NULL;
  int video_stream_index = -1;
  int video_width;
  int video_height;
  /**
   * For audio.
   */
  AVCodecContext *audio_codec_context = NULL;
  AVCodec *audio_codec = NULL;
  int audio_stream_index = -1;
  SwrContext *swr_context = NULL;
  int out_sample_rate;
  uint64_t out_channel_config;
  AVSampleFormat out_format;
};

struct Native {
  ANativeWindow_Buffer window_buffer;
  ANativeWindow *native_window = NULL;
  int buffer_size;
  uint8_t *video_out_buffer = NULL;
};
struct Temp {
  AVPacket *packet = NULL;
  AVFrame *frame = NULL;
  AVFrame *rgba_frame = NULL;
  uint8_t *audio_out_buffer = NULL;
};
struct Reflection {
  jmethodID create_audio_track_method_id;
  jmethodID add_to_audio_track_method_id;
  jmethodID release_audio_track_method_id;
  jmethodID on_progress_method_id;
};
struct Arguments {
  jobject instance;
  jobject callback;
  Arguments(jobject instance, jobject callback) {
    this->instance = instance;
    this->callback = callback;
  }
};

PlayerInfo player_info;
Native native;
Temp temp;
Reflection reflection;
JavaVM *jvm = NULL;

/**
 * Set by main thread. Read-only in play_thread().
 */
bool paused = false;
bool stopped = false;
bool need_seek = false;
double seek_dest;

//struct SwsContext *data_convert_context;

// 以下代码按照 C 语言编译执行
extern "C"
JNIEXPORT void JNICALL
Java_org_ecnu_ryuou_player_Player_initByNative(JNIEnv *env,
                                               jobject instance,
                                               jstring path_,
                                               jobject surface) {
  // 临时存放函数调用结果（状态）
  int result;

  // Java String -> C String
  player_info.path = env->GetStringUTFChars(path_, 0);
  // 注册
  //av_register_all();

  // 初始化 AVformat_context
  player_info.format_context = avformat_alloc_context();

  // 打开视频文件
  result = avformat_open_input(&player_info.format_context, player_info.path, NULL, NULL);
  if (result < 0) {
    LOGE("Player Error : Can not open video file");
    release_resources();
    return;
  }

  // 查找流信息
  result = avformat_find_stream_info(player_info.format_context, NULL);
  if (result < 0) {
    LOGE("Player Error : Can not find stream info");
    release_resources();
    return;
  }

  // 查找编码器
  for (int i = 0; i < player_info.format_context->nb_streams; i++) {
    if (player_info.format_context->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
      player_info.video_stream_index = i;
    } else if (player_info.format_context->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
      player_info.audio_stream_index = i;
    }
  }
  if (player_info.video_stream_index < 0) {
    LOGE("Player Error : Can not find video stream");
    release_resources();
    return;
  }

  // 初始化解码器上下文 AVCodecContext
  player_info.video_codec_context = avcodec_alloc_context3(NULL);
  if (player_info.video_codec_context == NULL) {
    LOGE("Player Error : Can not allocate codec context");
    release_resources();
    return;
  }
  avcodec_parameters_to_context(player_info.video_codec_context,
                                player_info.format_context->streams[player_info.video_stream_index]->codecpar);

  player_info.audio_codec_context = avcodec_alloc_context3(NULL);
  avcodec_parameters_to_context(player_info.audio_codec_context,
                                player_info.format_context->streams[player_info.audio_stream_index]->codecpar);

  // 初始化解码器 AVCodec
  player_info.video_codec = avcodec_find_decoder(player_info.video_codec_context->codec_id);
  if (player_info.video_codec == NULL) {
    LOGE("Player Error : Can not find video codec");
    release_resources();
    return;
  }
  player_info.audio_codec = avcodec_find_decoder(player_info.audio_codec_context->codec_id);

  // 打开解码器
  result = avcodec_open2(player_info.video_codec_context, player_info.video_codec, NULL);
  if (result < 0) {
    LOGE("Player Error : Can not open stream");
    release_resources();
    return;
  }
  avcodec_open2(player_info.audio_codec_context, player_info.audio_codec, NULL);
  player_info.total_length =
      player_info.format_context->streams[player_info.audio_stream_index]->duration
          * av_q2d(player_info.format_context->streams[player_info.audio_stream_index]->time_base);

  //Video related initialization
  player_info.video_width = player_info.video_codec_context->width;
  player_info.video_height = player_info.video_codec_context->height;

  // 初始化 NativeWindow
  native.native_window = ANativeWindow_fromSurface(env, surface);
  if (native.native_window == NULL) {
    LOGE("Player Error : Can not create native window");
    release_resources();
    return;
  }

  // 设置缓冲区尺寸为视频尺寸（而非物理尺寸）
#ifndef USE_YUV
  result = ANativeWindow_setBuffersGeometry(native.native_window,
                                            player_info.video_width,
                                            player_info.video_height,
                                            WINDOW_FORMAT_RGBA_8888);
#else
  result = ANativeWindow_setBuffersGeometry(native_window,
                                            video_width,
                                            video_height,
                                            AHARDWAREBUFFER_FORMAT_Y8Cb8Cr8_420);
#endif
  if (result < 0) {
    LOGE("Player Error : Can not create native window buffer");
    release_resources();
    return;
  }

  // 解码前数据容器
  temp.packet = av_packet_alloc();
  if (temp.packet == NULL) {
    LOGE("Player Error : Can not allocate packet");
    release_resources();
    return;
  }

  // 解码后数据容器（像素数据）
  temp.frame = av_frame_alloc();
  if (temp.frame == NULL) {
    LOGE("Player Error : Can not allocate frame");
    release_resources();
    return;
  }

  // 转码后数据容器（直接播放）
#ifndef USE_YUV
  temp.rgba_frame = av_frame_alloc();
  if (temp.rgba_frame == NULL) {
    LOGE("Player Error : Can not allocate RGBA frame");
    release_resources();
    return;
  }
#endif

  // 申请输出 buffer
#ifndef USE_YUV
  native.buffer_size = av_image_get_buffer_size(AV_PIX_FMT_RGBA,
                                                player_info.video_width,
                                                player_info.video_height,
                                                1);
#else
  buffer_size = av_image_get_buffer_size(AV_PIX_FMT_YUV420P, video_width, video_height, 1);
#endif
  LOGE("buffer_size=%d", native.buffer_size);
  LOGE("video_width=%d", player_info.video_width);
  LOGE("video_height=%d", player_info.video_height);
  native.video_out_buffer = (uint8_t *) av_malloc(native.buffer_size * sizeof(uint8_t));
#ifndef USE_YUV
  av_image_fill_arrays(temp.rgba_frame->data,
                       temp.rgba_frame->linesize,
                       native.video_out_buffer,
                       AV_PIX_FMT_RGBA,
                       player_info.video_width,
                       player_info.video_height,
                       1);
#else
  av_image_fill_arrays(frame->data,frame->linesize,video_out_buffer,AV_PIX_FMT_YUV420P,video_width,video_height,1);
#endif
//  LOGE("pix_fmt=%d",video_codec_context->pix_fmt);
  //Audio related initialization
  //Output attributes
  player_info.out_sample_rate = player_info.audio_codec_context->sample_rate;
  player_info.out_channel_config = AV_CH_LAYOUT_STEREO;
  player_info.out_format = AV_SAMPLE_FMT_S16;
  //Buffer
  temp.audio_out_buffer = (uint8_t *) av_malloc(player_info.out_sample_rate * 2);
  //Resampling context
  player_info.swr_context = swr_alloc_set_opts(NULL,
                                               player_info.out_channel_config,
                                               player_info.out_format,
                                               player_info.out_sample_rate,
                                               player_info.audio_codec_context->channel_layout,
                                               player_info.audio_codec_context->sample_fmt,
                                               player_info.audio_codec_context->sample_rate,
                                               0,
                                               NULL);
  swr_init(player_info.swr_context);
  //Setup reflection
  jclass player_class = env->GetObjectClass(instance);
  reflection.create_audio_track_method_id =
      env->GetMethodID(player_class, "createAudioTrack", "(II)V");
  reflection.add_to_audio_track_method_id =
      env->GetMethodID(player_class, "addToAudioTrack", "([BI)V");
  reflection.release_audio_track_method_id =
      env->GetMethodID(player_class, "releaseAudioTrack", "()V");
  //Status initialization
  paused = false;
  stopped = false;
  need_seek = false;
}

extern "C"
JNIEXPORT void JNICALL
Java_org_ecnu_ryuou_player_Player_playByNative(JNIEnv *env, jobject instance, jobject callback) {
  if (paused) {
    paused = false;
  } else {
    jclass callback_class = env->GetObjectClass(callback);
    reflection.on_progress_method_id = env->GetMethodID(callback_class, "onProgress", "(DD)V");
    pthread_t t_play;
    instance = env->NewGlobalRef(instance);
    callback = env->NewGlobalRef(callback);
    Arguments *args = new Arguments(instance, callback);
    env->GetJavaVM(&jvm);
    pthread_create(&t_play, NULL, play_thread, args);
  }
}

void release_resources() {
  if (native.video_out_buffer != NULL) {
    av_free(native.video_out_buffer);
    native.video_out_buffer = NULL;
  }
  if (temp.frame != NULL) {
    av_frame_free(&temp.frame);
  }
  if (temp.rgba_frame != NULL) {
    av_frame_free(&temp.rgba_frame);
  }
  if (temp.packet != NULL) {
    av_packet_free(&temp.packet);
  }
  if (native.native_window != NULL) {
    ANativeWindow_release(native.native_window);
    native.native_window = NULL;
  }
  if (player_info.video_codec_context != NULL) {
    avcodec_free_context(&player_info.video_codec_context);
  }
  if (player_info.audio_codec_context != NULL) {
    avcodec_free_context(&player_info.audio_codec_context);
  }
  if (player_info.format_context != NULL) {
    avformat_close_input(&player_info.format_context);
  }
  if (player_info.swr_context != NULL) {
    swr_free(&player_info.swr_context);
  }
  if (temp.audio_out_buffer != NULL) {
    av_free(temp.audio_out_buffer);
    temp.audio_out_buffer = NULL;
  }
}

void *play_thread(void *args) {
  //Get thread-specific jnienv and instance
  JNIEnv *env = NULL;
  if (jvm != NULL) {
    int status = jvm->AttachCurrentThread(&env, NULL);
    if (status < 0) {
      LOGE("JVM error during attaching thread");
      return NULL;
    }
  }

  jobject instance = ((Arguments *) args)->instance;
  jobject callback = ((Arguments *) args)->callback;
  // 临时存放函数调用结果（状态）
  int result;

  // 数据格式转换上下文
#ifndef USE_YUV
  struct SwsContext
      *data_convert_context = data_convert_context = sws_getContext(player_info.video_width,
                                                                    player_info.video_height,
                                                                    player_info.video_codec_context->pix_fmt,
                                                                    player_info.video_width,
                                                                    player_info.video_height,
                                                                    AV_PIX_FMT_RGBA,
                                                                    SWS_BICUBIC,
                                                                    NULL,
                                                                    NULL,
                                                                    NULL);
#endif
  int num_channels = av_get_channel_layout_nb_channels(player_info.out_channel_config);
  env->CallVoidMethod(instance,
                      reflection.create_audio_track_method_id,
                      player_info.out_sample_rate,
                      num_channels);

  int skip_video = 0;
  int skip_audio = 0;
  long long video_count = 0;
  long long audio_count = 0;

  // 播放
  while (av_read_frame(player_info.format_context, temp.packet) >= 0 && !stopped) {
    if (temp.packet->stream_index == player_info.video_stream_index) {
      // 匹配视频流
      // 解码
      result = avcodec_send_packet(player_info.video_codec_context, temp.packet);
      if (result < 0 && result != AVERROR(EAGAIN) && result != AVERROR_EOF) {
        LOGE("Player Error : Can not send packet (codec step 1)");
        return NULL;
      }

      result = avcodec_receive_frame(player_info.video_codec_context, temp.frame);
      if (result < 0 && result != AVERROR_EOF) {
        LOGE("Player Error : Can not receive frame (codec step 2)");
        return NULL;
      }
//      LOGE("frame.format=%d",frame->format);

//      if(skip_video>=1){
//        skip_video=0;
//        continue;
//      }
//      skip_video++;
//      video_count++;
//      if(video_count>80000){
//        LOGE("ERROR!!!!!!!!!!!!!!!!VIDEO");
//      }

      // 数据格式转换
#ifndef USE_YUV
      result = sws_scale(data_convert_context,
                         (const uint8_t *const *) temp.frame->data,
                         temp.frame->linesize,
                         0,
                         player_info.video_height,
                         temp.rgba_frame->data,
                         temp.rgba_frame->linesize);
      if (result <= 0) {
        LOGE("Player Error : Data convert fail");
        return NULL;
      }
#endif

      // 播放
      result = ANativeWindow_lock(native.native_window, &native.window_buffer, NULL);
      if (result < 0) {
        LOGE("Player Error : Can not lock native window");
        return NULL;
      }
//      LOGE("window_buffer.format=%d",window_buffer.format);
//      LOGE("window_buffer.bits=%d",*((uint8_t *)window_buffer.bits));
//      LOGE("window_buffer.stride=%d",window_buffer.stride);

      // 在界面上绘制图形（如果rgba_frame和window_buffer的行大小不同，可能会花屏）
      uint8_t *bits = (uint8_t *) native.window_buffer.bits;
#ifndef USE_YUV
      for (int h = 0; h < player_info.video_height; h++) {
        memcpy(bits + h * native.window_buffer.stride * 4,
               native.video_out_buffer + h * temp.rgba_frame->linesize[0],
               (size_t) temp.rgba_frame->linesize[0]);
      }
#else
      for (int h = 0; h < video_height; h++) {
        memcpy(bits + (uint8_t)(h * window_buffer.stride * 1.5),
               video_out_buffer + h * frame->linesize[0],
               (size_t) frame->linesize[0]);
      }
#endif
      ANativeWindow_unlockAndPost(native.native_window);
    } else if (temp.packet->stream_index == player_info.audio_stream_index) {
      //Audio stream matched
      result = avcodec_send_packet(player_info.audio_codec_context, temp.packet);
      if (result < 0 && result != AVERROR(EAGAIN) && result != AVERROR_EOF) {
        LOGE("Player Error : Can not send packet (codec step 1)");
        return NULL;
      }
      result = avcodec_receive_frame(player_info.audio_codec_context, temp.frame);
      if (result < 0 && result != AVERROR_EOF) {
        LOGE("Player Error : Can not receive frame (codec step 2)");
        return NULL;
      }

//      if (skip_audio>=1){
//        skip_audio=0;
//        continue;
//      }
//      skip_audio++;
//      audio_count++;
//      if(audio_count>80000){
//        LOGE("ERROR!!!!!!!!!!!!!!!!!!!AUDIO");
//      }

      //Resample
      swr_convert(player_info.swr_context,
                  &temp.audio_out_buffer,
                  player_info.out_sample_rate * 2,
                  (const uint8_t **) temp.frame->data,
                  temp.frame->nb_samples);

      int size = av_samples_get_buffer_size(NULL,
                                            num_channels,
                                            temp.frame->nb_samples,
                                            player_info.out_format,
                                            1);
      jbyteArray audio_sample_array = env->NewByteArray(size);
      env->SetByteArrayRegion(audio_sample_array, 0, size, (const jbyte *) temp.audio_out_buffer);
      env->CallVoidMethod(instance,
                          reflection.add_to_audio_track_method_id,
                          audio_sample_array,
                          size);
      env->DeleteLocalRef(audio_sample_array);
      //Update playing progress
      env->CallVoidMethod(callback,
                          reflection.on_progress_method_id,
                          temp.packet->pts
                              * av_q2d(player_info.format_context->streams[player_info.audio_stream_index]->time_base),
                          player_info.total_length);
    }
    //Busy wait if paused
    while (paused) {
      if (stopped) {
        break;
      }
    }
    // 释放packet引用
    av_packet_unref(temp.packet);
    if (need_seek) {
      result = av_seek_frame(player_info.format_context,
                             player_info.video_stream_index,
                             (int64_t) (seek_dest
                                 / av_q2d(player_info.format_context->streams[player_info.video_stream_index]->time_base)),
                             AVSEEK_FLAG_BACKWARD);
      if (result < 0) {
        LOGE("Player Error : Failed to seek video frame");
        break;
      }
      result = av_seek_frame(player_info.format_context,
                             player_info.audio_stream_index,
                             (int64_t) (seek_dest
                                 / av_q2d(player_info.format_context->streams[player_info.audio_stream_index]->time_base)),
                             AVSEEK_FLAG_BACKWARD);
      if (result < 0) {
        LOGE("Player Error : Failed to seek audio frame");
        break;
      }
      need_seek = false;
    }
  }
//  LOGE("video_count=%lld,audio_count=%lld", video_count, audio_count);
#ifndef USE_YUV
  sws_freeContext(data_convert_context);
#endif
  //Release allocated resources.
  env->CallVoidMethod(instance, reflection.release_audio_track_method_id);
  env->DeleteGlobalRef(instance);
  delete (Arguments *) args;
  release_resources();
  jvm->DetachCurrentThread();
  return NULL;
}

extern "C"
JNIEXPORT void JNICALL
Java_org_ecnu_ryuou_player_Player_stopByNative(JNIEnv *env, jobject instance) {
  stopped = true;
}

extern "C"
JNIEXPORT void JNICALL
Java_org_ecnu_ryuou_player_Player_pauseByNative(JNIEnv *env, jobject thiz) {
  paused = true;
}

extern "C"
JNIEXPORT void JNICALL
Java_org_ecnu_ryuou_player_Player_seekToByNative(JNIEnv *env, jobject thiz, jdouble dest) {
  seek_dest = dest;
  need_seek = true;
}

extern "C"
JNIEXPORT void JNICALL
Java_org_ecnu_ryuou_player_Player_cutByNative(JNIEnv *env, jobject thiz, jstring path_, jdouble start, jdouble dest) {

  // result variable saving temporary return value
  int result = 0;

  AVFormatContext *in_format_context = NULL;
  AVFormatContext *out_format_context = NULL;
  AVOutputFormat *output_format = NULL;

  const char *in_filename = env->GetStringUTFChars(path_, 0);

  // use file protocol
  char out_filename[50] = "file://";

  // generate output filename (add "cut" after origin name)
  const char* suffix = strrchr(in_filename, '.');
  strncpy(out_filename, in_filename, suffix - in_filename);
  strcat(out_filename, "cut.mp4");
  //strcat(out_filename, suffix);

  in_format_context = avformat_alloc_context();

  // initialize input and output context
  result = avformat_open_input(&in_format_context, in_filename, 0, 0);
  if (result < 0) {
    LOGE("Player Error : Failed to open input");
    return;
  }
  result = avformat_alloc_output_context2(&out_format_context, NULL, NULL, out_filename);
  if (result < 0) {
    LOGE("Player Error : Failed to create output context");
    return;
  }
  output_format = out_format_context->oformat;

  // create output stream and copy parameters
  for (int i = 0; i < in_format_context->nb_streams; i++) {
    AVStream *in_stream = in_format_context->streams[i];
    AVStream *out_stream = avformat_new_stream(out_format_context, NULL);
    if (!out_stream) {
      LOGE("Player Error : Failed to allocate output stream");
      return;
    }
    avcodec_parameters_copy(out_stream->codecpar, in_stream->codecpar);
    out_stream->codecpar->codec_tag = 0;
  }

  /* open the output file */
  if (!(output_format->flags & AVFMT_NOFILE))
  {
    result = avio_open(&out_format_context->pb, out_filename, AVIO_FLAG_WRITE);
    if (result < 0)
    {
      LOGE("Player Info: Cannot open url");
      return;
    }
  }

  result = avformat_write_header(out_format_context, NULL);
  if (result < 0) {
    LOGE("Player Error : Failed to open file");
    return;
  }

  // seek to start point in both audio and video streams
  result = av_seek_frame(in_format_context,
                         0,
                         (int64_t) (start / av_q2d(in_format_context->streams[0]->time_base)),
                         AVSEEK_FLAG_ANY);
  if (result < 0) {
    LOGE("Player Error : Failed to seek start point in stream[0]");
    return;
  }
  result = av_seek_frame(in_format_context,
                         1,
                         (int64_t) (start / av_q2d(in_format_context->streams[1]->time_base)),
                         AVSEEK_FLAG_ANY);
  if (result < 0) {
    LOGE("Player Error : Failed to seek start point in stream[1]");
    return;
  }
  LOGI("Player Info: found start point");

  // allocate dts_start_from (depression timestamp) and initialize it to 0's
  int64_t *dts_start_from = (int64_t*)malloc(sizeof(int64_t) * in_format_context->nb_streams);
  memset(dts_start_from, 0, sizeof(int64_t) * in_format_context->nb_streams);

  // allocate pts_start_from (presentation timestamp) and initialize it to 0's
  int64_t *pts_start_from = (int64_t*)malloc(sizeof(int64_t) * in_format_context->nb_streams);
  memset(pts_start_from, 0, sizeof(int64_t) * in_format_context->nb_streams);

  AVPacket packet;
  while (1) {
    AVStream *in_stream, *out_stream;


    // read data
    result = av_read_frame(in_format_context, &packet);
    if (result < 0) {
      LOGE("Player Error : Failed to read frame, or reach the end of file");
      break;
    }

    in_stream = in_format_context->streams[packet.stream_index];
    out_stream = out_format_context->streams[packet.stream_index];

    // 时间超过要截取的时间，就退出循环
    if (av_q2d(in_stream->time_base) * packet.pts > dest) {
      av_packet_unref(&packet);
      break;
    }

    // save the first packet's dts and pts
    if (dts_start_from[packet.stream_index] == 0) {
      dts_start_from[packet.stream_index] = packet.dts;
    }
    if (pts_start_from[packet.stream_index] == 0) {
      pts_start_from[packet.stream_index] = packet.pts;
    }

    // rescale timebase of pts, dts and duration from input timebase to output timebase
    packet.pts = av_rescale_q_rnd(packet.pts - pts_start_from[packet.stream_index], in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF);
    packet.dts = av_rescale_q_rnd(packet.dts - dts_start_from[packet.stream_index], in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF);
    if (packet.pts < 0) {
      packet.pts = 0;
    }
    if (packet.dts < 0) {
      packet.dts = 0;
    }
    packet.duration = (int)av_rescale_q((int64_t)packet.duration, in_stream->time_base, out_stream->time_base);

    packet.pos = -1;

    // pts should be behind dts, just throw away error packets
    if (packet.pts < packet.dts) {
      continue;
    }

    result = av_interleaved_write_frame(out_format_context, &packet);
    if (result < 0) {
      LOGE("Player Error : Failed to write frame to the output file,");
      break;
    }

    av_packet_unref(&packet);
  }

  free(dts_start_from);
  free(pts_start_from);

  // write tail
  av_write_trailer(out_format_context);
}