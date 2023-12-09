package com.RobinNotBad.BiliClient.util;

import android.util.Log;

import org.json.JSONObject;

public class JsonUtil {
    public static String searchString(String name, JSONObject input){
        Log.e("尝试拆解","开始");
        if(name==null || name.isEmpty() || input==null) return null;
        Log.e("尝试拆解","非空");
        String searchKey = "\"" + name + "\":";
        String json = input.toString();
        int index = json.indexOf(searchKey);   // "name":
        if(index==-1) return null;

        Log.e("index",String.valueOf(index));

        int count = 0;
        int i = index + searchKey.length() + 1;
        for (int j = i; j < json.length(); j++) {
            Log.e("index", String.valueOf(json.charAt(j)));

            if(json.charAt(j)=='{') count++;
            if(json.charAt(j)=='}') count--;
            if((json.charAt(j+1)==',' || json.charAt(j+1)=='}') && count==0){
                if(json.charAt(i)=='\"')return json.substring(i+1,j-1);
                else return json.substring(i,j);
            }
        }
        return null;
    }
}
