
#include <stdlib.h>

#include "jni.h"

#include "JNIHelper.h"

#include "BrotliJni.h"

#define TAG "BrotliCompressor"

// DalvikVM calls this on startup, so we can statically register all our native methods.
jint JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        LOGE("JavaVM::GetEnv() failed");
        abort();
    }
    register_com_wolfcstech_BrotliCompressor(env);
    register_com_wolfcstech_BrotliDeCompressor(env);

    return JNI_VERSION_1_6;
}