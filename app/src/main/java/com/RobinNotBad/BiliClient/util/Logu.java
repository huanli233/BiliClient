package com.RobinNotBad.BiliClient.util;

/*
 * 未完工的log
 */


import android.util.Log;

public class Logu {
    public static void v(String s){
        Log.v(getCaller(), s);
    }
    public static void i(String s){
        Log.i(getCaller(), s);
    }
    public static void d(String s){
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
