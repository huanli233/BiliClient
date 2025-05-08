package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.PageInfo;
import com.RobinNotBad.BiliClient.model.Series;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//我宣布：
//在此之后：series统一叫系列，collection统一叫合集
//我看b站他们自己都没搞明白，全都叫合集，请允许我在此问候下他们的开发者……
//2024-10-20
//我靠怎么还有season

public class SeriesApi {


    public static int getUserSeries(long mid, int page, List<Series> seasonList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/polymer/web-space/seasons_series_list?";
        url += "mid=" + mid + "&page_num=" + page + "&page_size=20";
        JSONObject all = NetWorkUtil.getJson(ConfInfoApi.signWBI(url), NetWorkUtil.webHeaders);

        if (all.has("data") && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            if (data.has("items_lists") && !data.isNull("items_lists")) {
                JSONObject itemsLists = data.getJSONObject("items_lists");
                boolean finished = false;
                if (itemsLists.has("seasons_list")) {
                    JSONArray seasons = itemsLists.getJSONArray("seasons_list");
                    for (int i = 0; i < seasons.length(); i++)
                        seasonList.add(getSeriesByJson(seasons.getJSONObject(i)));
                    if (seasons.length() > 0) finished = true;
                }
                if (itemsLists.has("series_list")) {
                    JSONArray series = itemsLists.getJSONArray("series_list");
                    for (int i = 0; i < series.length(); i++)
                        seasonList.add(getSeriesByJson(series.getJSONObject(i)));
                    if (series.length() > 0) finished = true;
                }
                if (finished) return 0;
                else return 1;
            }
        }
        return -1;
    }

    /**
     * 获取合集信息
     *
     * @param mid       mid（或许可以为任意）
     * @param id 合集id
     * @param page      页数
     * @return Collection对象与分页信息
     */
    public static PageInfo getSeriesInfo(String type, long mid, int id, int page, ArrayList<VideoCard> videoList) throws JSONException, IOException {
        String url;
        switch (type) {
            case "series":
                url = "https://api.bilibili.com/x/series/archives" + new NetWorkUtil.FormData()
                    .setUrlParam(true)
                    .put("mid", mid)
                    .put("series_id", id)
                    .put("pn", page)
                    .put("ps", 30);
                break;
            case "season":
                url = "https://api.bilibili.com/x/polymer/web-space/seasons_archives_list" + new NetWorkUtil.FormData()
                        .setUrlParam(true)
                        .put("mid", mid)
                        .put("season_id", id)
                        .put("page_num", page)
                        .put("page_size", 30);
                break;
            default:
                throw new JSONException("传入类型有误！");
        }

        JSONObject result = NetWorkUtil.getJson(url);

        PageInfo pageInfo = new PageInfo();

        JSONObject data = result.optJSONObject("data");
        if (data != null) {
            JSONObject pageJson = data.optJSONObject("page");
            if (pageJson != null) {
                switch (type){
                    case "series":
                        pageInfo.page_num = pageJson.optInt("num", -1);
                        pageInfo.require_ps = pageJson.optInt("size", -1);
                        pageInfo.total = pageJson.optInt("total", -1);
                        break;
                    case "season":
                        pageInfo.page_num = pageJson.optInt("page_num", -1);
                        pageInfo.require_ps = pageJson.optInt("page_size", -1);
                        pageInfo.total = pageJson.optInt("total", -1);
                        break;
                }
            }

            JSONArray archives = data.optJSONArray("archives");
            if (archives != null) {
                pageInfo.return_ps = archives.length();
                for (int i = 0; i < archives.length(); i++) {
                    JSONObject archive = archives.getJSONObject(i);
                    VideoCard videoCard = new VideoCard();
                    videoCard.aid = archive.optLong("aid", 0);
                    videoCard.bvid = archive.optString("bvid","");
                    videoCard.cover = archive.optString("pic","");
                    videoCard.view = StringUtil.toWan(archive.getJSONObject("stat").optInt("view", -1));
                    videoCard.title = archive.optString("title","");
                    videoList.add(videoCard);
                }
            }
        }
        return pageInfo;
    }

    public static Series getSeriesByJson(JSONObject data) throws JSONException {
        Series series = new Series();
        JSONObject meta = data.getJSONObject("meta");

        if (meta.has("season_id")) {
            series.type = "season";
            series.id = meta.getInt("season_id");
        }
        else {
            series.type = "series";
            series.id = meta.getInt("series_id");
        }
        series.title = meta.getString("name");
        series.cover = meta.optString("cover", "");
        series.mid = meta.getLong("mid");
        series.intro = meta.getString("description");
        series.total = meta.getString("total");

        return series;
    }
}
