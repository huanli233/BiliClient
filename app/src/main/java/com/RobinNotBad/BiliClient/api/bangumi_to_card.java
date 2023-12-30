package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class bangumi_to_card {
    public static JSONArray bangumi_to_car(String season_id) throws JSONException, IOException {
        JSONObject result =  GetMain_section(season_id);
        JSONArray cardArray = new JSONArray();
        JSONObject main_section = result.getJSONObject("main_section");
        JSONArray episodes = main_section.getJSONArray("episodes");
        JSONObject input = new JSONObject();
        ArrayList<VideoCard> list = new ArrayList<VideoCard>();
        for (int j = 0;j < episodes.length();j++){
            JSONObject array = episodes.getJSONObject(j);
            String title = array.getString("long_title");
            String upname = array.getString("badge");
            String playTimesStr = "敬请期待" + "观看";
            String cover = array.getString("cover");
            String cid = String.valueOf(array.getLong("cid"));
            long aid = array.getLong("aid");
            list.add(new VideoCard(title,upname,playTimesStr,cover,aid,cid));
        }
        input.put("card",list);
        input.put("title",main_section.getString("title"));
        cardArray.put(input);
        JSONArray section = result.getJSONArray("section");
        for (int j = 0;j < section.length();j++){
            input = new JSONObject();
            list = new ArrayList<>();
            JSONObject card = section.getJSONObject(j);
            JSONArray CardArray = card.getJSONArray("episodes");
            for (int i = 0;i < CardArray.length();i++){
                JSONObject array = CardArray.getJSONObject(i);
                String title = array.getString("long_title");
                String upname = array.getString("badge");
                String playTimesStr = "敬请期待" + "观看";
                String cover = array.getString("cover");
                String cid = String.valueOf(array.getLong("cid"));
                long aid = array.getLong("aid");
                list.add(new VideoCard(title,upname,playTimesStr,cover,aid,cid));
            }
            input.put("card",list);
            input.put("title",card.getString("title"));
            cardArray.put(input);
        }
        return cardArray;
    }
    public static JSONObject GetMain_section(String season_id)  throws IOException, JSONException {
        String url = "https://api.bilibili.com/pgc/web/season/section?season_id=" + season_id;
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url).body()).string());  //得到一整个json
        return all.getJSONObject("result");
    }
}

