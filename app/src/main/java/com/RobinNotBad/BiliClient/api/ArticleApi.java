package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/*
专栏API
API是自己扒的
 */
public class ArticleApi {
    public static ArticleInfo getArticle(long id) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/article/view?";
        String args = "id=" + id + "&gaia_source=main_web&web_location=333.976";
        JSONObject data = NetWorkUtil.getJson(url + ConfInfoApi.signWBI(args)).getJSONObject("data");

        ArticleInfo articleInfo = new ArticleInfo();
        articleInfo.id = id;
        articleInfo.title = data.getString("title");
        articleInfo.summary = data.getString("summary");
        articleInfo.banner = data.getString("banner_url");
        articleInfo.ctime = data.getLong("ctime");
        JSONObject author = data.getJSONObject("author");
        articleInfo.upMid = author.getLong("mid");
        articleInfo.upName = author.getString("name");
        articleInfo.upAvatar = author.getString("face");
        articleInfo.upFans = author.getInt("fans");
        articleInfo.upLevel = author.getInt("level");
        JSONObject stats = data.getJSONObject("stats");
        articleInfo.view = ToolsUtil.toWan(stats.getInt("view")) + "阅读";
        articleInfo.favourite = stats.getInt("favorite");
        articleInfo.like = stats.getInt("like");
        articleInfo.reply = stats.getInt("reply");
        articleInfo.share = stats.getInt("share");
        articleInfo.coin = stats.getInt("coin");
        articleInfo.wordCount = data.getInt("words");
        articleInfo.isLike = data.getBoolean("is_like");
        articleInfo.content = data.getString("content");
        articleInfo.keywords = data.getString("keywords");
        return articleInfo;
    }
}
