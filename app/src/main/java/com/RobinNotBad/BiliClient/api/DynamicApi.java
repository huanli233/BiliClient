package com.RobinNotBad.BiliClient.api;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.At;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.Emote;
import com.RobinNotBad.BiliClient.model.LiveRoom;
import com.RobinNotBad.BiliClient.model.Stats;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.DmImgParamUtil;
import com.RobinNotBad.BiliClient.util.EmoteUtil;
import com.RobinNotBad.BiliClient.util.Logu;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;
import okhttp3.ResponseBody;

//新的动态api，旧的那个实在太蛋疼而且说不定随时会被弃用（

public class DynamicApi {

    /**
     * 发送纯文本动态
     *
     * @param content 文字内容
     * @return 发送成功返回的动态id，失败返回-1
     */
    public static long publishTextContent(String content) throws IOException {
        String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/create";
        Response resp = Objects.requireNonNull(NetWorkUtil.post(url, new NetWorkUtil.FormData()
                .put("dynamic_id", 0)
                .put("type", 4)
                .put("rid", 0)
                .put("content", content)
                .put("csrf", SharedPreferencesUtil.getString("csrf", ""))
                .toString(), NetWorkUtil.webHeaders));
        try {
            ResponseBody body = resp.body();
            if (body == null) return -1;
            JSONObject result = new JSONObject(body.string());
            if (result.getString("code").equals("0") && result.has("data"))
                return result.getJSONObject("data").getLong("dynamic_id");
        } catch (JSONException ignored) {
            return -1;
        }
        return -1;
    }

    /**
     * 发送复杂动态
     *
     * @param contents 动态内容
     * @param pics     携带图片
     * @param option   选项
     * @param topic    话题
     * @param scene    动态类型
     * @return 发送成功返回的动态id，失败返回-1
     */
    public static long publishComplex(@NonNull JSONArray contents, JSONArray pics, JSONObject option, JSONObject topic, int scene, Map<String, Object> otherArgs) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/dynamic/feed/create/dyn?csrf=" + SharedPreferencesUtil.getString("csrf", "");
        JSONObject reqBody = new JSONObject()
                .put("content", new JSONObject().put("contents", contents))
                .put("scene", scene)
                .put("meta", new JSONObject().put("app_meta", new JSONObject()
                        .put("from", "create.dynamic.web")
                        .put("mobi_app", "web")));
        if (pics != null) reqBody.put("pics", pics);
        if (option != null) reqBody.put("option", option);
        if (topic != null) reqBody.put("topic", topic);
        reqBody = new JSONObject().put("dyn_req", reqBody);
        if (otherArgs != null) {
            for (Map.Entry<String, Object> entry : otherArgs.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                reqBody.put(key, val);
            }
        }
        Logu.v("publishComplex reqBody=" + reqBody);
        Response resp = Objects.requireNonNull(NetWorkUtil.postJson(url, reqBody.toString()));
        try {
            ResponseBody body = resp.body();
            if (body == null) return -1;
            JSONObject result = new JSONObject(body.string());
            if (result.getString("code").equals("0") && result.has("data"))
                return result.getJSONObject("data").getLong("dyn_id");
        } catch (JSONException e) {
            MsgUtil.err("发送动态", e);
            return -1;
        }
        return -1;
    }

    /**
     * 发布可包含艾特信息的文本动态
     *
     * @param content   文本内容
     * @param atUserUid 文本内at到的人的用户名uid map
     * @return 发送成功返回的动态id，失败返回-1
     */
    public static long publishTextContent(String content, Map<String, Long> atUserUid) throws JSONException, IOException {
        return publishComplex(parseAtContent(content, atUserUid), null, null, null,
                1, null);
    }

    /**
     * 转发视频到动态，瞎扒的api
     *
     * @param text 附加文字
     * @param aid  aid
     * @return 发送成功返回的动态id，失败返回-1
     */
    public static long relayVideo(String text, Map<String, Long> atUserUid, long aid) throws JSONException, IOException {
        return publishComplex(text == null ? new JSONArray().put(Content.create("", 1, null)) : atUserUid != null ? parseAtContent(text, atUserUid) : new JSONArray().put(Content.create(text, 1, null)),
                null, null, null,
                5, Map.of("web_repost_src",
                        new JSONObject().put("revs_id", new JSONObject()
                                .put("dyn_type", 8)
                                .put("rid", aid))));
    }

    /**
     * 转发动态
     *
     * @param text 文字内容
     * @param dyid 动态id
     * @return 发送成功返回的动态id，失败返回-1
     */
    public static long relayDynamic(String text, long dyid) throws IOException {
        String url = "https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/repost";
        Response resp = Objects.requireNonNull(NetWorkUtil.post(url, new NetWorkUtil.FormData()
                .put("dynamic_id", dyid)
                .put("content", text)
                .put("csrf_token", SharedPreferencesUtil.getString("csrf", ""))
                .toString(), NetWorkUtil.webHeaders));
        try {
            ResponseBody body = resp.body();
            if (body == null) return -1;
            JSONObject result = new JSONObject(body.string());
            if (result.getString("code").equals("0") && result.has("data"))
                return result.getJSONObject("data").getLong("dynamic_id");
        } catch (JSONException ignored) {
            return -1;
        }
        return -1;
    }

    /**
     * 转发动态（复杂动态api），还是自己瞎扒的api
     *
     * @param text      文字内容
     * @param atUserUid 文本内at到的人的用户名uid map
     * @param dyid      动态id
     * @return 发送成功返回的动态id，失败返回-1
     */
    public static long relayDynamic(String text, Map<String, Long> atUserUid, long dyid) throws JSONException, IOException {
        return publishComplex(text == null ? new JSONArray().put(Content.create("", 1, null)) : atUserUid != null ? parseAtContent(text, atUserUid) : new JSONArray().put(Content.create(text, 1, null)),
                null, null, null,
                4, Map.of("web_repost_src", new JSONObject().put("dyn_id_str", String.valueOf(dyid))));
    }

    /**
     * 解析包含艾特信息的文本动态内容
     *
     * @param content   文本内容
     * @param atUserUid 文本内at到的人的用户名uid map
     * @return Content JSON数组
     */
    public static JSONArray parseAtContent(String content, Map<String, Long> atUserUid) throws JSONException {
        JSONArray contentJSONArray = new JSONArray();

        Set<Pair<Integer, Integer>> indexes = new HashSet<>();
        Map<Pair<Integer, Integer>, Long> uidIndexes = new HashMap<>();
        for (Map.Entry<String, Long> entry : atUserUid.entrySet()) {
            String key = entry.getKey();
            long val = entry.getValue();

            Pattern pattern = Pattern.compile("@" + key + " ");
            Matcher matcher = pattern.matcher(content);
            List<Pair<Integer, Integer>> mIndex = new ArrayList<>();
            while (matcher.find()) {
                int start = matcher.start();
                // 不包含空格，我直接按照我抓的请求内容弄的
                int end = matcher.end();
                Pair<Integer, Integer> pair = new Pair<>(start, end);
                mIndex.add(pair);
                uidIndexes.put(pair, val);
            }
            indexes.addAll(mIndex);
        }

        ArrayList<Pair<Integer, Integer>> indexesList = new ArrayList<>(indexes);
        int pos = 0;
        for (Pair<Integer, Integer> index : indexesList) {
            int start = index.first;
            int end = index.second;
            String sub = content.substring(pos, start);
            if (!sub.isEmpty()) contentJSONArray.put(Content.create(sub, 1, null));
            String subAt = content.substring(start, end);
            if (!subAt.isEmpty())
                contentJSONArray.put(Content.create(subAt, 2, String.valueOf(uidIndexes.get(index))));
            pos = end;
        }
        String sub = content.substring(pos);
        if (!sub.isEmpty()) contentJSONArray.put(Content.create(sub, 1, null));

        if (indexesList.isEmpty()) contentJSONArray.put(Content.create(content, 1, null));
        return contentJSONArray;
    }

    /**
     * 动态点赞/取消赞
     *
     * @param dyid 动态id
     * @param up   是否为点赞
     * @return resultCode
     */
    public static int likeDynamic(long dyid, boolean up) throws IOException {
        String url = "https://api.vc.bilibili.com/dynamic_like/v1/dynamic_like/thumb";
        Response resp = Objects.requireNonNull(NetWorkUtil.post(url, new NetWorkUtil.FormData()
                .put("dynamic_id", dyid)
                .put("up", up ? 1 : 2)
                .put("csrf_token", SharedPreferencesUtil.getString("csrf", ""))
                .toString(), NetWorkUtil.webHeaders));
        try {
            ResponseBody responseBody = resp.body();
            if (responseBody == null) return -1;
            JSONObject result = new JSONObject(responseBody.string());
            return result.getInt("code");
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int deleteDynamic(long dyid) throws IOException {
        String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/rm_dynamic";
        Response resp = Objects.requireNonNull(NetWorkUtil.post(url, new NetWorkUtil.FormData()
                .put("dynamic_id", dyid)
                .put("csrf_token", SharedPreferencesUtil.getString("csrf", ""))
                .toString(), NetWorkUtil.webHeaders));
        try {
            ResponseBody body = resp.body();
            if (body == null) return -1;
            JSONObject result = new JSONObject(body.string());
            return result.getInt("code");
        } catch (JSONException ignored) {
            return -1;
        }
    }

    /**
     * 寻找用户（完全匹配），仍然自己瞎扒的，不清楚是否有更好方案
     *
     * @param name 名称
     * @return 用户UID，未找到返回-1
     */
    public static long mentionAtFindUser(String name) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/mention/search?keyword=" + name;

        JSONObject resp = NetWorkUtil.getJson(url, NetWorkUtil.webHeaders);
        if (resp.has("data") && !resp.isNull("data")) {
            JSONObject data = resp.getJSONObject("data");
            if (data.has("groups") && !data.isNull("groups")) {
                JSONArray groups = data.getJSONArray("groups");
                for (int i = 0; i < groups.length(); i++) {
                    JSONArray items = groups.getJSONObject(i).getJSONArray("items");
                    for (int j = 0; j < items.length(); j++) {
                        if (items.getJSONObject(j).getString("name").equals(name))
                            return Long.parseLong(items.getJSONObject(j).getString("uid"));
                    }
                }
            }
        }

        return -1;
    }

    public static long getDynamicList(List<Dynamic> dynamicList, long offset, long mid, String type) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/"
                + (mid == 0 ? "all?type=" + type : "space?platform=web&web_location=333.1387&timezone_offset=-480&host_mid=" + mid)
                + (offset == 0 ? "" : "&offset=" + offset)
                + "&features=itemOpusStyle,listOnlyfans,opusBigCover,onlyfansVote,forwardListHidden,decorationCard,commentsNewVersion,onlyfansAssetsV2,ugcDelete,onlyfansQaCard,avatarAutoTheme,sunflowerStyle,eva3CardOpus,eva3CardVideo,eva3CardComment";


        JSONObject all = NetWorkUtil.getJson(ConfInfoApi.signWBI(DmImgParamUtil.getDmImgParamsUrl(url)));
        
        // 检查是否是网络错误返回的错误JSON
        if (all.optBoolean("retry_failed", false)) {
            throw new IOException(all.optString("message", "网络请求失败"));
        }
        
        int code = all.optInt("code", -1);
        if (code != 0) {
            String message = all.optString("message", "未知错误");
            throw new IOException("API错误 (code=" + code + "): " + message);
        }


        JSONObject data = all.getJSONObject("data");

        boolean has_more = data.getBoolean("has_more");
        long offset_new = (has_more ? Long.parseLong(data.getString("offset")) : -1);

        if (mid == 0) {
            long update_baseline = data.optLong("update_baseline", -1);
            if (update_baseline > -1) SharedPreferencesUtil.putLong("dynamic_update_baseline", update_baseline);
            else if (offset_new != -1) {
                SharedPreferencesUtil.putLong("dynamic_update_baseline", offset_new);
            }
        }

        JSONArray items = data.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            dynamicList.add(analyzeDynamic(items.getJSONObject(i)));
        }

        return offset_new;
    }

    public static Dynamic getDynamic(long id) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/detail?id=" + id;

        JSONObject all = NetWorkUtil.getJson(url);
        
        // 检查是否是网络错误返回的错误JSON
        if (all.optBoolean("retry_failed", false)) {
            throw new IOException(all.optString("message", "网络请求失败"));
        }
        
        int code = all.optInt("code", -1);
        if (code != 0) {
            String message = all.optString("message", "未知错误");
            throw new IOException("API错误 (code=" + code + "): " + message);
        }

        JSONObject data = all.getJSONObject("data");
        JSONObject item = data.getJSONObject("item");
        return analyzeDynamic(item);
    }

    public static int checkDynamicUpdate(String type, long updateBaseline) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/all/update?type=" + type + "&update_baseline=" + updateBaseline + "&web_location=333.1365";
        JSONObject result = NetWorkUtil.getJson(url, NetWorkUtil.webHeaders);
        
        // 检查是否是网络错误返回的错误JSON
        if (result.optBoolean("retry_failed", false)) {
            throw new IOException(result.optString("message", "网络请求失败"));
        }
        
        int code = result.optInt("code", -1);
        if (code != 0) {
            String message = result.optString("message", "未知错误");
            throw new IOException("API错误 (code=" + code + "): " + message);
        }
        if (result.has("data") && !result.isNull("data")) {
            JSONObject data = result.getJSONObject("data");
            return data.optInt("update_num", 0);
        }
        return 0;
    }

    public static Dynamic analyzeDynamic(JSONObject dynamic_json) throws JSONException {
        Logu.v("--------------");
        Dynamic dynamic = new Dynamic();

        if (!dynamic_json.isNull("id_str"))
            try {
                dynamic.dynamicId = Long.parseLong(dynamic_json.optString("id_str", "0"));
            } catch (Exception ignored) {
            }
        else {
            dynamic.dynamicId = 0;
        }
        dynamic.type = dynamic_json.optString("type");

        JSONObject basic = dynamic_json.getJSONObject("basic");
        String comment_id = basic.optString("comment_id_str", "0");
        if (!TextUtils.isEmpty(comment_id))
            try {
                dynamic.comment_id = Long.parseLong(comment_id);
            } catch (Exception ignored) {
            }
        else
            dynamic.comment_id = 0;

        dynamic.comment_type = basic.optInt("comment_type");

        Logu.v("id", String.valueOf(dynamic.dynamicId));
        Logu.v("oid", String.valueOf(dynamic.comment_id));
        Logu.v("type", dynamic.type);
        Logu.v("otype", String.valueOf(dynamic.comment_type));

        JSONObject modules = dynamic_json.getJSONObject("modules");

        //发布者
        UserInfo userInfo = new UserInfo();
        if (!modules.isNull("module_author")) {
            JSONObject module_author = modules.getJSONObject("module_author");
            userInfo.mid = module_author.getLong("mid");
            userInfo.name = module_author.getString("name");
            if (!module_author.isNull("following"))
                userInfo.followed = module_author.getBoolean("following");
            userInfo.avatar = module_author.getString("face");
            JSONObject vipJson = module_author.optJSONObject("vip");
            if (vipJson != null) {
                userInfo.vip_nickname_color = vipJson.optString("nickname_color", "");
            }
            Logu.v("sender", userInfo.name);
            dynamic.pubTime = module_author.getString("pub_time");
        }
        dynamic.userInfo = userInfo;

        if (dynamic.type.equals("DYNAMIC_TYPE_NONE")) {
            dynamic.content = "[动态不存在]";
            return dynamic;
        }

        //动态主体
        if (!modules.isNull("module_dynamic")) {
            JSONObject module_dynamic = modules.getJSONObject("module_dynamic");

            //内容
            if (!module_dynamic.isNull("desc")) {
                JSONObject desc = module_dynamic.getJSONObject("desc");
                JSONArray rich_text_nodes = desc.optJSONArray("rich_text_nodes");
                dynamic.content = analyzeTextContent(rich_text_nodes);
            } else dynamic.content = "";

            //这里面什么都有，直译为主要的
            if (!module_dynamic.isNull("major")) {
                JSONObject major = module_dynamic.getJSONObject("major");
                String major_type = major.getString("type");
                dynamic.major_type = major_type;
                Logu.d(major_type);
                switch (major_type) {
                    case "MAJOR_TYPE_ARCHIVE":
                        dynamic.major_object = analyzeVideoCard(major.getJSONObject("archive"));
                        break;
                    case "MAJOR_TYPE_UGC_SEASON":
                        dynamic.major_object = analyzeVideoCard(major.getJSONObject("ugc_season"));
                        break;
                    case "MAJOR_TYPE_PGC":
                        JSONObject bangumi = major.getJSONObject("pgc");
                        VideoCard card = new VideoCard();
                        card.type = "media_bangumi";
                        card.aid = BangumiApi.getMdidFromEpid(bangumi.getLong("epid"));
                        card.title = bangumi.getString("title");
                        card.cover = bangumi.getString("cover");
                        card.view = bangumi.getJSONObject("stat").getString("play");
                        dynamic.major_object = card;
                        break;
                    case "MAJOR_TYPE_ARTICLE":
                        JSONObject article = major.getJSONObject("article");
                        dynamic.major_object = new ArticleCard(
                                article.getString("title"),
                                article.getLong("id"),
                                (article.has("covers") && !article.isNull("covers") ? article.getJSONArray("covers").getString(0) : ""),
                                "投稿文章",
                                article.getString("label")
                        );
                        break;

                    case "MAJOR_TYPE_DRAW":
                        JSONObject draw = major.getJSONObject("draw");
                        JSONArray items = draw.getJSONArray("items");
                        ArrayList<String> picture_list = new ArrayList<>();
                        for (int i = 0; i < items.length(); i++) {
                            picture_list.add(items.getJSONObject(i).getString("src"));
                        }
                        dynamic.major_object = picture_list;
                        break;

                    case "MAJOR_TYPE_COMMON":
                        dynamic.content = dynamic.content + "\n[无法显示活动类动态的附加内容]";
                        break;

                    case "MAJOR_TYPE_LIVE_RCMD":
                        JSONObject live_rcmd = new JSONObject(major.getJSONObject("live_rcmd").getString("content")).getJSONObject("live_play_info");
                        LiveRoom room = new LiveRoom();
                        room.roomid = live_rcmd.getLong("room_id");
                        room.title = live_rcmd.getString("title");
                        room.cover = live_rcmd.getString("cover");
                        room.online = live_rcmd.getInt("online");
                        dynamic.major_object = room;
                        dynamic.content = (TextUtils.isEmpty(dynamic.content) ? "" : dynamic.content + "\n");
                        break;

                    case "MAJOR_TYPE_LIVE":
                        JSONObject live = major.getJSONObject("live");
                        LiveRoom room_card = new LiveRoom();
                        room_card.roomid = live.getLong("id");
                        room_card.title = live.getString("title");
                        room_card.cover = live.getString("cover");
                        dynamic.major_object = room_card;
                        dynamic.content = (TextUtils.isEmpty(dynamic.content) ? "" : dynamic.content + "\n");
                        break;

                    case "MAJOR_TYPE_OPUS":
                        JSONObject opusJson = major.getJSONObject("opus");

                        String title = opusJson.optString("title");
                        if (!TextUtils.isEmpty(title) && !"null".equals(title))
                            dynamic.title = title;

                        JSONArray pics = opusJson.optJSONArray("pics");
                        if (pics != null && pics.length() > 0) {
                            ArrayList<String> opusPicList = new ArrayList<>();
                            for (int i = 0; i < pics.length(); i++)
                                opusPicList.add(pics.getJSONObject(i).optString("url"));

                            dynamic.major_object = opusPicList;
                        } else {
                            // 纯文本动态，设置一个空列表避免后续处理出错
                            dynamic.major_object = new ArrayList<String>();
                        }

                        JSONObject summary = opusJson.optJSONObject("summary");
                        if (summary != null)
                            dynamic.content = analyzeTextContent(summary.optJSONArray("rich_text_nodes"));
                        else
                            dynamic.content = "";

                        break;

                    default:
                        dynamic.content = dynamic.content + "\n[*哔哩终端暂时无法查看此动态的附加内容QwQ|类型：" + major_type + "]";
                        break;
                }
            }
            if (modules.has("module_additional") && !modules.isNull("module_additional")) {
                JSONObject module_additional = modules.getJSONObject("module_additional");
                if (module_additional.getString("type").equals("ADDITIONAL_TYPE_UGC")) {
                    dynamic.major_type = "MAJOR_TYPE_ARCHIVE";
                    dynamic.major_object = analyzeVideoCard(module_additional.getJSONObject("ugc"));
                } else Logu.v("addi", module_additional.getString("type"));
            }
        }

        // 动态Stats
        if (modules.has("module_stat") && !modules.isNull("module_stat")) {
            JSONObject module_stat = modules.getJSONObject("module_stat");
            JSONObject like = module_stat.getJSONObject("like");
            Stats stats = new Stats();
            stats.like = like.getInt("count");
            stats.liked = like.getBoolean("status");
            stats.like_disabled = like.getBoolean("forbidden");
            // TODO 转发&回复

            dynamic.stats = stats;
        }

        if (modules.has("module_more") && !modules.isNull("module_more")) {
            List<String> supportItemTypes = new ArrayList<>();
            JSONArray three_point_items = modules.getJSONObject("module_more").getJSONArray("three_point_items");
            for (int i = 0; i < three_point_items.length(); i++) {
                supportItemTypes.add(three_point_items.getJSONObject(i).getString("type"));
            }
            dynamic.canDelete = supportItemTypes.contains("THREE_POINT_DELETE");
        }

        if (dynamic_json.has("orig") && !dynamic_json.isNull("orig")) {
            dynamic.dynamic_forward = analyzeDynamic(dynamic_json.getJSONObject("orig"));
        }

        return dynamic;
    }

    private static VideoCard analyzeVideoCard(JSONObject jsonObject) throws JSONException {
        return new VideoCard(
                jsonObject.getString("title"),
                "投稿视频",
                jsonObject.getJSONObject("stat").getString("play"),
                jsonObject.getString("cover"),
                Long.parseLong(jsonObject.getString("aid")),
                jsonObject.getString("bvid")
        );
    }

    private static SpannableStringBuilder analyzeTextContent(JSONArray rich_text_nodes) {
        if (rich_text_nodes == null) return new SpannableStringBuilder("[动态内容解析异常]");

        ArrayList<Emote> emoteList = new ArrayList<>();
        ArrayList<At> atList = new ArrayList<>();
        SpannableStringBuilder content = new SpannableStringBuilder();
        for (int i = 0; i < rich_text_nodes.length(); i++) {
            JSONObject rich_text_node = rich_text_nodes.optJSONObject(i);
            if (rich_text_node == null) continue;
            String type = rich_text_node.optString("type");
            switch (type) {
                case "RICH_TEXT_NODE_TYPE_EMOJI":
                    content.append(rich_text_node.optString("text"));
                    JSONObject emoji = rich_text_node.optJSONObject("emoji");
                    if (emoji == null) continue;
                    emoteList.add(new Emote(emoji.optString("text"), emoji.optString("icon_url"), emoji.optInt("size")));
                    break;
                case "RICH_TEXT_NODE_TYPE_AT":
                    Pair<Integer, Integer> indexs = StringUtil.appendString(content, rich_text_node.optString("text"));
                    atList.add(new At(rich_text_node.optLong("rid"), indexs.first, indexs.second));
                    break;
                case "RICH_TEXT_NODE_TYPE_WEB":
                    content.append(rich_text_node.optString("orig_text"));
                    break;
                case "RICH_TEXT_NODE_TYPE_TEXT":
                default:
                    content.append(rich_text_node.optString("text"));
                    break;
            }
        }

        EmoteUtil.textReplaceEmote(content.toString(), emoteList, 1.0f, BiliTerminal.context, content);
        for (At at : atList) {
            StringUtil.setSingleAt(content, at);
        }

        return content;
    }

    public static class Content {
        public static JSONObject create(@NonNull String raw_text, int type, String biz_id) throws JSONException {
            return new JSONObject()
                    .put("raw_text", raw_text)
                    .put("type", type)
                    .put("biz_id", biz_id == null ? "" : biz_id);
        }
    }
}
