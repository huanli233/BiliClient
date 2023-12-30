package com.RobinNotBad.BiliClient.api;

import java.util.HashMap;

public class BilibiliIDConverter {
    private static final String table = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF";
    private static final int[] s = {11, 10, 3, 8, 4, 6};
    private static final int xor = 177451812;
    private static final long add = 8728348608L;

    private static final HashMap<Character, Integer> tr = new HashMap<>();

    static {
        for (int i = 0; i < 58; i++) {
            tr.put(table.charAt(i), i);
        }
    }
    public static Long bvtoaid(String bv) {
        long x = 0;
        for (int i = 0; i < 6; i++) {
            x += tr.get(bv.charAt(s[i])) * Math.pow(58, i);
        }
        x = (x - add) ^ xor;
        return x;
    }
    public static String aidtobv(Long aid) {
        long x = aid;
        x = (x ^ xor) + add;
        StringBuilder r = new StringBuilder("BV1  4 1 7  ");
        for (int i = 0; i < 6; i++) {
            r.setCharAt(s[i], table.charAt((int) (x / Math.pow(58, i) % 58)));
        }
        return r.toString();
    }
}