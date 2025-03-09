package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


//搜索API 自己写的
//逐渐感觉拆json是个很爽的事（
//2023-07-14

public class SearchApi {

    public static String seid = "";
    public static String search_keyword = "";

    public static JSONArray search(String keyword, int page) throws IOException, JSONException {
        if (!search_keyword.equals(keyword)) {
            search_keyword = keyword;
            seid = "";
        }

        String url = "https://api.bilibili.com/x/web-interface/wbi/search/all/v2?";
        url += "page=" + page +
                "&keyword=" + URLEncoder.encode(search_keyword, "UTF-8") + "&seid=" + seid;

        JSONObject all = NetWorkUtil.getJson(ConfInfoApi.signWBI(url));  //得到一整个json

        JSONObject data = all.getJSONObject("data");  //搜索列表中的data项又是一个json，把它提出来

        seid = data.getString("seid");

        if (data.has("result") && !data.isNull("result"))
            return data.getJSONArray("result");  //其实这还不是我们要的结果，下面的函数对它进行再次拆解  这里做了判空
        else return null;
    }

    public static Object searchType(String keyword, int page, String type) throws IOException, JSONException {
        if (!search_keyword.equals(keyword)) {
            search_keyword = keyword;
            seid = "";
        }

        String url = "https://api.bilibili.com/x/web-interface/wbi/search/type?";
        url += "page=" + page +
                "&keyword=" + URLEncoder.encode(search_keyword, "UTF-8") + "&search_type=" + type + "&seid=" + seid;

        JSONObject all = NetWorkUtil.getJson(ConfInfoApi.signWBI(url));  //得到一整个json

        JSONObject data = all.getJSONObject("data");  //搜索列表中的data项又是一个json，把它提出来

        seid = data.getString("seid");

        if (data.has("result") && !data.isNull("result"))
            return data.get("result");  //其实这还不是我们要的结果，下面的函数对它进行再次拆解  这里做了判空
        else return null;
    }

    public static void getVideosFromSearchResult(JSONArray input, ArrayList<VideoCard> videoCardList, boolean first) throws JSONException {
        for (int i = 0; i < input.length(); i++) {  //遍历所有的分类，找到视频那一项
            JSONObject typecard = input.getJSONObject(i);
            String type = typecard.getString("result_type");
            if (type.equals("video")) {
                JSONArray data = typecard.getJSONArray("data");    //把这个列表提出来，接着拆
                for (int j = 0; j < data.length(); j++) {
                    JSONObject card = data.getJSONObject(j);    //获得视频卡片
                    if (!card.getString("type").equals("video"))
                        continue; //警惕虚假视频卡片（阿B为什么要把直播间放进视频结果里）

                    String title = card.getString("title");
                    title = title.replace("<em class=\"keyword\">", "").replace("</em>", "");
                    //标题里的红字，知道怎么显示但因为懒所以直接删（//显示方式可以用imagespan，参照表情包那部分程序（EmoteUtil），期待后人补齐（
                    title = ToolsUtil.htmlToString(title);

                    String bvid = card.getString("bvid");
                    long aid = card.getLong("aid");
                    String cover = "http:" + card.getString("pic");  //离谱了嗷，前面甚至不肯加个http:
                    String upName = card.getString("author");

                    long play = card.getLong("play");
                    String playTimesStr = ToolsUtil.toWan(play) + "观看";

                    videoCardList.add(new VideoCard(title, upName, playTimesStr, cover, aid, bvid, type));
                }
            } else if (type.equals("media_bangumi") && first) {
                JSONArray data = typecard.getJSONArray("data");
                for (int j = 0; j < data.length(); j++) {
                    JSONObject card = data.getJSONObject(j);    //获得番剧卡片

                    String title = card.getString("title");
                    title = title.replace("<em class=\"keyword\">", "").replace("</em>", "");
                    //标题里的红字,直接上面复制粘贴
                    title = ToolsUtil.htmlToString(title);
                    String cover = card.getString("cover");
                    String upName = card.getString("areas");
                    long aid = card.getLong("media_id");
                    String bvid = card.getString("season_id");
                    String playTimesStr = card.getString("index_show");
                    videoCardList.add(new VideoCard(title, upName, playTimesStr, cover, aid, bvid, type));
                }
            }
        }
    }

    public static void getUsersFromSearchResult(JSONArray input, List<UserInfo> userInfoList) throws JSONException {
        for (int i = 0; i < input.length(); i++) {
            JSONObject card = input.getJSONObject(i);    //获得用户卡片

            long mid = card.getLong("mid");
            String name = card.getString("uname");
            String avatar = "http:" + card.getString("upic");
            String sign = card.getString("usign");
            int fans = card.getInt("fans");
            int level = card.getInt("level");

            userInfoList.add(new UserInfo(mid, name, avatar, sign, fans, 0, level, false, "", 0, "", 0));
        }
    }

    public static void getArticlesFromSearchResult(JSONArray input, ArrayList<ArticleCard> articleCardList) throws JSONException {
        for (int i = 0; i < input.length(); i++) {
            ArticleCard articleCard = new ArticleCard();
            JSONObject card = input.getJSONObject(i);    //获得专栏卡片

            articleCard.id = card.getLong("id");
            if (card.getJSONArray("image_urls").length() > 0)
                articleCard.cover = "http:" + card.getJSONArray("image_urls").getString(0);
            else articleCard.cover = "";
            articleCard.upName = card.getString("category_name");
            articleCard.title = ToolsUtil.htmlReString(card.getString("title"));
            articleCard.view = ToolsUtil.toWan(card.getInt("view")) + "阅读";

            articleCardList.add(articleCard);
        }
    }
}

