package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.LiveRoom;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.DmImgParamUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

//用户信息API

public class UserInfoApi {

    public static UserInfo getUserInfo(long mid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/card?mid=" + mid;
        JSONObject all = NetWorkUtil.getJson(url);
        if (all.has("data") && !all.isNull("data")) {
            JSONObject notice_all = NetWorkUtil.getJson("https://api.bilibili.com/x/space/notice?mid=" + mid);
            String notice;
            if (notice_all.has("data") && !notice_all.isNull("data"))
                notice = notice_all.getString("data");
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
            int attention = card.getInt("attention");

            JSONObject official_data = card.getJSONObject("Official");
            int official = official_data.getInt("role");
            String officialDesc = official_data.getString("title");

            String sys_notice = "";
            LiveRoom liveroom = null;
            try {
                JSONObject spaceInfo = getUserSpaceInfo(mid);
                if (spaceInfo != null) {
                    if (!spaceInfo.isNull("sys_notice")) {
                        sys_notice = spaceInfo.getJSONObject("sys_notice").optString("content");
                        if (sys_notice == null) sys_notice = "";
                        else sys_notice = sys_notice.replace("请点此查看纪念账号相关说明", "");
                    }
                    if (!spaceInfo.isNull("live_room")) {
                        JSONObject live_room = spaceInfo.getJSONObject("live_room");
                        if (live_room.getInt("roomStatus") == 1 && live_room.getInt("liveStatus") == 1) {
                            liveroom = new LiveRoom();
                            liveroom.title = "直播中：" + live_room.getString("title");
                            liveroom.user_cover = live_room.getString("cover");
                            liveroom.roomid = live_room.getLong("roomid");
                        }
                    }
                }
            } catch (Exception ignore) {
            }

            JSONObject vip = card.getJSONObject("vip");
            if (vip.getInt("status") == 1) {
                UserInfo result = new UserInfo(mid, name, avatar, sign, fans, attention, level, followed, notice, official, officialDesc, vip.getInt("role"), sys_notice, liveroom, card.getInt("is_senior_member"));
                result.vip_nickname_color = vip.optString("nickname_color", "");
                return result;
            } else
                return new UserInfo(mid, name, avatar, sign, fans, attention, level, followed, notice, official, officialDesc, sys_notice, liveroom, card.getInt("is_senior_member"));
        } else return null;
    }

    public static JSONObject getUserSpaceInfo(long mid) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/space/wbi/acc/info?";
        url += "mid=" + mid;
        JSONObject all = NetWorkUtil.getJson(ConfInfoApi.signWBI(DmImgParamUtil.getDmImgParamsUrl(url)));
        if (all.has("data") && !all.isNull("data")) {
            return all.getJSONObject("data");
        }
        return null;
    }

    public static UserInfo getCurrentUserInfo() throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/space/myinfo";
        JSONObject all = NetWorkUtil.getJson(url);
        if (all.has("data") && !all.isNull("data")) {
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

            JSONObject level_exp = data.getJSONObject("level_exp");
            long current_exp = level_exp.getLong("current_exp");
            long next_exp = level_exp.getLong("next_exp");

            return new UserInfo(mid, name, avatar, sign, fans, 0, level, false, "", official, officialDesc, current_exp, next_exp, data.getInt("is_senior_member"));
        } else return new UserInfo(0, "加载失败", "", "", 0, 0, 0, false, "", 0, "", 0);
    }

    public static int getCurrentUserCoin() {
        try {
            String url = "https://account.bilibili.com/site/getCoin";
            JSONObject all = NetWorkUtil.getJson(url);
            if (all.has("data") && !all.isNull("data")) {
                JSONObject data = all.getJSONObject("data");
                return data.has("money") ? data.getInt("money") : 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static int getUserVideos(long mid, int page, String searchKeyword, List<VideoCard> videoList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/space/wbi/arc/search?";
        url += "keyword=" + searchKeyword + "&mid=" + mid + "&order_avoided=true&order=pubdate&pn=" + page
                + "&ps=30&tid=0&web_location=333.999";
        JSONObject all = NetWorkUtil.getJson(ConfInfoApi.signWBI(DmImgParamUtil.getDmImgParamsUrl(url)));
        if (all.has("data") && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            JSONObject list = data.getJSONObject("list");
            if (list.has("vlist") && !list.isNull("vlist")) {
                JSONArray vlist = list.getJSONArray("vlist");
                if (vlist.length() == 0) return 1;
                for (int i = 0; i < vlist.length(); i++) {
                    JSONObject card = vlist.getJSONObject(i);
                    String cover = card.getString("pic");
                    long play = card.getLong("play");
                    String playStr = ToolsUtil.toWan(play) + "观看";
                    long aid = card.getLong("aid");
                    String bvid = card.getString("bvid");
                    String upName = card.getString("author");
                    String title = card.getString("title");

                    videoList.add(new VideoCard(title, upName, playStr, cover, aid, bvid));
                }
                return 0;
            } else return -1;
        } else return -1;
    }


    public static int getUserArticles(long mid, int page, List<ArticleCard> articleList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/space/wbi/article?";
        url += "mid=" + mid + "&order_avoided=true&order=pubdate&pn=" + page
                + "&ps=30&tid=0";
        JSONObject all = NetWorkUtil.getJson(ConfInfoApi.signWBI(url), NetWorkUtil.webHeaders);
        if (all.has("data") && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            if (data.has("articles")) {
                JSONArray list = data.getJSONArray("articles");
                if (list.length() == 0) return 1;
                for (int i = 0; i < list.length(); i++) {
                    JSONObject card = list.getJSONObject(i);

                    ArticleCard articleCard = new ArticleCard();
                    articleCard.id = card.getLong("id");
                    articleCard.title = card.getString("title");
                    JSONObject stats = card.getJSONObject("stats");
                    articleCard.view = ToolsUtil.toWan(stats.getInt("view")) + "阅读";
                    articleCard.cover = card.getString("banner_url");
                    JSONObject author = card.getJSONObject("author");
                    articleCard.upName = author.getString("name");
                    articleList.add(articleCard);
                }
                return 0;
            } else return 1;
        } else return -1;
    }

    public static int followUser(long mid, boolean isFollow) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/relation/modify?";
        String arg = "fid=" + mid + "&csrf=" + NetWorkUtil.getInfoFromCookie("bili_jct", SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
        if (isFollow) arg += "&act=1"; //关注
        else arg += "&act=2"; //取消关注
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, arg, NetWorkUtil.webHeaders).body()).string());
        return all.getInt("code");
    }

    public static void exitLogin() {
        try {
            String url = "https://passport.bilibili.com/login/exit/v2";
            NetWorkUtil.get(url, NetWorkUtil.webHeaders);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
