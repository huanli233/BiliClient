//
// Created by hanpfei0306 on 17-4-5.
//

#include <stdlib.h>

#include <brotli/encode.h>

#include "jni.h"

#include "JNIHelper.h"

#define TAG "BrotliCompressor"

static jlong BrotliCompressor_nativeCreateBrotliCompressorInstance(JNIEnv *env, jobject, jint jmode, jint jquality,
                                                                   jint jlgwin, jint jlgblock) {
    uint32_t mode = (uint32_t) jmode;
    uint32_t quality = (uint32_t) jquality;
    uint32_t lgwin = (uint32_t) jlgwin;
    uint32_t lgblock = (uint32_t) jlgblock;

    BrotliEncoderState *s = BrotliEncoderCreateInstance(0, 0, 0);

    BrotliEncoderSetParameter(s, BROTLI_PARAM_MODE, mode);
    BrotliEncoderSetParameter(s, BROTLI_PARAM_QUALITY, quality);
    BrotliEncoderSetParameter(s, BROTLI_PARAM_LGWIN, lgwin);
    BrotliEncoderSetParameter(s, BROTLI_PARAM_LGBLOCK, lgblock);

    return reinterpret_cast<jlong>(s);
}

static jint BrotliCompressor_nativeCompress(JNIEnv *env, jobject, jlong jencoderInstance, jbyteArray javaData, jint jstart, jint jlength,
                                            jbyteArray javaBuffer, jboolean jIsEof) {
    jbyte *data = env->GetByteArrayElements(javaData, NULL);
    uint8_t *nativeData = (uint8_t *)(data + jstart);
    size_t nativeDataLength = jlength;

    jbyte *buffer = env->GetByteArrayElements(javaBuffer, NULL);
    uint8_t *nativeBuffer = (uint8_t *)buffer;
    size_t nativeBufferSize = env->GetArrayLength(javaBuffer);

    BrotliEncoderState *encoderState = reinterpret_cast<BrotliEncoderState *>(jencoderInstance);

    int isEof = jIsEof;

    size_t totalOut = -1;
    if (encoderState && nativeBuffer) {
        if (!BrotliEncoderCompressStream(encoderState, isEof ? BROTLI_OPERATION_FINISH : BROTLI_OPERATION_PROCESS,
                                         &nativeDataLength, (const uint8_t **) &nativeData, &nativeBufferSize,
                                         &nativeBuffer, &totalOut)) {
        }
    }
//    LOGW("dataLength %d, bufferSize %d, totalOut %d", dataLength, bufferSize, totalOut);

    env->ReleaseByteArrayElements(javaData, data, 0);
    env->ReleaseByteArrayElements(javaBuffer, buffer, 0);
    return totalOut;
}

static const size_t kFileBufferSize = 65536;

static jboolean BrotliCompressor_nativeCompressFile(JNIEnv *env, jobject, jlong jencoderInstance,
                                                    jstring jinputFilePath, jstring joutputFilePath) {
    const char *inputFilePath = env->GetStringUTFChars(jinputFilePath, NULL);
    const char *outputFilePath = env->GetStringUTFChars(joutputFilePath, NULL);

    BrotliEncoderState *encoderInstance = reinterpret_cast<BrotliEncoderState *>(jencoderInstance);

    uint8_t *buffer = (uint8_t *) malloc(kFileBufferSize << 1);
    uint8_t *input = buffer;
    uint8_t *output = buffer + kFileBufferSize;

    FILE *fin = NULL;
    FILE *fout = NULL;
    int isOk = 0;

    if (encoderInstance && buffer) {
        size_t availableIn = 0;
        const uint8_t *nextIn = NULL;

        size_t availableOut = kFileBufferSize;
        uint8_t *nextOut = output;

        int isEof = 0;

        fin = openInputFile(inputFilePath);
        fout = openOutputFile(outputFilePath);

        while (1) {
            if (availableIn == 0 && !isEof) {
                availableIn = fread(input, 1, kFileBufferSize, fin);
                nextIn = input;
                if (ferror(fin)) break;
                isEof = feof(fin);
            }

            if (!BrotliEncoderCompressStream(encoderInstance,
                                             isEof ? BROTLI_OPERATION_FINISH
                                                   : BROTLI_OPERATION_PROCESS,
                                             &availableIn, &nextIn, &availableOut, &nextOut,
                                             NULL)) {
                isOk = 0;
                break;
            }

            if (availableOut != kFileBufferSize) {
                size_t outSize = kFileBufferSize - availableOut;
                fwrite(output, 1, outSize, fout);
                if (ferror(fout)) break;
                availableOut = kFileBufferSize;
                nextOut = output;
            }

            if (BrotliEncoderIsFinished(encoderInstance)) {
                isOk = 1;
                break;
            }
        }
    }

    if (fin != NULL) {
        fclose(fin);
    }
    if (fout != NULL) {
        fclose(fout);
    }

    free(buffer);

    env->ReleaseStringUTFChars(joutputFilePath, outputFilePath);
    env->ReleaseStringUTFChars(jinputFilePath, inputFilePath);

    if (isOk) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

static void BrotliCompressor_nativeDestroyBrotliCompressorInstance(JNIEnv *env, jobject, jlong jencoderInstance) {
    BrotliEncoderState *encoderInstance = reinterpret_cast<BrotliEncoderState *>(jencoderInstance);
    if (encoderInstance) {
        //    BrotliEncoderIsFinished(s);
        BrotliEncoderDestroyInstance(encoderInstance);
    }
}

static JNINativeMethod gBrotliCompressorMethods[] = {
        NATIVE_METHOD(BrotliCompressor, nativeCreateBrotliCompressorInstance, "(IIII)J"),
        NATIVE_METHOD(BrotliCompressor, nativeCompress, "(J[BII[BZ)I"),
        NATIVE_METHOD(BrotliCompressor, nativeCompressFile, "(JLjava/lang/String;Ljava/lang/String;)Z"),
        NATIVE_METHOD(BrotliCompressor, nativeDestroyBrotliCompressorInstance, "(J)V"),
};

void register_com_wolfcstech_BrotliCompressor(JNIEnv *env) {
    jniRegisterNativeMethods(env, "com/netease/hearttouch/brotlij/BrotliCompressor",
                             gBrotliCompressorMethods, NELEM(gBrotliCompressorMethods));
}