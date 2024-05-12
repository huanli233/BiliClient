package com.RobinNotBad.BiliClient.api;

import android.util.Log;

import com.RobinNotBad.BiliClient.model.Bangumi;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.ResponseBody;

public class BangumiApi {

    //获取番剧信息, 详情页需要有基本的cover, 信息等
    public static Bangumi getBangumi(long mediaId) throws JSONException,IOException {
        Bangumi bangumi = new Bangumi();
        bangumi.info = getInfo(mediaId);
        bangumi.sectionList = getSections(bangumi.info.season_id);
        return bangumi;
    }


    public static Long getMdidFromEpid(long epid) {
        try {
            String url = "https://api.bilibili.com/pgc/view/web/season?ep_id=" + epid;
            JSONObject all = NetWorkUtil.getJson(url);

            Log.e("debug-epid",String.valueOf(epid));

            int code = all.getInt("code");
            if (code != 0) return 0L;

            return all.getJSONObject("result").getLong("media_id");
        }catch (Exception e){
            e.printStackTrace();
            return 0L;
        }
    }

    public static Bangumi.Info getInfo(long mediaId) throws IOException, JSONException {
        String url = "https://api.bilibili.com/pgc/review/user?media_id=" + mediaId;
        JSONObject all = NetWorkUtil.getJson(url);

        int code = all.getInt("code");
        if (code != 0) {throw new JSONException("错误码：" + code);}

        JSONObject media = all.getJSONObject("result").getJSONObject("media");

        Bangumi.Info info = new Bangumi.Info();
        info.media_id = media.getLong("media_id");
        info.season_id = media.getLong("season_id");
        info.title = media.getString("title");
        info.cover = media.getString("cover");
        info.cover_horizontal = media.getString("horizontal_picture");
        info.type = media.getInt("type");
        info.type_name = media.getString("type_name");

        JSONObject rating = media.getJSONObject("rating");
        info.count = rating.getInt("count");
        info.score = (float) rating.getDouble("score");

        JSONArray areas = media.getJSONArray("areas");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < areas.length(); i++) {
            stringBuilder.append(areas.getJSONObject(i).getString("name"));
            stringBuilder.append("|");
        }
        stringBuilder.substring(0,stringBuilder.length()-1);
        info.area_name = stringBuilder.toString();

        return info;
    }

    public static ArrayList<Bangumi.Section> getSections(long season_id) throws IOException, JSONException {
        String url = "https://api.bilibili.com/pgc/web/season/section?season_id=" + season_id;
        JSONObject all = new JSONObject(Objects.requireNonNull(Objects.requireNonNull(NetWorkUtil.get(url)).body()).string());  //得到一整个json
        JSONObject result = all.getJSONObject("result");
        ArrayList<Bangumi.Section> sectionList = new ArrayList<>();

        sectionList.add(analyzeSection(result.getJSONObject("main_section")));

        JSONArray other_sections = result.getJSONArray("section");
        for (int i = 0; i < other_sections.length(); i++) {
            sectionList.add(analyzeSection(other_sections.getJSONObject(i)));
        }

        return sectionList;
    }

    public static Bangumi.Section analyzeSection(JSONObject jsonObject) throws JSONException {
        Bangumi.Section section = new Bangumi.Section();
        section.id = jsonObject.getLong("id");
        section.title = jsonObject.getString("title");
        section.type = jsonObject.getInt("type");

        JSONArray episodes = jsonObject.getJSONArray("episodes");
        ArrayList<Bangumi.Episode> episodeList = new ArrayList<>();
        for (int i = 0; i < episodes.length(); i++) {
            episodeList.add(analyzeEpisode(episodes.getJSONObject(i)));
        }
        section.episodeList = episodeList;

        return section;
    }

    public static Bangumi.Episode analyzeEpisode(JSONObject jsonObject) throws JSONException {
        Bangumi.Episode episode = new Bangumi.Episode();
        episode.id = jsonObject.getLong("id");
        episode.aid = jsonObject.getLong("aid");
        episode.cid = jsonObject.getLong("cid");
        episode.cover = jsonObject.getString("cover");
        episode.badge = jsonObject.getString("badge");
        episode.title = jsonObject.getString("title");
        episode.title_long = jsonObject.getString("long_title");

        return episode;
    }
}

