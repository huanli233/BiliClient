package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.util.Log;

import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Response;


//视频信息API 自己写的


public class VideoInfoApi {
    public static JSONObject getJsonByBvid(String bvid) throws IOException, JSONException {  //通过bvid获取json
        String url = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        return result.getJSONObject("data");
    }

    public static JSONObject getJsonByAid(long aid) throws IOException, JSONException {  //通过aid获取json
        String url = "https://api.bilibili.com/x/web-interface/view?aid=" + aid;
        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        return result.getJSONObject("data");
    }

    
    public static JSONArray getTagsByBvid(String bvid) throws IOException, JSONException {  //通过bvid获取tag
        String url = "https://api.bilibili.com/x/tag/archive/tags?bvid=" + bvid;
        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        return result.getJSONArray("data");
    }

    public static JSONArray getTagsByAid(long aid) throws IOException, JSONException {  //通过aid获取tag
        String url = "https://api.bilibili.com/x/tag/archive/tags?aid=" + aid;
        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        return result.getJSONArray("data");
    }
    
    public static VideoInfo getInfoByJson(JSONObject data,JSONArray tagJson) throws JSONException {  //项目实在太多qwq 拆就完事了
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
        videoInfo.timeDesc = sdf.format(data.getLong("ctime") * 1000);
        Log.e("发布时间",String.valueOf(videoInfo.timeDesc));

        int duration = data.getInt("duration");
        int min = duration / 60;
        int sec = duration % 60;
        videoInfo.duration = (min<10 ? "0" : "") + min + ":" + (sec<10 ? "0" : "") + sec;
        Log.e("视频时长",videoInfo.duration);
        
        JSONObject owner = data.getJSONObject("owner");
        videoInfo.upName = owner.getString("name");
        Log.e("UP主",videoInfo.upName);
        videoInfo.upAvatar = owner.getString("face");
        Log.e("UP主头像",videoInfo.upAvatar);
        videoInfo.upMid = owner.getLong("mid");
        Log.e("mid",String.valueOf(videoInfo.upMid));

        JSONObject stat = data.getJSONObject("stat");
        videoInfo.view = stat.getInt("view");
        Log.e("观看数",String.valueOf(videoInfo.view));
        videoInfo.like = stat.getInt("like");
        Log.e("点赞数",String.valueOf(videoInfo.like));
        videoInfo.coin = stat.getInt("coin");
        Log.e("硬币数",String.valueOf(videoInfo.coin));
        videoInfo.reply = stat.getInt("reply");
        Log.e("回复数",String.valueOf(videoInfo.reply));
        videoInfo.danmaku = stat.getInt("danmaku");
        Log.e("弹幕数",String.valueOf(videoInfo.danmaku));
        videoInfo.favorite = stat.getInt("favorite");
        Log.e("收藏数",String.valueOf(videoInfo.favorite));

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
        
        StringBuilder tags = new StringBuilder();
        for (int i = 0;i<tagJson.length();i++){
            if(i>0) tags.append("/ ");
            tags.append(((JSONObject) tagJson.get(i)).getString("tag_name"));
        }
        videoInfo.tagsDesc = tags.toString();

        return videoInfo;
    }

    public static JSONObject getAiSummary(long aid,int cid,long mid) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/web-interface/view/conclusion/get?";
        String args = "aid=" + aid + "&cid=" + cid + "&up_mid=" + mid;
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url + ConfInfoApi.signWBI(args), ConfInfoApi.webHeaders).body()).string());

        if(all.getInt("code") == 0) return all.getJSONObject("data");
        return new JSONObject();
    }
    public static JSONObject getAiSummary(String bvid,int cid,long mid) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/web-interface/view/conclusion/get?";
        String args = "bvid=" + bvid + "&cid=" + cid + "&up_mid=" + mid;
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url + ConfInfoApi.signWBI(args), ConfInfoApi.webHeaders).body()).string());

        if(all.getInt("code") == 0) return all.getJSONObject("data");
        return new JSONObject();
    }
}
