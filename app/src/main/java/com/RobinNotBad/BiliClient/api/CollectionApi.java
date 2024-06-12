package com.RobinNotBad.BiliClient.api;

import android.util.Pair;

import com.RobinNotBad.BiliClient.model.Collection;
import com.RobinNotBad.BiliClient.model.Page;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollectionApi {
    /**
     * 获取合集信息
     * @param mid mid（或许可以为任意）
     * @param season_id 合集id
     * @param page 页数
     * @return Collection对象与分页信息
     */
    public static Pair<Collection, Page> getSeasonInfo(long mid, int season_id, int page) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/polymer/web-space/seasons_archives_list" + new NetWorkUtil.FormData()
                .setUrlParam(true)
                .put("mid", mid)
                .put("season_id", season_id)
                .put("page_num", page)
                .put("page_size", 6);
        JSONObject result = NetWorkUtil.getJson(url);
        Page pageInfo = new Page();
        JSONObject data = result.optJSONObject("data");
        Collection collection = new Collection();
        if (data != null) {
            JSONObject pageJson = data.optJSONObject("page");
            if (pageJson != null) {
                pageInfo.page_num = pageJson.optInt("page_num", -1);
                pageInfo.page_size = pageJson.optInt("page_size", -1);
                pageInfo.total = pageJson.optInt("total", -1);
            }
            JSONObject meta = data.optJSONObject("meta");
            if (meta != null) {
                collection.title = meta.optString("name");
                collection.id = meta.optInt("season_id", -1);
                collection.cover = meta.optString("cover");
                collection.intro = meta.optString("description");
            }
            JSONArray archives = data.optJSONArray("archives");
            if (archives != null) {
                List<VideoCard> videoCards = new ArrayList<>();
                for (int i = 0; i < archives.length(); i++) {
                    JSONObject archive = archives.getJSONObject(i);
                    VideoCard videoCard = new VideoCard();
                    videoCard.aid = archive.optLong("aid", -1);
                    videoCard.bvid = archive.optString("bvid");
                    videoCard.cover = archive.optString("pic");
                    videoCard.view = ToolsUtil.toWan(archive.getJSONObject("stat").optInt("view", -1));
                    videoCard.title = archive.optString("title");
                    videoCards.add(videoCard);
                }
                collection.cards = videoCards;
            }
        }
        return new Pair<>(collection, pageInfo);
    }
    
    public static Collection getSeasonByJson(JSONObject data) throws JSONException{
        Collection season = new Collection();
        JSONObject meta = data.getJSONObject("meta");
        
        if(meta.has("season_id")) season.id = meta.getInt("season_id");
        else season.id = meta.getInt("series_id");
        season.title = meta.getString("name");
        season.cover = meta.getString("cover");
        season.mid = meta.getLong("mid");
        season.intro = meta.getString("description");
                        
        season.cards = new ArrayList<>();
        
        long view = 0;
        JSONArray archives = data.getJSONArray("archives");
        for(int i = 0;i < archives.length();i++) {
            JSONObject card = archives.getJSONObject(i);
            String cover = card.getString("pic");
            long play = card.getJSONObject("stat").getLong("view");
            view += play;
            String playStr = ToolsUtil.toWan(play) + "观看";
            long aid = card.getLong("aid");
            String bvid = card.getString("bvid");
            String title = card.getString("title");
                            
            season.cards.add(new VideoCard(title,"",playStr,cover,aid,bvid));
        }
        
        season.view = ToolsUtil.toWan(view);
        
        return season;
    }
}
