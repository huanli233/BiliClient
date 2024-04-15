package com.RobinNotBad.BiliClient.util;


public class GlideUtil {
    public static int QUALITY_HIGH = 80;
    public static int QUALITY_LOW = 15;

    public static String url(String url){
        if(url.endsWith("gif")) return url;
        if(url.endsWith("webp")) return url;
        return url + "@" + QUALITY_LOW + "q.webp";
    }

    public static String url_hq(String url){
        if(url.endsWith("gif")) return url;
        if(url.endsWith("webp")) return url;
        return url + "@" + QUALITY_HIGH + "q.webp";
    }
}
