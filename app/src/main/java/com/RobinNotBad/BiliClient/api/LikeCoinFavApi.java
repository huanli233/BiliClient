package com.RobinNotBad.BiliClient.api;

import android.util.Log;

import com.RobinNotBad.BiliClient.model.ApiResult;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class LikeCoinFavApi {


    public static int like(long aid, int likeState) throws IOException, JSONException {  //likeState 1点赞0取消
        String url = "https://api.bilibili.com/x/web-interface/archive/like";
        String per = "aid=" + aid + "&like=" + likeState + "&csrf=" + SharedPreferencesUtil.getString("csrf", "");

        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, per, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-点赞", result.toString());
        return result.getInt("code");
    }

    public static int coin(long aid, int multiply) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/coin/add";
        String per = "aid=" + aid + "&multiply=" + multiply + "&csrf=" + SharedPreferencesUtil.getString("csrf", "");

        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, per, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-投币", result.toString());
        return result.getInt("code");
    }

    public static int favorite(long aid, long fid) throws IOException, JSONException {
        String strMid = String.valueOf(SharedPreferencesUtil.getLong("mid", 0));
        String addFid = fid + strMid.substring(strMid.length() - 2);
        String url = "https://api.bilibili.com/medialist/gateway/coll/resource/deal";
        String per = "rid=" + aid + "&type=2&add_media_ids=" + addFid + "&del_media_ids=&csrf=" + SharedPreferencesUtil.getString("csrf", "");

        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, per, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-添加收藏", result.toString());
        return result.getInt("code");
    }

    public static ApiResult getVideoStats(VideoInfo videoInfo) {
        ApiResult apiResult = new ApiResult();
        try {
            String url = "https://api.bilibili.com/x/web-interface/archive/relation?aid=" + videoInfo.aid;
            JSONObject result = NetWorkUtil.getJson(url);
            apiResult.fromJson(result);
            JSONObject data = result.getJSONObject("data");
            videoInfo.stats.followed = data.optBoolean("attention");
            videoInfo.stats.liked = data.optBoolean("like");
            videoInfo.stats.disliked = data.optBoolean("dislik");
            videoInfo.stats.favoured = data.optBoolean("favorite");
            videoInfo.stats.coined = data.optInt("coin");
        } catch (Exception e){
            MsgUtil.err(apiResult.message, e);
        }
        return apiResult;
    }

    public static boolean getLiked(long aid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/archive/has/like?aid=" + aid;
        JSONObject result = NetWorkUtil.getJson(url);
        return (result.getInt("code") == 0 && result.getInt("data") == 1);
    }

    public static int getCoined(long aid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/archive/coins?aid=" + aid;
        JSONObject result = NetWorkUtil.getJson(url);
        return (result.getInt("code") == 0 ? result.getJSONObject("data").getInt("multiply") : 0);
    }

    public static boolean getFavoured(long aid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/fav/video/favoured?aid=" + aid;
        JSONObject result = NetWorkUtil.getJson(url);
        return (result.getInt("code") == 0 && result.getJSONObject("data").getBoolean("favoured"));
    }

}
