
#include <stdlib.h>

#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>

#include <unistd.h>

#include "JNIHelper.h"

#define TAG "JNIHelper"

int jniRegisterNativeMethods(JNIEnv* env, const char *classPathName, JNINativeMethod *nativeMethods, jint nMethods) {
    jclass clazz;
    clazz = env->FindClass(classPathName);
    if (clazz == NULL) {
        LOGW("Native registration unable to find class '%s'", classPathName);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, nativeMethods, nMethods) < 0) {
        LOGW("RegisterNatives failed for '%s'", classPathName);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

FILE* openInputFile(const char* input_path) {
    FILE* f = fopen(input_path, "rb");
    return f;
}

FILE *openOutputFile(const char *output_path) {
    int fd = open(output_path, O_CREAT | O_EXCL | O_WRONLY | O_TRUNC, S_IRUSR | S_IWUSR);
    FILE * file = NULL;
    if (fd > 0) {
        file = fdopen(fd, "wb");
    }
    return file;
}