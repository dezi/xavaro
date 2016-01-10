#include "NativeSocket.h"

extern "C"
{
JNIEXPORT int JNICALL Java_de_xavaro_android_common_NativeSocket_nativeCreate(
        JNIEnv* env, jobject obj);

JNIEXPORT int JNICALL Java_de_xavaro_android_common_NativeSocket_nativeClose(
        JNIEnv* env, jobject obj,
        jint socketfd);

JNIEXPORT int JNICALL Java_de_xavaro_android_common_NativeSocket_nativeGetTTL(
        JNIEnv* env, jobject obj,
        jint socketfd);

JNIEXPORT int JNICALL Java_de_xavaro_android_common_NativeSocket_nativeSetTTL(
        JNIEnv* env, jobject obj,
        jint socketfd,
        jint ttl);

JNIEXPORT int JNICALL Java_de_xavaro_android_common_NativeSocket_nativeSend(
        JNIEnv* env, jobject obj,
        jint socketfd,
        jbyteArray data, jint offset,jint length,
        jstring destip, jint destport);

JNIEXPORT int JNICALL Java_de_xavaro_android_common_NativeSocket_nativeReceive(
        JNIEnv* env, jobject obj,
        jint socketfd,
        jbyteArray data,jint length);

JNIEXPORT jstring JNICALL Java_de_xavaro_android_common_NativeSocket_nativeStrError(
        JNIEnv* env, jobject obj,
        jint errnum);
}

int Java_de_xavaro_android_common_NativeSocket_nativeCreate(JNIEnv* env, jobject obj)
{
    int socketfd = socket(AF_INET, SOCK_DGRAM, 0);

    return socketfd;
}

int Java_de_xavaro_android_common_NativeSocket_nativeClose(
        JNIEnv* env, jobject obj,
        jint socketfd)
{
    return close(socketfd);
}

int Java_de_xavaro_android_common_NativeSocket_nativeGetTTL(
        JNIEnv* env, jobject obj,
        jint socketfd)
{
    socklen_t ttlval;
    socklen_t ttllen;

    int err = getsockopt(socketfd, IPPROTO_IP, IP_TTL, &ttlval, &ttllen);

    return (err < 0) ? err : ttlval;
}

int Java_de_xavaro_android_common_NativeSocket_nativeSetTTL(
        JNIEnv* env, jobject obj,
        jint socketfd,
        jint ttl)
{
    socklen_t ttlval = (socklen_t) ttl;

    int err = setsockopt(socketfd, IPPROTO_IP, IP_TTL, &ttlval, sizeof(socklen_t));

    return err;
}

int Java_de_xavaro_android_common_NativeSocket_nativeSend(
        JNIEnv* env, jobject obj,
        jint socketfd,
        jbyteArray data, jint offset, jint length,
        jstring destip, jint destport)
{
    //
    // Pepare destination ip and port.
    //

    const char *cdestip = env->GetStringUTFChars(destip, (jboolean *) 0);

    struct sockaddr_in dest_si;

    memset((char *) &dest_si, 0, sizeof(dest_si));

    dest_si.sin_family = AF_INET;
    dest_si.sin_port = htons(destport);
    inet_aton(cdestip, &dest_si.sin_addr);

    //
    // Dereference byte[] from java.
    //

    size_t clength = (size_t) length;
    char *cdata = (char *) env->GetByteArrayElements(data,(jboolean *) 0);

    ssize_t xfer = sendto(socketfd, cdata + offset, clength, 0, (struct sockaddr *) &dest_si, sizeof(dest_si));

    //
    // Release dereferenced item.
    //

    env->ReleaseByteArrayElements(data, (jbyte *) cdata, JNI_ABORT);

    return (int) xfer;
}

int Java_de_xavaro_android_common_NativeSocket_nativeReceive(
        JNIEnv* env, jobject obj,
        jint socketfd,
        jbyteArray data, jint length)
{
    size_t clength = (size_t) length;
    void *cdata = malloc(clength);

    ssize_t xfer = recv(socketfd,cdata,clength,0);

    if (xfer >= 0)
    {
        env->SetByteArrayRegion(data, 0, (int) xfer, (const jbyte*) cdata);
    }

    free(cdata);

    return (int) xfer;
}

JNIEXPORT jstring JNICALL Java_de_xavaro_android_common_NativeSocket_nativeStrError(
        JNIEnv* env, jobject obj,
        jint errnum)
{
    return env->NewStringUTF(strerror(errnum));
}
