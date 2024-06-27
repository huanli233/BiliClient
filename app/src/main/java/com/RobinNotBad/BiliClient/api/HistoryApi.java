package com.RobinNotBad.BiliClient.api;

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

    public static int getHistory(int page, List<VideoCard> videoList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/history?pn=" + page + "&ps=30";
        JSONObject result = NetWorkUtil.getJson(url);
        if (!result.isNull("data")) {
            JSONArray data = result.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject videoCard = data.getJSONObject(i);
                long aid = videoCard.getLong("aid");
                String bvid = videoCard.getString("bvid");
                String title = videoCard.getString("title");
                String cover = videoCard.getString("pic");
                String upName = videoCard.getJSONObject("owner").getString("name");
                int progress = videoCard.getInt("progress");
                String viewStr;
                if (progress == 0) viewStr = "还没看过";
                else viewStr = "看到" + ToolsUtil.toTime(videoCard.getInt("progress"));
                videoList.add(new VideoCard(title, upName, viewStr, cover, aid, bvid));
            }
            return 0;
        } else return 1;
    }
}
