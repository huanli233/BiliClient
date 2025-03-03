package com.RobinNotBad.BiliClient.util;

/*
 * 未完工的log
 */


import android.util.Log;

public class Logu {
    public static boolean LOGV_ENABLED = false;
    public static boolean LOGD_ENABLED = false;
    public static boolean LOGI_ENABLED = false;

    public static void v(String s){
        if(!LOGV_ENABLED) return;
        Log.v(getCaller(), s);
    }
    public static void i(String s){
        if(!LOGI_ENABLED) return;
        Log.i(getCaller(), s);
    }
    public static void d(String s){
        if(!LOGD_ENABLED) return;
        Log.d(getCaller(), s);
    }
    public static void w(String s){
        Log.w(getCaller(), s);
    }
    public static void e(String s){
        Log.e(getCaller(), s);
    }
    public static void wtf(String s){
        Log.wtf(getCaller(), s);
    }


    public static void v(String tag, String info){
        if(!LOGV_ENABLED) return;
        Log.v(getCaller(), tag + ">" + info);
    }
    public static void i(String tag, String info){
        if(!LOGI_ENABLED) return;
        Log.i(getCaller(), tag + ">" + info);
    }
    public static void d(String tag, String info){
        if(!LOGD_ENABLED) return;
        Log.d(getCaller(), tag + ">" + info);
    }
    public static void w(String tag, String info){
        Log.w(getCaller(), tag + ">" + info);
    }
    public static void e(String tag, String info){
        Log.e(getCaller(), tag + ">" + info);
    }
    public static void wtf(String tag, String info){
        Log.wtf(getCaller(), tag + ">" + info);
    }

    private static String getCaller(){
        StackTraceElement caller = Thread.currentThread().getStackTrace()[4];
        String name = caller.getClassName();
        int index = name.length();
        for (; index > 1; index--) {
            if(name.charAt(index-1) == '.') break;
        }
        return name.substring(index) + ">" + caller.getMethodName();
    }
}
