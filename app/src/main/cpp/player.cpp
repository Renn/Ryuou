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
};
struct Arguments {
  jobject instance;
  Arguments(jobject instance) {
    this->instance = instance;
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
}

extern "C"
JNIEXPORT void JNICALL
Java_org_ecnu_ryuou_player_Player_playByNative(JNIEnv *env, jobject instance) {
  if (paused) {
    paused = false;
  } else {
    pthread_t t_play;
    instance = env->NewGlobalRef(instance);
    Arguments *args = new Arguments(instance);
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
      //Resample
      swr_convert(player_info.swr_context,
                  &temp.audio_out_buffer,
                  player_info.out_sample_rate * 2,
                  (const uint8_t **) temp.frame->data,
                  temp.frame->nb_samples);
      //todo:
      // Relationship between size and sizeof(temp.audio_out_buffer) ?
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
    }
    //Busy wait if paused
    while (paused) {
      if (stopped) {
        break;
      }
    }
    // 释放packet引用
    av_packet_unref(temp.packet);
  }
#ifndef USE_YUV
  sws_freeContext(data_convert_context);
#endif
  //Release allocated resources.
  env->CallVoidMethod(instance, reflection.release_audio_track_method_id);
  env->DeleteGlobalRef(instance);
  delete args;
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