package com.RobinNotBad.BiliClient.util;

import android.util.Pair;

public class StringUtil {
    public static Pair<Integer, Integer> appendString(StringBuilder stringBuilder, String str) {
        int startIndex = stringBuilder.length();
        stringBuilder.append(str);
        int endIndex = stringBuilder.length();
        return new Pair<>(startIndex, endIndex);
    }
}
