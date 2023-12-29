package com.RobinNotBad.BiliClient.util;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

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

    //json列项函数 用于表情包，自己写的以替换luern的库
    public static ArrayList<String> getJsonKeys(JSONObject jsonObject){
        ArrayList<String> list = new ArrayList<>();
        String str = jsonObject.toString();


        while (str.contains("\":")){    //先看有没有项，找到项的结束符 ":
            int end_index = str.indexOf("\":");
            int i = end_index-1;
            while (str.charAt(i)!='\"' && i>1) i--;    //找到后，一直往后退格，直到找到 "
            String key = str.substring(i+1,end_index);    //截取，添加到列表
            list.add(key);

            //还没结束喵！
            if(str.charAt(end_index+2) == '{'){    //如果该项是jsonObject
                int count=1;
                for (int j = end_index+3; j < str.length(); j++) {  //找到这个jsonObject的结束位置，然后把它截掉，防止里面的项干扰下一次寻找
                    if(str.charAt(j)=='{') count++;
                    else if (str.charAt(j)=='}') count--;
                    if(count==0) {
                        str = str.substring(j+1);
                        break;
                    }
                }
            } else str = str.substring(end_index+2);  //如果不是，直接把 "项名:" 截了即可
        }
        return list;
    }
}
