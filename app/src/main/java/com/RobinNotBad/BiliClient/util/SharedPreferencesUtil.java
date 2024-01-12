package com.RobinNotBad.BiliClient.util;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    private static SharedPreferences sharedPreferences;

    public static void initSharedPrefs(Context context){
        //实际上在BaseActivity里已经帮你init过了，通常无需再调用此函数
        if(sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("default", Context.MODE_PRIVATE);
        }
    }

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
    public static void saveCookiesFromResponse(Response response){
        CenterThreadPool.run(() -> {
            List<String> cookiesList = response.headers("Set-Cookie");
            //如果没有新cookies，直接返回
            if (cookiesList.isEmpty()) return;
            //将新cookies转换为键值对map
            StringBuilder cookies = new StringBuilder();
            for (String s : cookiesList) cookies.append(s.split("; ")[0]).append("; ");
            Map<String, String> newCookiesPair = createCookiePairs(cookies.toString());

            //将旧cookies转换为键值对map
            Map<String, String> oldCookiesPair = createCookiePairs(
                    getString(SharedPreferencesUtil.cookies, "")
            );
            //更新值
            oldCookiesPair.putAll(newCookiesPair);
            //保存
            StringBuilder realCookies = new StringBuilder();
            oldCookiesPair.forEach((k, v) -> {
                realCookies.append(k).append("=").append(v).append("; ");
            });
            putString(SharedPreferencesUtil.cookies, realCookies.substring(0, realCookies.length() - 2));
        });
    }
    private static Map<String, String>createCookiePairs(String cookies){
        Map<String, String> cookiePairs = new HashMap<>();
        //cookies为空直接返回
        if(cookies == null || cookies.trim().isEmpty()) return cookiePairs;
        //分割成key=value形式
        String[] split = cookies.split("; ");
        for (String s : split) {
            //去除空串
            if(s.trim().isEmpty()) continue;
            String[] split1 = s.split("=");
            int length = split1.length;
            //如果形式为key=value,那么长度一定是2， 否则就是无效cookie, 忽略
            if(length == 2){
                String key = split1[0];
                String value = split1[1];
                cookiePairs.put(key, value);
            }
        }
        return cookiePairs;
    }
}
