//
// Created by Tel on 9/25/2020.
//

#include <jni.h>
#include <string>
#include <iostream>

extern "C" JNIEXPORT jstring JNICALL

Java_fun_flexpad_com_RoomChatActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Click for random number";
    return env->NewStringUTF(hello.c_str());
}