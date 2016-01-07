//
// Created by Dennis Zierahn on 10.12.15.
//

#ifndef SAFEHOME_RAWSOCKET_H
#define SAFEHOME_RAWSOCKET_H

#include <jni.h>
#include <android/log.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <unistd.h>

#define DEBUG 1

#define LOG_TAG "RAWSOCKET"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#endif //SAFEHOME_RAWSOCKET_H
