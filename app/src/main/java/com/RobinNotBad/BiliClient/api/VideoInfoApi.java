package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.util.Log;

import com.RobinNotBad.BiliClient.model.Stats;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


//视频信息API 自己写的


public class VideoInfoApi {
    public static JSONObject getJsonByBvid(String bvid) throws IOException, JSONException {  //通过bvid获取json
        String url = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
        JSONObject result = NetWorkUtil.getJson(url);
        return result.getJSONObject("data");
    }

    public static JSONObject getJsonByAid(long aid) throws IOException, JSONException {  //通过aid获取json
        String url = "https://api.bilibili.com/x/web-interface/view?aid=" + aid;
        JSONObject result = NetWorkUtil.getJson(url);
        return result.getJSONObject("data");
    }

    
    public static String getTagsByBvid(String bvid) throws IOException, JSONException {  //通过bvid获取tag
        String url = "https://api.bilibili.com/x/tag/archive/tags?bvid=" + bvid;
        JSONObject result = NetWorkUtil.getJson(url);
        return analyzeTags(result.getJSONArray("data"));
    }

    public static String getTagsByAid(long aid) throws IOException, JSONException {  //通过aid获取tag
        String url = "https://api.bilibili.com/x/tag/archive/tags?aid=" + aid;
        JSONObject result = NetWorkUtil.getJson(url);
        return analyzeTags(result.getJSONArray("data"));
    }

    public static String analyzeTags(JSONArray tagJson) throws JSONException {
        StringBuilder tags = new StringBuilder();
        for (int i = 0;i<tagJson.length();i++){
            if(i>0) tags.append("/");
            tags.append(((JSONObject) tagJson.get(i)).getString("tag_name"));
        }
        return tags.toString();
    }
    
    public static VideoInfo getInfoByJson(JSONObject data) throws JSONException {  //项目实在太多qwq 拆就完事了
        VideoInfo videoInfo = new VideoInfo();
        Log.e("视频信息","--------");
        videoInfo.title = data.getString("title");
        Log.e("标题",videoInfo.title);
        videoInfo.cover = data.getString("pic");
        Log.e("封面",videoInfo.cover);
        videoInfo.description = data.getString("desc");
        Log.e("简介",videoInfo.description);

        videoInfo.bvid = data.getString("bvid");
        videoInfo.aid = data.getLong("aid");

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        videoInfo.timeDesc = sdf.format(data.getLong("pubdate") * 1000);
        Log.e("发布时间",String.valueOf(videoInfo.timeDesc));

        int duration = data.getInt("duration");
        int min = duration / 60;
        int sec = duration % 60;
        videoInfo.duration = (min<10 ? "0" : "") + min + ":" + (sec<10 ? "0" : "") + sec;
        Log.e("视频时长",videoInfo.duration);
        
        JSONObject owner = data.getJSONObject("owner");
        UserInfo userInfo = new UserInfo();
        userInfo.name = owner.getString("name");
        userInfo.avatar = owner.getString("face");
        userInfo.mid = owner.getLong("mid");
        videoInfo.upInfo = userInfo;

        JSONObject stat = data.getJSONObject("stat");
        Stats stats = new Stats();
        stats.view = stat.getInt("view");
        stats.like = stat.getInt("like");
        stats.coin = stat.getInt("coin");
        stats.reply = stat.getInt("reply");
        stats.danmaku = stat.getInt("danmaku");
        stats.favorite = stat.getInt("favorite");
        videoInfo.stats = stats;

        JSONArray pages = data.getJSONArray("pages");
        ArrayList<String> pagenames = new ArrayList<>();
        ArrayList<Long> cids = new ArrayList<>();
        for (int i = 0; i < pages.length(); i++) {
            JSONObject page = pages.getJSONObject(i);
            String pagename = page.getString("part");
            pagenames.add(pagename);
            Log.e("第" + i + "个视频的标题",pagename);
            long cid = page.getLong("cid");
            cids.add(cid);
            Log.e("第" + i + "个视频的cid",String.valueOf(cid));
        }
        videoInfo.pagenames = pagenames;
        videoInfo.cids = cids;

        videoInfo.upowerExclusive = data.getBoolean("is_upower_exclusive");

        return videoInfo;
    }

}
