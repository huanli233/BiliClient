package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.Stats;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Response;

/*
专栏API
API是自己扒的
 */
public class ArticleApi {
    public static ArticleInfo getArticle(long id) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/article/view?";
        String args = "id=" + id + "&gaia_source=main_web&web_location=333.976";
        JSONObject result = NetWorkUtil.getJson(url + ConfInfoApi.signWBI(args));
        if (!result.has("data")) return null;
        JSONObject data = result.getJSONObject("data");

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

    /**
     * 另一个获取专栏相关信息api
     * @param id cvid
     * @return
     */
    public static ArticleInfo getArticleViewInfo(long id) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/article/viewinfo?";
        String args = "id=" + id + "&gaia_source=main_web&web_location=333.976&mobi_app=pc&from=web";
        JSONObject result = NetWorkUtil.getJson(url + ConfInfoApi.signWBI(args));
        if (!result.has("data")) return null;
        JSONObject data = result.getJSONObject("data");

        ArticleInfo articleInfo = new ArticleInfo();
        articleInfo.id = id;
        articleInfo.title = data.getString("title");
        articleInfo.banner = data.getString("banner_url");

        UserInfo upInfo = new UserInfo();
        upInfo.mid = data.getLong("mid");
        upInfo.name = data.getString("author_name");
        articleInfo.upInfo = upInfo;

        JSONObject jsonStats = data.getJSONObject("stats");
        Stats stats = new Stats();
        stats.view = jsonStats.getInt("view");
        stats.favorite = jsonStats.getInt("favorite");
        stats.like = jsonStats.getInt("like");
        stats.reply = jsonStats.getInt("reply");
        stats.share = jsonStats.getInt("share");
        stats.coin = jsonStats.getInt("coin");
        stats.liked = data.getInt("like") == 1;
        stats.favoured = data.getBoolean("favorite");
        stats.coined = data.getInt("coin");
        articleInfo.stats = stats;
        return articleInfo;
    }

    /**
     * 专栏点赞
     * @param cvid cvid
     * @param type true=点赞，false=取消赞
     * @return resultCode
     */
    public static int like(long cvid, boolean type) throws IOException {
        String url = "https://api.bilibili.com/x/article/like";
        Response resp = Objects.requireNonNull(NetWorkUtil.post(url, new NetWorkUtil.FormData()
                .put("id", cvid)
                .put("type", type ? 1 : 2)
                .put("csrf", SharedPreferencesUtil.getString("csrf",""))
                .toString(), NetWorkUtil.webHeaders));
        try {
            JSONObject respBody = new JSONObject(resp.body().string());
            return respBody.getInt("code");
        } catch (JSONException ignored) {
            return -1;
        }
    }

    /**
     * 专栏投币
     * @param cvid CVID
     * @param upid UP主ID
     * @param multiply 投币数量
     * @return 返回码
     */
    public static int addCoin(long cvid, long upid, int multiply) throws IOException {
        String url = "https://api.bilibili.com/x/web-interface/coin/add";
        Response resp = Objects.requireNonNull(NetWorkUtil.post(url, new NetWorkUtil.FormData()
                .put("aid", cvid)
                .put("upid", upid)
                .put("avtype", 2)
                .put("multiply", multiply)
                .put("csrf", SharedPreferencesUtil.getString("csrf",""))
                .toString(), NetWorkUtil.webHeaders));
        try {
            JSONObject respBody = new JSONObject(resp.body().string());
            return respBody.getInt("code");
        } catch (JSONException ignored) {
            return -1;
        }
    }

    /**
     * 收藏专栏
     * @param cvid CVID
     * @return 返回码
     */
    public static int favorite(long cvid) throws IOException {
        String url = "https://api.bilibili.com/x/article/favorites/add";
        Response resp = Objects.requireNonNull(NetWorkUtil.post(url, new NetWorkUtil.FormData()
                .put("id", cvid)
                .put("csrf", SharedPreferencesUtil.getString("csrf",""))
                .toString(), NetWorkUtil.webHeaders));
        try {
            JSONObject respBody = new JSONObject(resp.body().string());
            return respBody.getInt("code");
        } catch (JSONException ignored) {
            return -1;
        }
    }

    /**
     * 取消收藏专栏
     * @param cvid CVID
     * @return 返回码
     */
    public static int delFavorite(long cvid) throws IOException {
        String url = "https://api.bilibili.com/x/article/favorites/del";
        Response resp = Objects.requireNonNull(NetWorkUtil.post(url, new NetWorkUtil.FormData()
                .put("id", cvid)
                .put("csrf", SharedPreferencesUtil.getString("csrf",""))
                .toString(), NetWorkUtil.webHeaders));
        try {
            JSONObject respBody = new JSONObject(resp.body().string());
            return respBody.getInt("code");
        } catch (JSONException ignored) {
            return -1;
        }
    }
    public static int opusId2cvid(String opusId) throws JSONException,IOException{
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/opus/detail?id="+opusId+"&time_zone_offset="+TimeZone.getDefault().getRawOffset()/100000;
        JSONObject result = NetWorkUtil.getJson(url);
        int cvid = Integer.valueOf(result.getJSONObject("data").getJSONObject("item").getString("rid_str"));
        return cvid;
    }
}
