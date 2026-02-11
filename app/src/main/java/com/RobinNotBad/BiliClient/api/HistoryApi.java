package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.ApiResult;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.Logu;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class HistoryApi {

    public static final int ARTICLE_HISTORY_TYPE = 3;
    public static final int ARTICLE_LIST_HISTORY_TYPE = 5;

    /**
     * 上传历史记录
     *
     * @param aid      视频aid
     * @param cid      分集cid
     * @param progress 观看进度，单位为s
     * @throws IOException
     */
    public static void reportHistory(long aid, long cid, long progress) throws IOException {
        String url = "https://api.bilibili.com/x/v2/history/report";
        String per = "aid=" + aid + "&cid=" + cid
                + "&progress=" + (progress >= 0 ? progress : "")
                + "&platform=pc"
                + "&csrf=" + SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        NetWorkUtil.post(url, per, NetWorkUtil.webHeaders);
    }

    /**
     * 上传专栏历史记录
     *
     * @param aid  专栏cvid
     * @param type 内容类型，3=article，5=article-list
     * @throws IOException
     */
    public static void reportArticleHistory(long aid, int type) throws IOException {
        String url = "https://api.bilibili.com/x/v2/history/report";
        String per = "aid=" + aid + "&type=" + type
                + "&csrf=" + SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        postHistoryReport(url, per);
    }

    /**
     * 上传 article-list 历史记录。
     */
    public static void reportArticleListHistory(long aid) throws IOException {
        reportArticleHistory(aid, ARTICLE_LIST_HISTORY_TYPE);
    }

    /**
     * 上传 article-list 历史记录（携带当前专栏ID用于回显）。
     *
     * @param listId 合集ID
     * @param cvid   当前专栏ID（用于 history.cid）
     */
    public static void reportArticleListHistory(long listId, long cvid) throws IOException {
        String url = "https://api.bilibili.com/x/v2/history/report";
        String per = "aid=" + listId
                + (cvid > 0 ? "&cid=" + cvid : "")
                + "&type=" + ARTICLE_LIST_HISTORY_TYPE
                + "&csrf=" + SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        postHistoryReport(url, per);
    }

    private static void postHistoryReport(String url, String form) throws IOException {
        okhttp3.Response response = NetWorkUtil.post(url, form, NetWorkUtil.webHeaders);
        if (response == null || response.body() == null) {
            return;
        }
        String body = response.body().string();
        try {
            JSONObject json = new JSONObject(body);
            int code = json.optInt("code", -1);
            String message = json.optString("message", "");
            if (code != 0) {
                Logu.e("HistoryReport", "history/report failed: code=" + code + ", message=" + message + ", form=" + form);
            } else {
                Logu.i("HistoryReport", "history/report ok: form=" + form);
            }
        } catch (Exception ignored) {
            Logu.e("HistoryReport", "history/report invalid response: " + body);
        }
    }

    /**
     * 获取历史记录（支持视频和专栏）
     *
     * @param lastResult 上一次获取返回的ApiResult，如果是第一次就传入新对象
     * @param videoList  已有的视频列表
     * @param type       历史记录类型，"all"表示全部，"archive"表示视频，"article"表示专栏
     * @return 新的ApiResult，包含了返回码、文本信息以及翻页所需的offset
     * @throws IOException
     * @throws JSONException
     */
    public static ApiResult getHistory(ApiResult lastResult, List<VideoCard> videoList, String type) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/history/cursor?type=" + type
                + "&ps=20"
                + "&view_at=" + lastResult.timestamp
                + "&max=" + lastResult.offset;
        JSONObject result = NetWorkUtil.getJson(url);
        ApiResult apiResult = new ApiResult(result);
        if (!result.isNull("data")) {
            JSONObject data = result.getJSONObject("data");
            JSONArray list = data.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject videoCard = list.getJSONObject(i);
                String title = videoCard.getString("title");
                String cover = videoCard.optString("cover", "");
                String upName = videoCard.getString("author_name");
                int progress = videoCard.getInt("progress");

                JSONObject history = videoCard.getJSONObject("history");
                long oid = history.getLong("oid");
                long aid = oid;
                long cid = history.optLong("cid", 0);
                long kid = videoCard.optLong("kid", 0);
                String bvid = history.optString("bvid", "");
                String business = history.optString("business", "archive");

                String viewStr;
                String contentType;

                // 兼容专栏异常形态（如 article-list），优先按专栏处理。
                boolean isArticleLike = "article".equals(business)
                        || "article-list".equals(business)
                        || ("专栏".equals(videoCard.optString("badge", ""))
                        && videoCard.optInt("videos", 1) == 0);

                if (isArticleLike) {
                    contentType = "article";
                    // 优先使用 cid（如果存在）作为实际 cvid，避免 article-list 被固定到合集首篇。
                    if (cid > 0) {
                        aid = cid;
                    }
                    viewStr = progress > 0 ? "已阅读" : "还没看过";
                    // 专栏的封面可能在covers数组中
                    if (cover.isEmpty() && videoCard.has("covers") && !videoCard.isNull("covers")) {
                        JSONArray covers = videoCard.getJSONArray("covers");
                        if (covers.length() > 0) {
                            cover = covers.getString(0);
                        }
                    }
                } else {
                    contentType = "video";
                    if (progress == 0) viewStr = "还没看过";
                    else viewStr = "看到" + StringUtil.toTime(videoCard.getInt("progress"));
                }

                VideoCard card = new VideoCard(title, upName, viewStr, cover, aid, bvid, contentType);
                card.kid = kid;
                card.historyBusiness = business;
                card.historyOid = oid;
                card.historyCid = cid;
                // 占位符特征：专栏类记录 oid 与 cvid 不一致（oid 常为合集或旧占位值）
                card.historyPlaceholder = isArticleLike && cid > 0 && oid != cid;
                videoList.add(card);
            }
            if (list.length() == 0) apiResult.isBottom = true;

            JSONObject cursor = data.getJSONObject("cursor");
            apiResult.business = cursor.optString("business");
            apiResult.offset = cursor.optLong("max");
            apiResult.timestamp = cursor.optLong("view_at");
        }
        return apiResult;
    }

    /**
     * 获取历史记录（默认获取全部类型）
     *
     * @param lastResult 上一次获取返回的ApiResult，如果是第一次就传入新对象
     * @param videoList  已有的视频列表
     * @return 新的ApiResult，包含了返回码、文本信息以及翻页所需的offset
     * @throws IOException
     * @throws JSONException
     */
    public static ApiResult getHistory(ApiResult lastResult, List<VideoCard> videoList) throws IOException, JSONException {
        return getHistory(lastResult, videoList, "all");
    }

    /**
     * 删除单条历史记录（基于事件ID kid）
     *
     * @param kid 历史事件ID
     */
    public static void deleteHistory(long kid) {
        if (kid <= 0) return;
        try {
            String url = "https://api.bilibili.com/x/v2/history/delete";
            String per = "kid=" + kid
                    + "&csrf=" + SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
            NetWorkUtil.post(url, per, NetWorkUtil.webHeaders);
        } catch (Exception ignored) {
        }
    }

}
