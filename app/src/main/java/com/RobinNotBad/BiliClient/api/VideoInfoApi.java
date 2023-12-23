package com.RobinNotBad.BiliClient.api;

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
        Response response = NetWorkUtil.get(url,ConfInfoApi.defHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        return new JSONObject(result.getJSONObject("data").toString());
    }

    public static JSONObject getJsonByAid(long aid) throws IOException, JSONException {  //通过aid获取json
        String url = "https://api.bilibili.com/x/web-interface/view?aid=" + aid;
        Response response = NetWorkUtil.get(url,ConfInfoApi.defHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        return new JSONObject(result.getJSONObject("data").toString());
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        videoInfo.timeDesc = sdf.format(data.getLong("ctime") * 1000);
        Log.e("发布时间",String.valueOf(videoInfo.timeDesc));

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
        ArrayList<Integer> cids = new ArrayList<>();
        for (int i = 0; i < pages.length(); i++) {
            JSONObject page = pages.getJSONObject(i);
            String pagename = page.getString("part");
            pagenames.add(pagename);
            Log.e("第" + i + "个视频的标题",pagename);
            int cid = page.getInt("cid");
            cids.add(cid);
            Log.e("第" + i + "个视频的cid",String.valueOf(cid));
        }
        videoInfo.pagenames = pagenames;
        videoInfo.cids = cids;

        return videoInfo;
    }

}
