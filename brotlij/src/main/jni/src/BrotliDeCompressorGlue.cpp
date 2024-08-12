//
// Created by hanpfei0306 on 17-4-5.
//

#include <stdio.h>
#include <stdlib.h>

#include <brotli/decode.h>

#include "jni.h"

#include "JNIHelper.h"

#define TAG "BrotliDeCompressor"

static jmethodID sWriteCompressedDataMethodId;

static jlong BrotliDeCompressor_nativeCreateBrotliDeCompressorInstance(JNIEnv *env, jobject) {
    BrotliDecoderState* decoderInstance = BrotliDecoderCreateInstance(NULL, NULL, NULL);

    return reinterpret_cast<jlong>(decoderInstance);
}

static const size_t kFileBufferSize = 65536;

static void writeCompressedData(JNIEnv *env, jobject jobj, jbyteArray jbytes, int start, int length) {
    env->CallVoidMethod(jobj, sWriteCompressedDataMethodId, jbytes, start, length);
}

static jint BrotliDeCompressor_nativeDeCompress(JNIEnv *env, jobject jobj, jlong jencoderInstance,
                                                jbyteArray javaData, jint jstart, jint jlength) {
    BrotliDecoderState* decoderInstance = reinterpret_cast<BrotliDecoderState*>(jencoderInstance);
    jbyte *data = env->GetByteArrayElements(javaData, NULL);

    const uint8_t* nextIn = (uint8_t *)(data + jstart);
    size_t availableIn = jlength;

    uint8_t* output = (uint8_t*)malloc(kFileBufferSize);
    size_t totalOut = 0;

    if (output && decoderInstance) {
        size_t availableOut = kFileBufferSize;
        uint8_t *nextOut = output;

        BrotliDecoderResult result = BROTLI_DECODER_RESULT_ERROR;

        jbyteArray jbytes = env->NewByteArray(kFileBufferSize);
        while (1) {
            size_t outLength = 0;
            result = BrotliDecoderDecompressStream(
                    decoderInstance, &availableIn, &nextIn, &availableOut, &nextOut, &outLength);
            totalOut += outLength;

            if (result == BROTLI_DECODER_RESULT_NEEDS_MORE_OUTPUT) {
                env->SetByteArrayRegion(jbytes, 0, kFileBufferSize, (jbyte *)output);
                writeCompressedData(env, jobj, jbytes, 0, kFileBufferSize);
                availableOut = kFileBufferSize;
                nextOut = output;
            } else {
                break; /* Error or success. */
            }
        }
        if (nextOut != output) {
            env->SetByteArrayRegion(jbytes, 0, (size_t) (nextOut - output), (jbyte *)output);
            writeCompressedData(env, jobj, jbytes, 0, (size_t) (nextOut - output));
        }
    }

    free(output);
    env->ReleaseByteArrayElements(javaData, data, 0);

    return totalOut;
}

static jboolean BrotliDeCompressor_nativeDeCompressFile(JNIEnv *env, jobject, jlong jencoderInstance,
                                                        jstring jinputFilePath, jstring joutputFilePath) {
    const char *inputFilePath = env->GetStringUTFChars(jinputFilePath, NULL);
    const char *outputFilePath = env->GetStringUTFChars(joutputFilePath, NULL);

    BrotliDecoderState* decoderInstance = reinterpret_cast<BrotliDecoderState*>(jencoderInstance);

    FILE *fin = NULL;
    FILE *fout = NULL;

    size_t availableIn;
    const uint8_t* nextIn;
    size_t availableOut = kFileBufferSize;
    uint8_t* nextOut;

    BrotliDecoderResult result = BROTLI_DECODER_RESULT_ERROR;

    uint8_t* input = (uint8_t*)malloc(kFileBufferSize);
    uint8_t* output = (uint8_t*)malloc(kFileBufferSize);

    if (input && output) {
        fin = openInputFile(inputFilePath);
        fout = openOutputFile(outputFilePath);

        nextOut = output;
        result = BROTLI_DECODER_RESULT_NEEDS_MORE_INPUT;
        while (1) {
            if (result == BROTLI_DECODER_RESULT_NEEDS_MORE_INPUT) {
                if (feof(fin)) {
                    break;
                }
                availableIn = fread(input, 1, kFileBufferSize, fin);
                nextIn = input;
                if (ferror(fin)) {
                    break;
                }
            } else if (result == BROTLI_DECODER_RESULT_NEEDS_MORE_OUTPUT) {
                fwrite(output, 1, kFileBufferSize, fout);
                if (ferror(fout)) {
                    break;
                }
                availableOut = kFileBufferSize;
                nextOut = output;
            } else {
                break; /* Error or success. */
            }
            result = BrotliDecoderDecompressStream(
                    decoderInstance, &availableIn, &nextIn, &availableOut, &nextOut, 0);
        }
        if (nextOut != output) {
            fwrite(output, 1, (size_t) (nextOut - output), fout);
        }

        if ((result == BROTLI_DECODER_RESULT_NEEDS_MORE_OUTPUT) || ferror(fout)) {
            LOGW("failed to write output\n");
        } else if (result != BROTLI_DECODER_RESULT_SUCCESS) {
            LOGW("corrupt input\n");
        }
    }

    if (input) {
        free(input);
    }
    if (output) {
        free(output);
    }
    if (fin) {
        fclose(fin);
    }
    if (fout) {
        fclose(fout);
    }

    env->ReleaseStringUTFChars(joutputFilePath, outputFilePath);
    env->ReleaseStringUTFChars(jinputFilePath, inputFilePath);

    return (result == BROTLI_DECODER_RESULT_SUCCESS) ? JNI_TRUE : JNI_FALSE;
}

static void BrotliDeCompressor_nativeDestroyBrotliDeCompressorInstance(JNIEnv *env, jobject, jlong jencoderInstance) {
    BrotliDecoderState* decoderInstance = reinterpret_cast<BrotliDecoderState*>(jencoderInstance);
    if (decoderInstance) {
        BrotliDecoderDestroyInstance(decoderInstance);
    }
}

static JNINativeMethod gBrotliDeCompressorMethods[] = {
        NATIVE_METHOD(BrotliDeCompressor, nativeCreateBrotliDeCompressorInstance, "()J"),
        NATIVE_METHOD(BrotliDeCompressor, nativeDeCompress, "(J[BII)I"),
        NATIVE_METHOD(BrotliDeCompressor, nativeDeCompressFile, "(JLjava/lang/String;Ljava/lang/String;)Z"),
        NATIVE_METHOD(BrotliDeCompressor, nativeDestroyBrotliDeCompressorInstance, "(J)V"),
};

void register_com_wolfcstech_BrotliDeCompressor(JNIEnv *env) {
    jniRegisterNativeMethods(env, "com/netease/hearttouch/brotlij/BrotliDeCompressor",
                             gBrotliDeCompressorMethods, NELEM(gBrotliDeCompressorMethods));
    jclass clazz = env->FindClass("com/netease/hearttouch/brotlij/BrotliDeCompressor");
    // jclass clazz, const char* name, const char* sig
    // void writeCompressedData(byte[] compressedData)
    sWriteCompressedDataMethodId = env->GetMethodID(clazz, "writeCompressedData", "([BII)V");
}