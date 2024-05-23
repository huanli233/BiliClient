package com.RobinNotBad.BiliClient.api;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.Emote;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import okhttp3.Response;

//新的动态api，旧的那个实在太蛋疼而且说不定随时会被弃用（

public class DynamicApi {

    /**
     * 发送纯文本动态
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
                .put("csrf", SharedPreferencesUtil.getString("csrf",""))
                .toString(), NetWorkUtil.webHeaders));
        try {
            JSONObject respBody = new JSONObject(resp.body().string());
            if (respBody.getString("code").equals("0") && respBody.has("data"))
                return respBody.getJSONObject("data").getLong("dynamic_id");
        } catch (JSONException ignored) {
            return -1;
        }
        return -1;
    }

    /**
     * 发送复杂动态
     * @param contents 动态内容
     * @param pics 携带图片
     * @param option 选项
     * @param topic 话题
     * @param scene 动态类型
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
        Log.d("debug", "publishComplex reqBody=" + reqBody);
        Response resp = Objects.requireNonNull(NetWorkUtil.postJson(url, reqBody.toString()));
        try {
            JSONObject respBody = new JSONObject(resp.body().string());
            if (respBody.getString("code").equals("0") && respBody.has("data"))
                return respBody.getJSONObject("data").getLong("dyn_id");
        } catch (JSONException e) {
            Log.e("debug", "publishComplex", e);
            return -1;
        }
        return -1;
    }

    /**
     * 转发视频到动态，瞎扒的api
     * @param text 附加文字
     * @param aid aid
     * @return 发送成功返回的动态id，失败返回-1
     */
    public static long relayVideo(String text, long aid) throws JSONException, IOException {
        return publishComplex(new JSONArray().put(Content.create(text == null ? "" : text, 1, null)), null, null, null,
                5, Map.of("web_repost_src",
                new JSONObject().put("revs_id", new JSONObject()
                        .put("dyn_type", 8)
                        .put("rid", aid))));
    }

    /**
     * 转发动态
     * @param text 文字内容
     * @param dyid 动态id
     * @return 发送成功返回的动态id，失败返回-1
     */
    public static long relayDynamic(String text, long dyid) throws IOException {
        String url = "https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/repost";
        Response resp = Objects.requireNonNull(NetWorkUtil.post(url, new NetWorkUtil.FormData()
                .put("dynamic_id", dyid)
                .put("content", text)
                .put("csrf_token", SharedPreferencesUtil.getString("csrf",""))
                .toString(), NetWorkUtil.webHeaders));
        try {
            JSONObject respBody = new JSONObject(resp.body().string());
            if (respBody.getString("code").equals("0") && respBody.has("data"))
                return respBody.getJSONObject("data").getLong("dynamic_id");
        } catch (JSONException ignored) {
            return -1;
        }
        return -1;
    }

    public static long getDynamicList(ArrayList<Dynamic> dynamicList, long offset, long mid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/"
                + (mid==0 ? "all?" : "space?host_mid=" + mid)
                + (offset==0 ? "" : "&offset=" + offset);

        JSONObject all = NetWorkUtil.getJson(url);
        if(all.getInt("code")!=0) throw new JSONException(all.getString("message"));

        JSONObject data = all.getJSONObject("data");

        boolean has_more = data.getBoolean("has_more");
        long offset_new = (has_more ? Long.parseLong(data.getString("offset")) : -1);

        JSONArray items = data.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            dynamicList.add(analyzeDynamic(items.getJSONObject(i)));
        }

        return offset_new;
    }

    public static Dynamic getDynamic(long id) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/detail?id=" + id;

        JSONObject all = NetWorkUtil.getJson(url);
        if(all.getInt("code")!=0) throw new JSONException(all.getString("message"));

        JSONObject data = all.getJSONObject("data");
        JSONObject item = data.getJSONObject("item");
        return analyzeDynamic(item);
    }

    public static Dynamic analyzeDynamic(JSONObject dynamic_json) throws JSONException {
        Log.e("debug-dynamic","--------------");
        Dynamic dynamic = new Dynamic();

        if(!dynamic_json.isNull("id_str")) dynamic.dynamicId = Long.parseLong(dynamic_json.getString("id_str"));
        else dynamic.dynamicId = 0;
        Log.e("debug-dynamic-id", String.valueOf(dynamic.dynamicId));
        dynamic.type = dynamic_json.getString("type");
        Log.e("debug-dynamic-type",dynamic.type);

        JSONObject basic = dynamic_json.getJSONObject("basic");
        String comment_id_str = basic.getString("comment_id_str");
        if(!comment_id_str.equals("")) dynamic.comment_id = Long.parseLong(comment_id_str);
        dynamic.comment_type = basic.getInt("comment_type");

        JSONObject modules = dynamic_json.getJSONObject("modules");


        //发布者
        UserInfo userInfo = new UserInfo();
        if(modules.has("module_author") && !modules.isNull("module_author")) {
            JSONObject module_author = modules.getJSONObject("module_author");
            userInfo.mid = module_author.getLong("mid");
            userInfo.name = module_author.getString("name");
            if(!module_author.isNull("following")) userInfo.followed = module_author.getBoolean("following");
            userInfo.avatar = module_author.getString("face");
            Log.e("debug-dynamic-sender",userInfo.name);
            dynamic.pubTime = module_author.getString("pub_time");
        }
        dynamic.userInfo = userInfo;

        if(dynamic.type.equals("DYNAMIC_TYPE_NONE")) {
            dynamic.content = "[动态不存在]";
            return dynamic;
        }

        //动态主体
        if(modules.has("module_dynamic") && !modules.isNull("module_dynamic")){
            JSONObject module_dynamic = modules.getJSONObject("module_dynamic");

            //内容
            if(module_dynamic.has("desc") && !module_dynamic.isNull("desc")) {
                StringBuilder dynamic_content = new StringBuilder();
                ArrayList<Emote> dynamic_emotes = new ArrayList<>();

                JSONObject desc = module_dynamic.getJSONObject("desc");
                JSONArray rich_text_nodes = desc.getJSONArray("rich_text_nodes");
                for (int i = 0; i < rich_text_nodes.length(); i++) {
                    JSONObject rich_text_node = rich_text_nodes.getJSONObject(i);
                    String type = rich_text_node.getString("type");
                    switch (type){
                        case "RICH_TEXT_NODE_TYPE_TEXT":
                            dynamic_content.append(rich_text_node.getString("text"));
                            break;
                        case "RICH_TEXT_NODE_TYPE_EMOJI":
                            dynamic_content.append(rich_text_node.getString("text"));
                            JSONObject emoji = rich_text_node.getJSONObject("emoji");
                            dynamic_emotes.add(new Emote(emoji.getString("text"), emoji.getString("icon_url"), emoji.getInt("size")));
                            break;
                        default:
                            dynamic_content.append(rich_text_node.getString("text"));
                            break;
                    }
                }
                dynamic.content = dynamic_content.toString();
                Log.e("debug-dynamic-content",dynamic.content);
                dynamic.emotes = dynamic_emotes;
            }
            else dynamic.content = "";

            //这里面什么都有，直译为主要的
            if(module_dynamic.has("major") && !module_dynamic.isNull("major")){
                JSONObject major = module_dynamic.getJSONObject("major");
                String major_type = major.getString("type");
                dynamic.major_type = major_type;
                switch (major_type){
                    case "MAJOR_TYPE_ARCHIVE":
                        dynamic.major_object = analyzeVideoCard(major.getJSONObject("archive"));
                        break;
                    case "MAJOR_TYPE_UGC_SEASON":
                        dynamic.major_object = analyzeVideoCard(major.getJSONObject("ugc_season"));
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
                        dynamic.content = (TextUtils.isEmpty(dynamic.content) ? "" : dynamic.content + "\n") + "[无法显示直播类动态的附加内容]";
                        break;

                    default:
                        dynamic.content = dynamic.content + "\n[*哔哩终端暂时无法查看此动态的附加内容QwQ|类型：" + major_type + "]";
                        break;
                }
            }
            if (modules.has("module_additional") && !modules.isNull("module_additional")){
                JSONObject module_additional = modules.getJSONObject("module_additional");
                if(module_additional.getString("type").equals("ADDITIONAL_TYPE_UGC")){
                    dynamic.major_type = "MAJOR_TYPE_ARCHIVE";
                    dynamic.major_object = analyzeVideoCard(module_additional.getJSONObject("ugc"));
                }
                else Log.e("debug-dynamic-addi",module_additional.getString("type"));
            }
        }

        if(dynamic_json.has("orig") && !dynamic_json.isNull("orig")){
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

    public static class Content {
        public static JSONObject create(@NonNull String raw_text, int type, String biz_id) throws JSONException {
            return new JSONObject()
                    .put("raw_text", raw_text)
                    .put("type", type)
                    .put("biz_id", biz_id == null ? "" : biz_id);
        }
    }
}
