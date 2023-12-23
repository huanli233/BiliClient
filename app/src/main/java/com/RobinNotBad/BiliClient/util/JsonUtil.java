package com.RobinNotBad.BiliClient.util;

import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONObject;

/*
2023-12-22 RobinNotBad
这玩意是可以用来快速拆解的，但也许并没有什么用
 */

public class JsonUtil {

    public static int searchInt(JSONObject input, String name, int defaultValue){
        String string = searchString(input, name, null);
        if(string==null)return defaultValue;
        return Integer.parseInt(string);
    }

    public static long searchLong(JSONObject input, String name, long defaultValue){
        String string = searchString(input, name, null);
        if(string==null)return defaultValue;
        return Long.parseLong(string);
    }

    public static double searchDouble(JSONObject input, String name, double defaultValue){
        String string = searchString(input, name, null);
        if(string==null)return defaultValue;
        return Double.parseDouble(string);
    }

    public static boolean searchBoolean(JSONObject input, String name, boolean defaultValue){
        String string = searchString(input, name, null);
        if(string==null)return defaultValue;
        return Boolean.parseBoolean(string);
    }

    public static String searchString(JSONObject input, String name, String defaultValue){
        if(name==null || name.isEmpty() || input==null) return defaultValue;

        String searchKey = "\"" + name + "\":";
        String json = input.toString();
        int index = json.indexOf(searchKey);   // "name":
        if(index==-1) return defaultValue;

        int count = 0;
        int i = index + searchKey.length();
        for (int j = i; j < json.length(); j++) {
            Log.e("index", String.valueOf(json.charAt(j)));

            if(json.charAt(j)=='{') count++;
            if(json.charAt(j)=='}') count--;
            if((json.charAt(j+1)==',' || json.charAt(j+1)=='}') && count==0){
                if(json.charAt(i)=='\"'){
                    return LittleToolsUtil.unEscape(json.substring(i+1,j));
                }
                else return json.substring(i,j+1);
            }
        }
        return defaultValue;
    }
}
