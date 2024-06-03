package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;

import com.RobinNotBad.BiliClient.util.Cookies;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Random;

/**
 * Cookies相关API
 */
public class CookiesApi {

    /**
     * 调用ExClimbWuzhi API激活Cookies，并检查如果有一些可本地生成的Cookie没有就顺带生成一下
     * @return 返回码
     */
    public static int activeCookieInfo() {
        String url = "https://api.bilibili.com/x/internal/gaia-gateway/ExClimbWuzhi";
        checkCookies();
        return -1;
    }

    private static void checkCookies() {
        Cookies cookies = NetWorkUtil.getCookies();

        // _uuid
        if (!cookies.containsKey("_uuid")) {
            cookies.set("_uuid", gen_uuid_infoc());
        }

        // b_lsid
        if (!cookies.containsKey("b_lsid")) {
            cookies.set("b_lsid", gen_b_lsid());
        }

        // buvid_fp. The generation of this parameter may not be accurate, can you also consider it as just a random generation?
        if (!cookies.containsKey("buvid_fp")) {
            cookies.set("buvid_fp", gen_buvid_fp(NetWorkUtil.USER_AGENT_WEB + System.currentTimeMillis(), 31));
        }

        // buvid3 & buvid4. Get from http API.
        if (!cookies.containsKey("buvid3") || !cookies.containsKey("buvid4")) {

        }
    }

    private static final String[] MP = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "10"
    };
    private static final int[] PCK = {8, 4, 4, 4, 12};
    private static final String CHARSET = "0123456789ABCDEF";

    private static String gen_b_lsid() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        String randomString = sb.toString();
        long currentTimeMillis = System.currentTimeMillis();
        return randomString + "_" + Long.toHexString(currentTimeMillis).toUpperCase();
    }

    @SuppressLint("DefaultLocale")
    private static String gen_uuid_infoc() {
        long t = System.currentTimeMillis() % 100000;
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int len : PCK) {
            for (int i = 0; i < len; i++) {
                sb.append(MP[random.nextInt(16)]);
            }
            sb.append("-");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(String.format("%05d", t)).append("infoc");
        return sb.toString();
    }

    private static String gen_buvid_fp(String key, int seed) {
        long m = murmur3_x64_128(key.getBytes(), seed);
        return String.format("%016x%016x", m & (MOD - 1), m >> 64);
    }

    private static final long MOD = 1L << 64;
    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;
    private static final long M = 0xc6a4a7935bd1e995L;
    private static final int R = 47;

    private static long murmur3_x64_128(byte[] key, int seed) {
        long h1 = seed;
        long h2 = seed;
        int len = key.length;
        int i = 0;

        while (len >= 16) {
            long k1 = getLong(key, i);
            i += 8;
            long k2 = getLong(key, i);
            i += 8;

            k1 *= C1;
            k1 = Long.rotateLeft(k1, R);
            k1 *= C2;
            h1 ^= k1;
            h1 = Long.rotateLeft(h1, 31);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;

            k2 *= C2;
            k2 = Long.rotateLeft(k2, R);
            k2 *= C1;
            h2 ^= k2;
            h2 = Long.rotateLeft(h2, 33);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;

            len -= 16;
        }

        if (len > 0) {
            if (len >= 8) {
                long k1 = getLong(key, i);
                k1 *= C1;
                k1 = Long.rotateLeft(k1, R);
                k1 *= C2;
                h1 ^= k1;
            }

            if (len >= 8) {
                long k2 = getLong(key, i + 8);
                k2 *= C2;
                k2 = Long.rotateLeft(k2, R);
                k2 *= C1;
                h2 ^= k2;
            }
        }

        h1 ^= key.length;
        h2 ^= key.length;

        h1 += h2;
        h2 += h1;

        h1 = fmix(h1);
        h2 = fmix(h2);

        h1 += h2;
        h2 += h1;

        return (h2 << 64) | h1;
    }

    private static long getLong(byte[] array, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(array, offset, 8);
        return buffer.getLong();
    }

    private static long fmix(long k) {
        k ^= (k >>> 33);
        k *= 0xff51afd7ed558ccdL;
        k ^= (k >>> 33);
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= (k >>> 33);
        return k;
    }
}
