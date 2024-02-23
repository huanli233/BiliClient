package com.RobinNotBad.BiliClient.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.RobinNotBad.BiliClient.BiliTerminal;

/**
 * 被 luern0313 创建于 2020/5/4.
 * #以下代码来源于腕上哔哩的开源项目，有修改。感谢开源者做出的贡献！
 */
public class SharedPreferencesUtil
{
    public static String cookies = "cookies";
    public static String mid = "mid";
    public static String csrf = "csrf";
    public static String access_key = "access_key";
    public static String refresh_token = "refresh_token";
    public static String setup = "setup";
    public static String last_version = "last_version";
    public static String player = "player";
    public static String padding_horizontal = "padding_horizontal";
    public static String padding_vertical = "padding_vertical";
    public static String cookie_refresh = "cookie_refresh";


    private static final SharedPreferences sharedPreferences = BiliTerminal.context.getSharedPreferences("default",Context.MODE_PRIVATE);

    public static String getString(String key, String def) {
        return sharedPreferences.getString(key, def);
    }

    public static void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static int getInt(String key, int def)
    {
        return sharedPreferences.getInt(key, def);
    }

    public static void putInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public static long getLong(String key, long def)
    {
        return sharedPreferences.getLong(key, def);
    }

    public static void putLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean def)
    {
        return sharedPreferences.getBoolean(key, def);
    }

    public static void putBoolean(String key, boolean value)
    {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static void putFloat(String key, float value) {
        sharedPreferences.edit().putFloat(key, value).apply();
    }

    public static float getFloat(String key, float def)
    {
        return sharedPreferences.getFloat(key, def);
    }

    public static void removeValue(String key) {
        sharedPreferences.edit().remove(key).apply();
    }


}
