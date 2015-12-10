#include "NativeSocket.h"

extern "C"
{
    JNIEXPORT int JNICALL Java_de_xavaro_android_safehome_NativeSocket_FortyTwo(JNIEnv* env, jobject obj, jstring text, jint port);
}

int Java_de_xavaro_android_safehome_NativeSocket_FortyTwo(JNIEnv* env, jobject obj, jstring text, jint port)
{
    const char* strChars = env->GetStringUTFChars(text, (jboolean *) 0);

    LOGE("&&&&&&&&%s&&=%d",strChars,port);

    struct sockaddr_in si_me,si_other;

    memset((char *) &si_me, 0, sizeof(si_me));

    si_me.sin_family = AF_INET;
    si_me.sin_port = htons(port);
    si_me.sin_addr.s_addr = htonl(INADDR_ANY);

    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);

    LOGE("&&&&&&&& Socket=%d",sockfd);

    int error;

    socklen_t ttl = 200;

    error = setsockopt(sockfd, IPPROTO_IP, IP_TTL, &ttl, sizeof(socklen_t));
    LOGE("&&&&&&&& setsockopt-TTL=%d",error);

    socklen_t enable = 1;
    error = setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(socklen_t));
    LOGE("&&&&&&&& setsockopt-REUSE=%d",error);

    error = bind(sockfd,(struct sockaddr *) &si_me, sizeof(si_me));

    LOGE("&&&&&&&& bind=%d",error);


    memset((char *) &si_other, 0, sizeof(si_other));
    si_other.sin_family = AF_INET;
    si_other.sin_port = htons(42742);
    inet_aton("176.9.65.213", &si_other.sin_addr);


    sendto(sockfd, strChars, 5, 0, (struct sockaddr*)&si_other, sizeof(si_other));

    return 42;
}