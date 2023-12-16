package com.RobinNotBad.BiliClient.api;

import android.util.Log;

import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;


//搜索API 自己写的
//逐渐感觉拆json是个很爽的事（
//2023-07-14

public class SearchApi {

    public static String seid = "";
    public static String search_keyword = "";

    public static JSONArray search(String keyword,int page) throws IOException , JSONException {
        if(!search_keyword.equals(keyword)) {
            search_keyword = keyword;
            seid = "";
        }

        String url = (SharedPreferencesUtil.getLong("mid",0) == 0 ? "https://api.bilibili.com/x/web-interface/wbi/search/all/v2" :"https://api.bilibili.com/x/web-interface/search/all/v2") + "?page=" + page + "&keyword=" + URLEncoder.encode(search_keyword, "UTF-8") + "&seid=" + seid;
        Log.e("debug-搜索链接",url);

        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.defHeaders).body()).string());  //得到一整个json

        JSONObject data = all.getJSONObject("data");  //搜索列表中的data项又是一个json，把它提出来

        seid = data.getString("seid");

        if(data.has("result") && !data.isNull("result")) return data.getJSONArray("result");  //其实这还不是我们要的结果，下面的函数对它进行再次拆解  这里做了判空
        else return null;
    }

    public static void getVideosFromSearchResult(JSONArray input,ArrayList<VideoCard> videoCardList) throws JSONException {
        for (int i = 0; i < input.length(); i++) {  //遍历所有的分类，找到视频那一项
            JSONObject type = input.getJSONObject(i);
            if(type.getString("result_type").equals("video")){
                JSONArray data = type.getJSONArray("data");    //把这个列表提出来，接着拆
                for (int j = 0; j < data.length(); j++) {
                    JSONObject card = data.getJSONObject(j);    //获得视频卡片

                    String title = card.getString("title");
                    title = title.replace("<em class=\"keyword\">","");  //标题里的红字，知道怎么显示但因为懒所以直接删（
                    title = title.replace("</em>","");                   //显示方式可以用imagespan，参照表情包那部分程序（EmoteUtil），期待后人补齐（
                    title = LittleToolsUtil.htmlToString(title);

                    String bvid = card.getString("bvid");
                    long aid = card.getLong("aid");
                    String cover = "http:" + card.getString("pic");  //离谱了嗷，前面甚至不肯加个http:
                    String upName = card.getString("author");

                    long play = card.getLong("play");
                    String playTimesStr = LittleToolsUtil.toWan(play) + "观看";

                    videoCardList.add(new VideoCard(title,upName,playTimesStr,cover,aid,bvid));
                }
            }
        }
    }

}

