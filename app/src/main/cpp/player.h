//
// Created by LSZ on 2020/5/23.
//

#ifndef RYUOU_PLAYER_H
#define RYUOU_PLAYER_H

//#define USE_YUV
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR, "player", FORMAT, ##__VA_ARGS__)
extern "C" void release_resources();
extern "C" void *play_thread(void *);
#endif //RYUOU_PLAYER_H
