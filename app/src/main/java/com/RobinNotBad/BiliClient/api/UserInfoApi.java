package com.RobinNotBad.BiliClient.api;

import android.util.Log;

import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.UserInfo;
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

            JSONObject official_data = card.getJSONObject("Official");
            int official = official_data.getInt("role");
            String officialDesc = official_data.getString("title");
            return new UserInfo(mid,name,avatar,sign,fans,level,followed,notice,official,officialDesc);
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

            JSONObject official_data = data.getJSONObject("official");
            int official = official_data.getInt("role");
            String officialDesc = official_data.getString("desc");
            return new UserInfo(mid,name,avatar,sign,fans,level,false,"",official,officialDesc);
        }
        else return new UserInfo(0,"加载失败","","",0,0,false,"",0,"");
    }
    
    public static int getCurrentUserCoin()  {
        try{
            String url = "https://account.bilibili.com/site/getCoin";
            JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.defHeaders).body()).string());
            if(all.has("data") && !all.isNull("data")) {
                JSONObject data = all.getJSONObject("data");
                return data.has("money") ? data.getInt("money") : 0;
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
                    JSONObject stats = card.getJSONObject("stats");
                    articleInfo.view = LittleToolsUtil.toWan(stats.getInt("view")) + "阅读";
                    articleInfo.favourite = stats.getInt("favorite");
                    articleInfo.like = stats.getInt("like");
                    articleInfo.reply = stats.getInt("reply");
                    articleInfo.share = stats.getInt("share");
                    articleInfo.coin = stats.getInt("coin");
                    articleInfo.ctime = card.getLong("ctime");
                    articleInfo.wordCount = card.getInt("words");
                    articleInfo.banner = card.getString("banner_url");
                    JSONObject author = card.getJSONObject("author");
                    articleInfo.upMid = author.getLong("mid");
                    articleInfo.upName = author.getString("name");
                    articleInfo.upAvatar = author.getString("face");
                    articleInfo.isLike = false;
                    articleInfo.upFans = 0;
                    articleInfo.upLevel = 0;

                    articleList.add(articleInfo);
                }
                return 0;
            }else return 1;
        }
        else return -1;
    }

    public static boolean followUser(long mid,boolean isFollow) {
        try {
            String url = "https://api.bilibili.com/x/relation/modify?";
            String arg = "fid=" + mid + "&csrf=" + LittleToolsUtil.getInfoFromCookie("bili_jct", SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""));
            if(isFollow) arg += "&act=1"; //关注
            else arg += "&act=2"; //取消关注
            JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url,arg, ConfInfoApi.webHeaders).body()).string());
            return (all.getInt("code") == 0);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void exitLogin(){
        try {
            String url = "https://passport.bilibili.com/login/exit/v2";
            NetWorkUtil.get(url, ConfInfoApi.webHeaders);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
