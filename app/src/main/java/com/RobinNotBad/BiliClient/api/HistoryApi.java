package com.RobinNotBad.BiliClient.api;

import android.util.Pair;

import com.RobinNotBad.BiliClient.model.ApiResult;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class HistoryApi {
    public static void reportHistory(long aid, long cid, long mid, long progress) throws IOException {
        String url = "https://api.bilibili.com/x/report/web/heartbeat";
        String per = "aid=" + aid + "&cid=" + cid + "&mid=" + mid + "&csrf=" + SharedPreferencesUtil.getString("csrf", "") + "&played_time=" + progress + "&realtime=0&start_ts=" + (System.currentTimeMillis() / 1000) + "&type=3&dt=2&play_type=1";
        NetWorkUtil.post(url, per, NetWorkUtil.webHeaders);
    }

    public static ApiResult getHistory(ApiResult lastResult, List<VideoCard> videoList) throws IOException, JSONException {
        ApiResult apiResult = new ApiResult();
        String url = "https://api.bilibili.com/x/web-interface/history/cursor?type=archive&view_at=" + lastResult.timestamp +"&business=" + lastResult.business + "&max=" + lastResult.offset;
        JSONObject result = NetWorkUtil.getJson(url);
        apiResult.code = result.optInt("code");
        apiResult.message = result.optString("message");
        if (!result.isNull("data")) {
            JSONObject data = result.getJSONObject("data");
            JSONArray list = data.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject videoCard = list.getJSONObject(i);
                String title = videoCard.getString("title");
                String cover = videoCard.getString("cover");
                String upName = videoCard.getString("author_name");
                int progress = videoCard.getInt("progress");

                JSONObject history = videoCard.getJSONObject("history");
                long aid = history.getLong("oid");
                String bvid = history.getString("bvid");

                String viewStr;
                if (progress == 0) viewStr = "还没看过";
                else viewStr = "看到" + ToolsUtil.toTime(videoCard.getInt("progress"));

                videoList.add(new VideoCard(title, upName, viewStr, cover, aid, bvid));
            }
            if(list.length() == 0) apiResult.isBottom = true;

            JSONObject cursor = data.getJSONObject("cursor");
            apiResult.business = cursor.optString("business");
            apiResult.offset = cursor.optLong("max");
            apiResult.timestamp = cursor.optLong("view_at");
        }
        return apiResult;
    }

    public static Pair<Long, Integer> getWatchProgress(long aid, boolean isBangumi) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/history/cursor?max=" + aid + "&ps=1&type="
                + (isBangumi ? "pgc" : "archive") + "&business=" + (isBangumi ? "pgc" : "archive");
        JSONObject result = NetWorkUtil.getJson(url);
        if (!result.isNull("data")) {
            JSONObject data = result.getJSONObject("data");
            JSONArray list = data.getJSONArray("list");

            JSONObject video = list.optJSONObject(0);
            JSONObject history = video.optJSONObject("history");
            if(history == null) return new Pair<>(0L, 0);
            return new Pair<>(history.optLong("cid", 0), video.getInt("progress"));
        }
        return new Pair<>(0L, 0);
    }
}
