package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

/*
专栏API
API是自己扒的
 */
public class ArticleApi {
    public static ArticleInfo getArticle(long id) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/article/view?";
        String args = "id=" + id + "&gaia_source=main_web&web_location=333.976";
        JSONObject data = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url + ConfInfoApi.signWBI(args), ConfInfoApi.defHeaders).body()).string()).getJSONObject("data");

        ArticleInfo articleInfo = new ArticleInfo();
        articleInfo.id = id;
        articleInfo.title = data.getString("title");
        articleInfo.summary = data.getString("summary");
        articleInfo.banner = data.getString("banner_url");
        articleInfo.ctime = data.getLong("ctime");
        articleInfo.upMid = data.getJSONObject("author").getLong("mid");
        articleInfo.upName = data.getJSONObject("author").getString("name");
        articleInfo.upAvatar = data.getJSONObject("author").getString("face");
        articleInfo.upFans = data.getJSONObject("author").getInt("fans");
        articleInfo.upLevel = data.getJSONObject("author").getInt("level");
        articleInfo.view = data.getJSONObject("stats").getString("view");
        articleInfo.favourite = data.getJSONObject("stats").getInt("favorite");
        articleInfo.like = data.getJSONObject("stats").getInt("like");
        articleInfo.reply = data.getJSONObject("stats").getInt("reply");
        articleInfo.share = data.getJSONObject("stats").getInt("share");
        articleInfo.coin = data.getJSONObject("stats").getInt("coin");
        articleInfo.wordCount = data.getInt("words");
        articleInfo.isLike = (data.getBoolean("is_like") ? 1:0);
        articleInfo.content = data.getString("content");
        articleInfo.keywords = data.getString("keywords");
        return articleInfo;
    }
}
