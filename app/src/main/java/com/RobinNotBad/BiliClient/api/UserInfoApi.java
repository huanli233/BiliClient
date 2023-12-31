package com.RobinNotBad.BiliClient.api;

import android.util.Log;

import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

//用户信息API

public class UserInfoApi {

    public static UserInfo getUserInfo(long mid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/card?mid=" + mid;
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.defHeaders).body()).string());
        if(all.has("data") && !all.isNull("data")) {
            JSONObject notice_all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get("https://api.bilibili.com/x/space/notice?mid=" + mid, ConfInfoApi.defHeaders).body()).string());
            String notice;
            if(notice_all.has("data") && !notice_all.isNull("data")) notice = notice_all.getString("data");
            else notice = "";
            JSONObject data = all.getJSONObject("data");
            boolean followed = data.getBoolean("following");
            int fans = data.getInt("follower");

            JSONObject card = data.getJSONObject("card");
            String name = card.getString("name");
            String avatar = card.getString("face");
            String sign = card.getString("sign");
            JSONObject levelInfo = card.getJSONObject("level_info");
            int level = levelInfo.getInt("current_level");

            return new UserInfo(mid,name,avatar,sign,fans,level,followed,notice);
        }
        else return null;

    }

    public static UserInfo getCurrentUserInfo() throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/space/myinfo";
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.defHeaders).body()).string());
        if(all.has("data") && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            long mid = data.getLong("mid");
            String name = data.getString("name");
            String avatar = data.getString("face");
            String sign = data.getString("sign");
            int fans = data.getInt("follower");
            int level = data.getInt("level");
            return new UserInfo(mid,name,avatar,sign,fans,level,false,"");
        }
        else return new UserInfo(0,"加载失败","","",0,0,false,"");
    }
    
    public static int getCurrentUserCoin()  {
        try{
            String url = "https://account.bilibili.com/site/getCoin";
            JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.defHeaders).body()).string());
            if(all.has("data") && !all.isNull("data")) {
                JSONObject data = all.getJSONObject("data");
                if(data.get("money") == null) return 0;
                else return data.getInt("money");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return 0;
    }


    public static int getUserVideos(long mid, int page, String searchKeyword,ArrayList<VideoCard> videoList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/space/wbi/arc/search?";
        String args = "keyword=" + searchKeyword + "&mid=" + mid + "&order_avoided=true&order=pubdate&pn=" + page
                + "&ps=30&tid=0";
        Log.e("debug",url);
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url + ConfInfoApi.signWBI(args), ConfInfoApi.defHeaders).body()).string());
        if(all.has("data") && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            JSONObject list = data.getJSONObject("list");
            if(list.has("vlist") && !list.isNull("vlist")){
                JSONArray vlist = list.getJSONArray("vlist");
                if(vlist.length() == 0) return 1;
                for (int i = 0; i < vlist.length(); i++) {
                    JSONObject card = vlist.getJSONObject(i);
                    String cover = card.getString("pic");
                    long play = card.getLong("play");
                    String playStr = LittleToolsUtil.toWan(play) + "观看";
                    long aid = card.getLong("aid");
                    String bvid = card.getString("bvid");
                    String upName = card.getString("author");
                    String title = card.getString("title");
                    videoList.add(new VideoCard(title,upName,playStr,cover,aid,bvid));
                }
                return 0;
            }
            else return -1;
        }
        else return -1;
    }


    public static int getUserArticles(long mid, int page,ArrayList<ArticleInfo> articleList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/space/wbi/article?";
        String args = "mid=" + mid + "&order_avoided=true&order=pubdate&pn=" + page
                + "&ps=30&tid=0";
        Log.e("debug",url);
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url + ConfInfoApi.signWBI(args), ConfInfoApi.defHeaders).body()).string());
        if(all.has("data") && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            if(data.has("articles")){
                JSONArray list = data.getJSONArray("articles");
                if(list.length() == 0) return 1;
                for (int i = 0; i < list.length(); i++) {
                    JSONObject card = list.getJSONObject(i);

                    ArticleInfo articleInfo = new ArticleInfo();
                    articleInfo.id = card.getLong("id");
                    articleInfo.title = card.getString("title");
                    articleInfo.summary = card.getString("summary");
                    articleInfo.keywords = "";
                    articleInfo.view = Integer.parseInt(LittleToolsUtil.toWan(card.getJSONObject("stats").getInt("view")) + "浏览");
                    articleInfo.favourite = card.getJSONObject("stats").getInt("favorite");
                    articleInfo.like = card.getJSONObject("stats").getInt("like");
                    articleInfo.reply = card.getJSONObject("stats").getInt("reply");
                    articleInfo.share = card.getJSONObject("stats").getInt("share");
                    articleInfo.coin = card.getJSONObject("stats").getInt("coin");
                    articleInfo.ctime = card.getLong("ctime");
                    articleInfo.wordCount = card.getInt("words");
                    articleInfo.banner = card.getString("banner_url");
                    articleInfo.upMid = card.getJSONObject("author").getLong("mid");
                    articleInfo.upName = card.getJSONObject("author").getString("name");
                    articleInfo.upAvatar = card.getJSONObject("author").getString("face");
                    articleInfo.upFans = 0;
                    articleInfo.upLevel = 0;

                    articleList.add(articleInfo);
                }
                return 0;
            }else return 1;
        }
        else return -1;
    }


}
