package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.ApiResult;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class HistoryApi {

    /**
     * 上传历史记录
     *
     * @param aid 视频aid
     * @param cid 分集cid
     * @param progress 观看进度，单位为s
     * @throws IOException
     */
    public static void reportHistory(long aid, long cid, long progress) throws IOException {
        String url = "https://api.bilibili.com/x/v2/history/report";
        String per = "aid=" + aid + "&cid=" + cid
                + "&progress=" + (progress >= 0 ? progress : "")
                + "&platform=pc"
                + "&csrf=" + SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf,"");
        NetWorkUtil.post(url, per, NetWorkUtil.webHeaders);
    }

    /**
     * 获取视频历史记录
     *
     * @param lastResult 上一次获取返回的ApiResult，如果是第一次就传入新对象
     * @param videoList 已有的视频列表
     * @return 新的ApiResult，包含了返回码、文本信息以及翻页所需的offset
     * @throws IOException
     * @throws JSONException
     */
    public static ApiResult getHistory(ApiResult lastResult, List<VideoCard> videoList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/history/cursor?type=archive&view_at=" + lastResult.timestamp +"&business=" + lastResult.business + "&max=" + lastResult.offset;
        JSONObject result = NetWorkUtil.getJson(url);
        ApiResult apiResult = new ApiResult(result);
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
                else viewStr = "看到" + StringUtil.toTime(videoCard.getInt("progress"));

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

}
