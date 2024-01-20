package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Response;


//推荐API 自己写的
//#如果想要增/删/改内容，可以直接把url复制到浏览器里，把得到的一大串东西交给json解析软件就能缕清结构了，然后就是拆拆拆
//2023-07-12
//2023-12-09

public class RecommendApi {
    public static void getRecommend(ArrayList<VideoCard> videoCardList) throws IOException, JSONException {
        String url = (SharedPreferencesUtil.getLong("mid",0) == 0 ? "https://api.bilibili.com/x/web-interface/wbi/index/top/feed/rcmd" : "https://api.bilibili.com/x/web-interface/index/top/rcmd");

        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());  //得到一整个json

        JSONObject data = result.getJSONObject("data");  //推荐列表中的data项又是一个json，把它提出来

        JSONArray item = data.getJSONArray("item");  //data里面的items是视频卡片列表，把它提出来

        for (int i = 0; i < item.length(); i++) {    //遍历所有的视频卡片
            JSONObject card = item.getJSONObject(i);
            String bvid = card.getString("bvid");    //bv号
            String cover = card.getString("pic");    //封面图片
            String title = card.getString("title");    //标题
            String upName = card.getJSONObject("owner").getString("name");  //up主名字
            String view = LittleToolsUtil.toWan(card.getJSONObject("stat").getInt("view")) + "观看";    //播放量
            videoCardList.add(new VideoCard(title, upName, view, cover, 0, bvid));
        }
    }

    public static ArrayList<VideoCard> getRelated(long aid) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/web-interface/archive/related?aid=" + aid;

        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());  //得到一整个json

        ArrayList<VideoCard> videoList = new ArrayList<>();
        if(result.has("data") && !result.isNull("data")) {
            JSONArray data = result.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject card = data.getJSONObject(i);
                VideoCard videoCard = new VideoCard();
                videoCard.aid = card.getLong("aid");
                videoCard.view = LittleToolsUtil.toWan(card.getJSONObject("stat").getLong("view")) + "观看";
                videoCard.cover = card.getString("pic");
                videoCard.title = card.getString("title");
                videoCard.upName = card.getJSONObject("owner").getString("name");
                videoList.add(videoCard);
            }

        }

        return videoList;
    }

    public static void getPopular(ArrayList<VideoCard> videoCardList,int page) throws JSONException, IOException {
        //热门接口在携带Cookie时返回的数据的排行是个性化的

        String url = "https://api.bilibili.com/x/web-interface/popular?pn=" + page + "&ps=10";

        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());  //得到一整个json

        if(result.has("data") && !result.isNull("data")){
            if(result.getJSONObject("data").has("list")){
                JSONArray list = result.getJSONObject("data").getJSONArray("list");
                for(int i = 0;i < list.length();i++){
                    JSONObject card = list.getJSONObject(i);
                    VideoCard videoCard = new VideoCard();
                    videoCard.aid = card.getLong("aid");
                    videoCard.cover = card.getString("pic");
                    videoCard.title = card.getString("title");
                    videoCard.upName = card.getJSONObject("owner").getString("name");
                    videoCard.view = LittleToolsUtil.toWan(card.getJSONObject("stat").getLong("view")) + "观看";
                    videoCardList.add(videoCard);
                }
            }
        }
    }
    public static void getPrecious(ArrayList<VideoCard> videoCardList,int page) throws JSONException, IOException {
        //热门接口在携带Cookie时返回的数据的排行是个性化的

        String url = "https://api.bilibili.com/x/web-interface/popular/precious?page=" + page + "&page_size=10";

        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());  //得到一整个json

        if(result.has("data") && !result.isNull("data")){
            if(result.getJSONObject("data").has("list")){
                JSONArray list = result.getJSONObject("data").getJSONArray("list");
                for(int i = 0;i < list.length();i++){
                    JSONObject card = list.getJSONObject(i);
                    VideoCard videoCard = new VideoCard();
                    videoCard.aid = card.getLong("aid");
                    videoCard.cover = card.getString("pic");
                    videoCard.title = card.getString("title");
                    videoCard.upName = card.getJSONObject("owner").getString("name");
                    videoCard.view = LittleToolsUtil.toWan(card.getJSONObject("stat").getLong("view")) + "观看";
                    videoCardList.add(videoCard);
                }
            }
        }
    }
}
