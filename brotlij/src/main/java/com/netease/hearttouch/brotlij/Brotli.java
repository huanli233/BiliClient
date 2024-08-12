
package com.netease.hearttouch.brotlij;

import java.io.File;

public class Brotli {

    public static final String BROTLI_VERSION = "HEAD-2016-03-20-commit_f453b1bf363be71a23be9d1eec8bf7975dbcfc4e";

    public static final Mode DEFAULT_MODE = Mode.GENERIC;
    public static final int DEFAULT_QUALITY = 11;
    public static final int DEFAULT_LGWIN = 22;
    public static final int DEFAULT_LGBLOCK = 0;

    static {
        System.loadLibrary("brotli");
    }

    public enum Mode {

        /**
         * Default compression mode. The compressor does not know anything in advance about the properties of the input.
         */
        GENERIC(0),

        /**
         * Compression mode for UTF-8 format text input.
         */
        TEXT(1),

        /**
         * Compression mode used in WOFF 2.0.
         */
        FONT(2);

        public int mode;

        Mode(int mode) {
            this.mode = mode;
        }
    }

    public static byte[] compress(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        BrotliCompressor compressor = new BrotliCompressor();
        compressor.compressData(data, 0, data.length, true);

        byte[] bytes = compressor.toByteArray();
        compressor.finish();

        return bytes;
    }

    public static boolean compressFile(String inputFilePath, String outputFilePath) {
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new IllegalArgumentException("Input file doesn't exist: " + inputFilePath);
        }
        File outputFile = new File(outputFilePath);
        if (outputFile.exists()) {
            throw new IllegalArgumentException("Output file has been existing: " + outputFilePath);
        }

        BrotliCompressor compressor = new BrotliCompressor();

        boolean ret = compressor.compressFile(inputFilePath, outputFilePath);

        compressor.finish();
        return ret;
    }

    public static byte[] decompress(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        BrotliDeCompressor decompressor = new BrotliDeCompressor();
        decompressor.decompressData(data, 0, data.length);

        byte[] bytes = decompressor.toByteArray();
        decompressor.finish();

        return bytes;
    }

    public static boolean decompressFile(String inputFilePath, String outputFilePath) {
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new IllegalArgumentException("Input file doesn't exist: " + inputFilePath);
        }
        File outputFile = new File(outputFilePath);
        if (outputFile.exists()) {
            throw new IllegalArgumentException("Output file has been existing: " + outputFilePath);
        }

        BrotliDeCompressor decompressor = new BrotliDeCompressor();

        boolean ret = decompressor.decompressFile(inputFilePath, outputFilePath);

        decompressor.finish();
        return ret;
    }
}
