package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.Stats;
import com.RobinNotBad.BiliClient.model.UserInfo;
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
        UserInfo upInfo = new UserInfo();
        upInfo.mid = author.getLong("mid");
        upInfo.name = author.getString("name");
        upInfo.avatar = author.getString("face");
        upInfo.fans = author.getInt("fans");
        upInfo.level = author.getInt("level");
        articleInfo.upInfo = upInfo;

        JSONObject jsonStats = data.getJSONObject("stats");
        Stats stats = new Stats();
        stats.view = jsonStats.getInt("view");
        stats.favorite = jsonStats.getInt("favorite");
        stats.like = jsonStats.getInt("like");
        stats.reply = jsonStats.getInt("reply");
        stats.share = jsonStats.getInt("share");
        stats.coin = jsonStats.getInt("coin");
        stats.liked = data.getBoolean("is_like");
        articleInfo.stats = stats;

        articleInfo.wordCount = data.getInt("words");
        articleInfo.content = data.getString("content");
        articleInfo.keywords = data.getString("keywords");
        return articleInfo;
    }
}
