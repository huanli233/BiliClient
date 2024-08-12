package com.netease.hearttouch.brotlij;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by hanpfei0306 on 17-4-5.
 */

public class BrotliDeCompressor {
    private static final int DATA_IN_BUFFER_SIZE = 16 * 1024;

    private long mDecoderInstance;

    private byte[] mDataInBuffer;

    private ByteArrayOutputStream mOutputByteArrayOS;

    public BrotliDeCompressor() {
        mDecoderInstance = nativeCreateBrotliDeCompressorInstance();
        mOutputByteArrayOS = new ByteArrayOutputStream();
    }

    private void checkState() {
        if (mOutputByteArrayOS == null) {
            throw new IllegalStateException("The encoder has been finished.");
        }
    }

    public void decompressData(byte[] data, int startPos, int length) {
        checkState();

        int remainingStart = startPos;
        int endPos = startPos + length;

        if (mDataInBuffer == null) {
            mDataInBuffer = new byte[DATA_IN_BUFFER_SIZE];
        }
        byte[] dataInBuffer = mDataInBuffer;
        while (remainingStart + dataInBuffer.length < endPos) {
            System.arraycopy(data, remainingStart, dataInBuffer, 0, dataInBuffer.length);
            nativeDeCompress(mDecoderInstance, dataInBuffer, 0, dataInBuffer.length);

            remainingStart += dataInBuffer.length;
        }

        System.arraycopy(data, remainingStart, dataInBuffer, 0, endPos - remainingStart);
        nativeDeCompress(mDecoderInstance, dataInBuffer, 0, endPos - remainingStart);
    }

    // Called by native.
    private void writeCompressedData(byte[] compressedData, int start, int length) {
        mOutputByteArrayOS.write(compressedData, start, length);
    }

    public byte[] toByteArray() {
        checkState();

        return mOutputByteArrayOS.toByteArray();
    }

    public boolean decompressFile(String inputFilePath, String outputFilePath) {
        boolean ret = nativeDeCompressFile(mDecoderInstance, inputFilePath, outputFilePath);
        return ret;
    }

    public void finish() {
        checkState();

        nativeDestroyBrotliDeCompressorInstance(mDecoderInstance);
        mOutputByteArrayOS = null;
    }

    private native boolean nativeDeCompressFile(long decoderInstance, String inputFilePath, String outputFilePath);

    private native long nativeCreateBrotliDeCompressorInstance();
    private native int nativeDeCompress(long decoderInstance, byte[] data, int startPos, int length);
    private native void nativeDestroyBrotliDeCompressorInstance(long decoderInstance);
}
