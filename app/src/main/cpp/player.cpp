//
// Created by Shen on 2020/5/19.
//

#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/log.h>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
}

#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR, "player", FORMAT, ##__VA_ARGS__);

bool toContinue = true;

const char *path;
AVFormatContext *format_context;
int video_stream_index;
AVCodecContext *video_codec_context;
AVCodec *video_codec;
ANativeWindow_Buffer window_buffer;
AVPacket *packet;
AVFrame *frame;
AVFrame *rgba_frame;
int video_width;
int video_height;
ANativeWindow *native_window;
int buffer_size;
uint8_t *out_buffer;
//struct SwsContext *data_convert_context;

// 以下代码按照 C 语言编译执行
extern "C"
JNIEXPORT void JNICALL
Java_org_ecnu_ryuou_player_Player_initByNative(JNIEnv *env, jobject instance, jstring path_, jobject surface) {
    // 临时存放函数调用结果（状态）
    int result;

    // Java String -> C String
    path = env->GetStringUTFChars(path_, 0);
    // 注册
    //av_register_all();

    // 初始化 AVformat_context
    format_context = avformat_alloc_context();

    // 打开视频文件
    result = avformat_open_input(&format_context, path, NULL, NULL);
    if (result < 0) {
        LOGE("Player Error : Can not open video file");
        return;
    }

    // 查找流信息
    result = avformat_find_stream_info(format_context, NULL);
    if (result < 0) {
        LOGE("Player Error : Can not find stream info");
        return;
    }

    // 查找编码器
    video_stream_index = -1;
    for (int i = 0; i < format_context->nb_streams; i++) {
        if (format_context->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_stream_index = i;
        }
    }
    if (video_stream_index < 0) {
        LOGE("Player Error : Can not find video stream");
        return;
    }

    // 初始化编码器上下文 AVCodecContext
    video_codec_context = avcodec_alloc_context3(NULL);
    avcodec_parameters_to_context(video_codec_context, format_context->streams[video_stream_index]->codecpar);

    // 初始化编码器 AVCodec
    video_codec = avcodec_find_decoder(video_codec_context->codec_id);
    if (video_codec == NULL) {
        LOGE("Player Error : Can not find video codec");
        return;
    }

    // 打开编码器
    result = avcodec_open2(video_codec_context, video_codec, NULL);
    if (result < 0) {
        LOGE("Player Error : Can not open stream");
        return;
    }

    video_width = video_codec_context->width;
    video_height = video_codec_context->height;

    // 初始化 NativeWindow
    native_window = ANativeWindow_fromSurface(env, surface);
    if (native_window == NULL) {
        LOGE("Player Error : Can not create native window");
        return;
    }

    // 设置缓冲区尺寸为视频尺寸（而非物理尺寸）
    result = ANativeWindow_setBuffersGeometry(native_window, video_width, video_height, WINDOW_FORMAT_RGBA_8888);
    if (result < 0) {
        LOGE("Player Error : Can not create native window buffer");
        ANativeWindow_release(native_window);
        return;
    }

    // 解码前数据容器
    packet = av_packet_alloc();

    // 解码后数据容器（像素数据）
    frame = av_frame_alloc();

    // 转码后数据容器（直接播放）
    rgba_frame = av_frame_alloc();

    // 申请输出 buffer
    buffer_size = av_image_get_buffer_size(AV_PIX_FMT_RGBA, video_width, video_height, 1);
    out_buffer = (uint8_t *)av_malloc(buffer_size * sizeof(uint8_t));
    av_image_fill_arrays(rgba_frame->data, rgba_frame->linesize, out_buffer, AV_PIX_FMT_RGBA, video_width, video_height, 1);

    toContinue = true;
}

extern "C"
JNIEXPORT void JNICALL
Java_org_ecnu_ryuou_player_Player_playByNative(JNIEnv *env, jobject instance) {

    // 临时存放函数调用结果（状态）
    int result;

    // 数据格式转换上下文
    struct SwsContext *data_convert_context = data_convert_context = sws_getContext(video_width, video_height, video_codec_context->pix_fmt,
                                          video_width, video_height, AV_PIX_FMT_RGBA,
                                          SWS_BICUBIC, NULL, NULL, NULL);

    // 播放
    while (av_read_frame(format_context, packet) >= 0 && toContinue == true) {
        // 匹配视频流
        if (packet->stream_index == video_stream_index) {
            // 解码
            result = avcodec_send_packet(video_codec_context, packet);
            if (result < 0 && result != AVERROR(EAGAIN) && result != AVERROR_EOF) {
                LOGE("Player Error : Can not send packet (codec step 1)");
                return;
            }

            result = avcodec_receive_frame(video_codec_context, frame);
            if (result < 0 && result != AVERROR_EOF) {
                LOGE("Player Error : Can not receive frame (codec step 2)");
                return;
            }

            // 数据格式转换
            result = sws_scale(data_convert_context, (const uint8_t* const*)frame->data,
                    frame->linesize, 0, video_height, rgba_frame->data, rgba_frame->linesize);
            if (result <= 0) {
                LOGE("Player Error : Data convert fail");
                return;
            }
            
            // 播放
            result = ANativeWindow_lock(native_window, &window_buffer, NULL);
            if (result < 0) {
                LOGE("Player Error : Can not lock native window");
                return;
            }

            // 在界面上绘制图形（如果rgba_frame和window_buffer的行大小不同，可能会花屏）
            uint8_t *bits = (uint8_t*)window_buffer.bits;
            for (int h = 0; h < video_height; h++) {
                memcpy(bits + h * window_buffer.stride * 4,
                        out_buffer + h * rgba_frame->linesize[0],
                       (size_t)rgba_frame->linesize[0]);
            }
            ANativeWindow_unlockAndPost(native_window);
        }
        // 释放packet引用
        av_packet_unref(packet);
    }
    sws_freeContext(data_convert_context);
    av_free(out_buffer);
    av_frame_free(&rgba_frame);
    av_frame_free(&frame);
    av_packet_free(&packet);
    ANativeWindow_release(native_window);
    avcodec_close(video_codec_context);
    avformat_close_input(&format_context);
    //env->ReleaseStringUTFChars(path_, path);
}

/* 还不能用
extern "C"
JNIEXPORT void JNICALL
Java_org_ecnu_ryuou_player_Player_stopByNative(JNIEnv *env, jobject instance) {
    toContinue = false;
} */