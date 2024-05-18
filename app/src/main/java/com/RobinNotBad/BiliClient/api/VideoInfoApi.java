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
import java.lang.reflect.Array;
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

        JSONObject rights = data.getJSONObject("rights");
        videoInfo.isCooperation = (rights.getInt("is_cooperation") == 1 ? true : false);
        videoInfo.isSteinGate = (rights.getInt("is_stein_gate") == 1 ? true : false);
        videoInfo.is360 = (rights.getInt("is_360") == 1 ? true : false);

        ArrayList<UserInfo> staff_list = new ArrayList<>();
        if(videoInfo.isCooperation) { //如果是联合投稿就存储联合UP列表
            JSONArray staff = data.getJSONArray("staff");
            for( int i = 0;i < staff.length();i++){
                UserInfo staff_member = new UserInfo();
                JSONObject staff_info = staff.getJSONObject(i);

                staff_member.mid = staff_info.getLong("mid");
                staff_member.sign = staff_info.getString("title"); //卡片的简介用来显示参与的事物
                staff_member.name = staff_info.getString("name");
                staff_member.avatar = staff_info.getString("face");
                staff_member.fans = staff_info.getInt("follower");
                staff_member.level = 6;
                staff_member.followed = false;
                staff_member.notice = "";
                staff_member.official = staff_info.getJSONObject("official").getInt("role");
                staff_member.officialDesc = staff_info.getJSONObject("official").getString("title");

                staff_list.add(staff_member);
            }
        } else {
            JSONObject owner = data.getJSONObject("owner");
            UserInfo userInfo = new UserInfo();
            userInfo.name = owner.getString("name");
            userInfo.avatar = owner.getString("face");
            userInfo.mid = owner.getLong("mid");
            userInfo.sign = "UP主";
            staff_list.add(userInfo);
        }
        videoInfo.staff = staff_list;

        videoInfo.argueMsg = data.getJSONObject("argue_info").getString("argue_msg");

        try {
            if((!data.getString("redirect_url").isEmpty()) && (data.getString("redirect_url").contains("bangumi"))) videoInfo.epid = Long.parseLong(data.getString("redirect_url").replace("https://www.bilibili.com/bangumi/play/ep",""));
            else videoInfo.epid = -1;
        } catch (Exception e){
            e.printStackTrace();
            videoInfo.epid = -1;
        }

        return videoInfo;
    }

}
