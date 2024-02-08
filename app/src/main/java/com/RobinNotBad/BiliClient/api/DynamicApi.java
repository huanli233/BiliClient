package com.RobinNotBad.BiliClient.api;

import android.util.Log;

import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.Emote;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Response;

//新的动态api，旧的那个实在太蛋疼而且说不定随时会被弃用（

public class DynamicApi {
    public static long getDynamicList(ArrayList<Dynamic> dynamicList, long offset, long mid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/"
                + (mid==0 ? "all?" : "space?host_mid=" + mid + "&")
                + (offset==0 ? "" : "offset=" + offset);

        Response response = NetWorkUtil.get(url);
        if(response.body()==null) throw new JSONException("动态返回数据为空TAT");
        JSONObject all = new JSONObject(response.body().string());
        if(all.getInt("code")!=0) throw new JSONException(all.getString("message"));
        //以后API也按照这个写吧，提供的报错信息清楚些也不会爆黄

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

        Response response = NetWorkUtil.get(url);
        if(response.body()==null) throw new JSONException("动态返回数据为空TAT");
        JSONObject all = new JSONObject(response.body().string());
        if(all.getInt("code")!=0) throw new JSONException(all.getString("message"));

        JSONObject data = all.getJSONObject("data");
        JSONObject item = data.getJSONObject("item");
        return analyzeDynamic(item);
    }

    public static Dynamic analyzeDynamic(JSONObject dynamic_json) throws JSONException {
        Dynamic dynamic = new Dynamic();
        dynamic.dynamicId = Long.parseLong(dynamic_json.getString("id_str"));
        dynamic.type = dynamic_json.getString("type");

        JSONObject basic = dynamic_json.getJSONObject("basic");
        String comment_id_str = basic.getString("comment_id_str");
        if(!comment_id_str.equals("")) dynamic.comment_id = Long.parseLong(comment_id_str);
        dynamic.comment_type = basic.getInt("comment_type");

        JSONObject modules = dynamic_json.getJSONObject("modules");
        Log.e("debug-dynamic","--------------");
        Log.e("debug-dynamic-id", String.valueOf(dynamic.dynamicId));
        Log.e("debug-dynamic-type",dynamic.type);

        //发布者
        if(modules.has("module_author") && !modules.isNull("module_author")) {
            JSONObject module_author = modules.getJSONObject("module_author");
            UserInfo userInfo = new UserInfo();
            userInfo.mid = module_author.getLong("mid");
            userInfo.name = module_author.getString("name");
            if(!module_author.isNull("following")) userInfo.followed = module_author.getBoolean("following");
            userInfo.avatar = module_author.getString("face");
            dynamic.userInfo = userInfo;
            Log.e("debug-dynamic-sender",userInfo.name);
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

                    default:
                        dynamic.content = dynamic.content + "\n[*哔哩终端暂时无法此动态的附加信息QwQ|类型：" + major_type + "]";
                }
            }

            if(modules.has("module_additional") && !modules.isNull("module_additional")){
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
}
