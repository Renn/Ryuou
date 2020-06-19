//
// Created by LSZ on 2020/6/5.
//

#include "editor.h"
#include <jni.h>
#include <android/log.h>
#include <time.h>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
#include <libswresample/swresample.h>
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_ecnu_ryuou_editor_Editor_cutByNative(JNIEnv *env,
                                              jobject thiz,
                                              jstring path_,
                                              jdouble start,
                                              jdouble dest) {
  // result variable saving temporary return value
  int result = 0;

  AVFormatContext *in_format_context = NULL;
  AVFormatContext *out_format_context = NULL;
  AVOutputFormat *output_format = NULL;

  const char *in_filename = env->GetStringUTFChars(path_, 0);

  // generate random output filename
  char in_filename_pure[100] = "";
  char out_filename[100] = "";
  const char *suffix = strrchr(in_filename, '.');
  strncpy(in_filename_pure, in_filename, suffix - in_filename);
  time_t cur_time = time(NULL);
  struct tm *cur_time_ptr = gmtime(&cur_time);
  sprintf(out_filename, "file:%s%d%d%d%d%d%d.mp4", in_filename_pure,
          cur_time_ptr->tm_year,
          cur_time_ptr->tm_mon,
          cur_time_ptr->tm_mday,
          cur_time_ptr->tm_hour,
          cur_time_ptr->tm_min,
          cur_time_ptr->tm_sec);

  jstring return_value = env->NewStringUTF(out_filename);

  in_format_context = avformat_alloc_context();

// initialize input and output context
  result = avformat_open_input(&in_format_context, in_filename, 0, 0);
  if (result < 0) {
    LOGE("Editor Error : Failed to open input");
    return return_value;
  }
  result = avformat_alloc_output_context2(&out_format_context, NULL, NULL, out_filename);
  if (result < 0) {
    LOGE("Editor Error : Failed to create output context");
    return return_value;
  }
  output_format = out_format_context->oformat;

// create output stream and copy parameters
  for (int i = 0; i < in_format_context->nb_streams; i++) {
    AVStream *in_stream = in_format_context->streams[i];
    AVStream *out_stream = avformat_new_stream(out_format_context, NULL);
    if (!out_stream) {
      LOGE("Editor Error : Failed to allocate output stream");
      return return_value;
    }
    avcodec_parameters_copy(out_stream->codecpar, in_stream->codecpar);
    out_stream->codecpar->codec_tag = 0;
  }

/* open the output file */
  if (!(output_format->flags & AVFMT_NOFILE)) {
    result = avio_open(&out_format_context->pb, out_filename, AVIO_FLAG_WRITE);
    if (result < 0) {
      LOGE("Editor Error: Cannot open url");
      return return_value;
    }
  }

  result = avformat_write_header(out_format_context, NULL);
  if (result < 0) {
    LOGE("Editor Error : Failed to open file");
    return return_value;
  }

// seek to start point in both audio and video streams
  result = av_seek_frame(in_format_context,
                         0,
                         (int64_t) (start / av_q2d(in_format_context->streams[0]->time_base)),
                         AVSEEK_FLAG_FRAME);
  if (result < 0) {
    LOGE("Editor Error : Failed to seek start point in stream[0]");
    return return_value;
  }
  result = av_seek_frame(in_format_context,
                         1,
                         (int64_t) (start / av_q2d(in_format_context->streams[1]->time_base)),
                         AVSEEK_FLAG_FRAME);
  if (result < 0) {
    LOGE("Editor Error : Failed to seek start point in stream[1]");
    return return_value;
  }

// allocate dts_start_from (depression timestamp) and initialize it to 0's
  int64_t *dts_start_from = (int64_t *) malloc(sizeof(int64_t) * in_format_context->nb_streams);
  memset(dts_start_from, 0, sizeof(int64_t) * in_format_context->nb_streams);

// allocate pts_start_from (presentation timestamp) and initialize it to 0's
  int64_t *pts_start_from = (int64_t *) malloc(sizeof(int64_t) * in_format_context->nb_streams);
  memset(pts_start_from, 0, sizeof(int64_t) * in_format_context->nb_streams);

  AVPacket packet;
  while (1) {
    AVStream *in_stream, *out_stream;

// read data
    result = av_read_frame(in_format_context, &packet);
    if (result < 0) {
      LOGE("Editor Error : Failed to read frame, or reach the end of file");
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
    packet.pts = av_rescale_q_rnd(packet.pts - pts_start_from[packet.stream_index],
                                  in_stream->time_base,
                                  out_stream->time_base,
                                  AV_ROUND_NEAR_INF);
    packet.dts = av_rescale_q_rnd(packet.dts - dts_start_from[packet.stream_index],
                                  in_stream->time_base,
                                  out_stream->time_base,
                                  AV_ROUND_NEAR_INF);
    if (packet.pts < 0) {
      packet.pts = 0;
    }
    if (packet.dts < 0) {
      packet.dts = 0;
    }
    packet.duration =
        (int) av_rescale_q((int64_t) packet.duration, in_stream->time_base, out_stream->time_base);

    packet.pos = -1;

// pts should be behind dts, just throw away error packets
    if (packet.pts < packet.dts) {
      continue;
    }

    result = av_interleaved_write_frame(out_format_context, &packet);
    if (result < 0) {
      LOGE("Editor Error : Failed to write frame to the output file,");
      break;
    }

    av_packet_unref(&packet);
  }

// write tail
  av_write_trailer(out_format_context);

  free(dts_start_from);
  free(pts_start_from);

  avformat_close_input(&in_format_context);
  avformat_free_context(in_format_context);
  avformat_free_context(out_format_context);

  LOGE("editor finished");

  return return_value;
}
