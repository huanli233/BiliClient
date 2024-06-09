package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;

import com.RobinNotBad.BiliClient.model.At;
import com.RobinNotBad.BiliClient.model.Collection;
import com.RobinNotBad.BiliClient.model.Stats;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


//视频信息API 自己写的


public class VideoInfoApi {
    public static JSONObject getJsonByBvid(String bvid) throws IOException, JSONException {  //通过bvid获取json
        String url = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
        JSONObject result = NetWorkUtil.getJson(url);
        return result.has("data") ? result.getJSONObject("data") : null;
    }

    public static JSONObject getJsonByAid(long aid) throws IOException, JSONException {  //通过aid获取json
        String url = "https://api.bilibili.com/x/web-interface/view?aid=" + aid;
        JSONObject result = NetWorkUtil.getJson(url);
        return result.has("data") ? result.getJSONObject("data") : null;
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

    public static Collection analyzeUgcSeason(JSONObject json) throws JSONException {
        Collection collection = new Collection();
        collection.id = json.optInt("id", -1);
        collection.title = json.optString("title");
        collection.intro = json.optString("intro");
        collection.cover = json.optString("cover");
        collection.mid = json.optLong("mid");
        JSONArray sections = json.optJSONArray("sections");
        if (sections != null) {
            List<Collection.Section> sectionList = new ArrayList<>();
            for (int i = 0; i < sections.length(); i++) {
                JSONObject sectionJson = sections.getJSONObject(i);
                Collection.Section section = new Collection.Section();
                section.season_id = sectionJson.optInt("season_id", -1);
                section.id = sectionJson.optInt("id", -1);
                section.title = sectionJson.optString("title");
                JSONArray episodes = sectionJson.optJSONArray("episodes");
                if (episodes != null) {
                    List<Collection.Episode> episodeList = new ArrayList<>();
                    for (int j = 0; j < episodes.length(); j++) {
                        JSONObject episodeJson = episodes.getJSONObject(j);
                        Collection.Episode episode = new Collection.Episode();
                        episode.season_id = episodeJson.optInt("season_id", -1);
                        episode.section_id = episodeJson.optInt("section_id", -1);
                        episode.id = episodeJson.optInt("id", -1);
                        episode.aid = episodeJson.optLong("aid", -1);
                        episode.cid = episodeJson.optLong("cid", -1);
                        episode.title = episodeJson.optString("title");
                        JSONObject arc = episodeJson.optJSONObject("arc");
                        if (arc != null) {
                            episode.arc = getInfoByJson(arc);
                        }
                        episode.bvid = episodeJson.optString("bvid");
                        episodeList.add(episode);
                    }
                    section.episodes = episodeList;
                }
                sectionList.add(section);
            }
            collection.sections = sectionList;
        }
        return collection;
    }
    
    public static VideoInfo getInfoByJson(JSONObject data) throws JSONException {  //项目实在太多qwq 拆就完事了
        VideoInfo videoInfo = new VideoInfo();
        Log.e("视频信息","--------");
        videoInfo.title = data.getString("title");
        Log.e("标题",videoInfo.title);
        videoInfo.cover = data.getString("pic");
        Log.e("封面",videoInfo.cover);
        if (data.has("desc_v2") && !data.isNull("desc_v2")) {
            StringBuilder sb = new StringBuilder();
            JSONArray descArray = data.getJSONArray("desc_v2");
            ArrayList<At> ats = new ArrayList<>();
            for (int i = 0; i < descArray.length(); i++) {
                JSONObject curObj = descArray.getJSONObject(i);
                int type = curObj.getInt("type");
                switch (type) {
                    case 2:
                        Pair<Integer, Integer> indexs = StringUtil.appendString(sb, "@" + curObj.getString("raw_text"));
                        ats.add(new At(curObj.getLong("biz_id"), indexs.first, indexs.second));
                        break;
                    default:
                        sb.append(curObj.getString("raw_text"));
                        break;
                }
            }
            videoInfo.description = sb.toString();
            videoInfo.descAts = ats;
        } else {
            videoInfo.description = data.getString("desc");
        }
        Log.e("简介",videoInfo.description);

        videoInfo.bvid = data.optString("bvid");
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
        stats.favorite = stat.optInt("favorite", -1);
        videoInfo.stats = stats;

        JSONArray pages = data.optJSONArray("pages");
        if (pages != null) {
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
        }

        videoInfo.upowerExclusive = data.optBoolean("is_upower_exclusive", true);

        JSONObject rights = data.optJSONObject("rights");
        if (rights != null) {
            videoInfo.isCooperation = (rights.optInt("is_cooperation", 0) == 1);
            videoInfo.isSteinGate = (rights.optInt("is_stein_gate", 0) == 1);
            videoInfo.is360 = (rights.optInt("is_360", 0) == 1);
        }

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
            if (data.optJSONObject("owner") != null) {
                JSONObject owner = data.getJSONObject("owner");
                UserInfo userInfo = new UserInfo();
                userInfo.name = owner.getString("name");
                userInfo.avatar = owner.getString("face");
                userInfo.mid = owner.getLong("mid");
                userInfo.sign = "UP主";
                staff_list.add(userInfo);
            }
        }
        videoInfo.staff = staff_list;

        if (data.optJSONObject("argue_info") != null) {
            videoInfo.argueMsg = data.getJSONObject("argue_info").getString("argue_msg");
        }

        try {
            if (data.has("redirect_url") && (!data.getString("redirect_url").isEmpty()) && (data.getString("redirect_url").contains("bangumi"))) videoInfo.epid = Long.parseLong(data.getString("redirect_url").replace("https://www.bilibili.com/bangumi/play/ep",""));
            else videoInfo.epid = -1;
        } catch (Exception e){
            e.printStackTrace();
            videoInfo.epid = -1;
        }

        if (data.has("copyright") && !data.isNull("copyright")) videoInfo.copyright = data.getInt("copyright");

        JSONObject ugc_season = data.optJSONObject("ugc_season");
        if (ugc_season != null) {
            videoInfo.collection = analyzeUgcSeason(ugc_season);
        }

        return videoInfo;
    }

}
