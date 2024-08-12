include $(call all-subdir-makefiles)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#source code
LOCAL_SRC_FILES = $(LOCAL_PATH)/third_party/brotli/common/dictionary.c \
    $(LOCAL_PATH)/third_party/brotli/dec/bit_reader.c \
    $(LOCAL_PATH)/third_party/brotli/dec/decode.c \
    $(LOCAL_PATH)/third_party/brotli/dec/huffman.c \
    $(LOCAL_PATH)/third_party/brotli/dec/state.c \
    $(LOCAL_PATH)/third_party/brotli/enc/backward_references.c \
    $(LOCAL_PATH)/third_party/brotli/enc/backward_references_hq.c \
    $(LOCAL_PATH)/third_party/brotli/enc/bit_cost.c \
    $(LOCAL_PATH)/third_party/brotli/enc/block_splitter.c \
    $(LOCAL_PATH)/third_party/brotli/enc/brotli_bit_stream.c \
    $(LOCAL_PATH)/third_party/brotli/enc/cluster.c \
    $(LOCAL_PATH)/third_party/brotli/enc/compress_fragment.c \
    $(LOCAL_PATH)/third_party/brotli/enc/compress_fragment_two_pass.c \
    $(LOCAL_PATH)/third_party/brotli/enc/dictionary_hash.c \
    $(LOCAL_PATH)/third_party/brotli/enc/encode.c \
    $(LOCAL_PATH)/third_party/brotli/enc/entropy_encode.c \
    $(LOCAL_PATH)/third_party/brotli/enc/histogram.c \
    $(LOCAL_PATH)/third_party/brotli/enc/literal_cost.c \
    $(LOCAL_PATH)/third_party/brotli/enc/memory.c \
    $(LOCAL_PATH)/third_party/brotli/enc/metablock.c \
    $(LOCAL_PATH)/third_party/brotli/enc/static_dict.c \
    $(LOCAL_PATH)/third_party/brotli/enc/utf8_util.c


LOCAL_SRC_FILES += $(LOCAL_PATH)/src/BrotliJni.cpp \
    $(LOCAL_PATH)/src/BrotliCompressorGlue.cpp \
    $(LOCAL_PATH)/src/BrotliDeCompressorGlue.cpp \
    $(LOCAL_PATH)/src/JNIHelper.cpp

LOCAL_CFLAGS := -DDEBUG

# LOCAL_CPPFLAGS :=

LOCAL_C_INCLUDES += $(LOCAL_PATH)/third_party/brotli/include \
    $(LOCAL_PATH)/src/

LOCAL_LDLIBS := -llog -landroid

LOCAL_SHARED_LIBRARIES :=

LOCAL_MODULE:= brotli

include $(BUILD_SHARED_LIBRARY)