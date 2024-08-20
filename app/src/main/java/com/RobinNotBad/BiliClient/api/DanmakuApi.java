package com.RobinNotBad.BiliClient.api;

import java.io.IOException;
import org.json.JSONException;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import org.json.JSONObject;
import java.util.Objects;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import android.util.Log;

//弹幕api

public class DanmakuApi {
    
    //发送弹幕
    public static int sendVideoDanmakuByBvid(long cid, String msg, String bvid, long progress, int color, int mode) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/dm/post";
        String arg = "type=1&oid=" + cid + "&msg=" + msg + "&bvid=" + bvid + "&progress=" + progress + "&color=" + color + "&mode=" + mode + "&rnd="+ (System.currentTimeMillis() * 1000000) + "&csrf=" + SharedPreferencesUtil.getString("csrf", "");
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, arg, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-发送弹幕", result.toString());
        return result.getInt("code"); //https://socialsisteryi.github.io/bilibili-API-collect/docs/danmaku/action.html#%E5%8F%91%E9%80%81%E8%A7%86%E9%A2%91%E5%BC%B9%E5%B9%95
    }
    public static int sendVideoDanmakuByAid(long cid, String msg, long aid, long progress, int color, int mode) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/dm/post";
        String arg = "type=1&oid=" + cid + "&msg=" + msg + "&aid=" + aid + "&progress=" + progress + "&color=" + color + "&mode=" + mode + "&rnd="+ (System.currentTimeMillis() * 1000000) + "&csrf=" + SharedPreferencesUtil.getString("csrf", "");
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, arg, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-发送弹幕", result.toString());
        return result.getInt("code"); //https://socialsisteryi.github.io/bilibili-API-collect/docs/danmaku/action.html#%E5%8F%91%E9%80%81%E8%A7%86%E9%A2%91%E5%BC%B9%E5%B9%95
    }
    
    //点赞弹幕
    public static int likeDanmaku(long dmid, long cid, int op) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/dm/thumbup/add";
        String arg = "oid=" + cid + "&dmid=" + dmid + "&op=" + op + "&platform=web_player" + "&csrf=" + SharedPreferencesUtil.getString("csrf", "");
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, arg, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-点赞弹幕", result.toString());
        return result.getInt("code");
    }
        
    //撤回弹幕
    public static int recallDanmaku(long dmid, long cid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/dm/recall";
        //文档里就是cid，不是oid
        String arg = "cid=" + cid + "&dmid=" + dmid + "&csrf=" + SharedPreferencesUtil.getString("csrf", "");
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, arg, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-撤回弹幕", result.toString());
        return result.getInt("code");
    }
}
