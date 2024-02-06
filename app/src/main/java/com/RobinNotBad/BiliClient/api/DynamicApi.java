package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.util.Log;

import com.RobinNotBad.BiliClient.model.DynamicOld;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Response;

public class DynamicApi {

    public static final String DYNAMIC_TYPES = "268435455,1,2,4,8";

    public static JSONObject getSelfDynamic(long offset) throws IOException, JSONException {
        String url;
        if(offset==0){
            url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new?uid="
                    + SharedPreferencesUtil.getLong("mid",0) + "&type=" + DYNAMIC_TYPES;
        }else{
            url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_history?uid="
                    + SharedPreferencesUtil.getLong("mid",0)
                    + "&offset_dynamic_id=" + offset + "&type=" + DYNAMIC_TYPES;
        }
        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        return new JSONObject(Objects.requireNonNull(response.body()).string());
    }

    public static JSONObject getUserDynamic(long mid,long offset) throws IOException, JSONException {
        String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid="
                    + SharedPreferencesUtil.getLong("mid",0) + "&host_uid=" + mid
                    + "&offset_dynamic_id=" + offset + "&type=" + DYNAMIC_TYPES;
        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        return new JSONObject(Objects.requireNonNull(response.body()).string());
    }

    public static JSONObject getDynamicInfo(long id) throws IOException, JSONException {
        String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail?dynamic_id=" + id;
        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        return new JSONObject(Objects.requireNonNull(response.body()).string()).getJSONObject("data").getJSONObject("card");
    }

    public static long analyzeDynamicList(JSONObject input, ArrayList<DynamicOld> list) throws JSONException {
        JSONObject data = input.getJSONObject("data");
        JSONArray cards = data.getJSONArray("cards");
        for (int i = 0; i < cards.length(); i++) {
            list.add(analyzeDynamic(cards.getJSONObject(i)));
        }
        if(data.has("history_offset")) return data.getLong("history_offset");
        else if(data.has("next_offset")) return data.getLong("next_offset");
        else return -1;
    }

    public static DynamicOld analyzeDynamic(JSONObject dynamicCard) throws JSONException {
        DynamicOld dynamicReturn = new DynamicOld();

        JSONObject desc = dynamicCard.getJSONObject("desc");
        dynamicReturn.type = desc.getInt("type");
        dynamicReturn.dynamicId = desc.getLong("dynamic_id");
        dynamicReturn.rid = desc.getLong("rid");
        dynamicReturn.liked = desc.getInt("is_liked") == 1;
        dynamicReturn.like = (desc.has("like") ? desc.getInt("like") : 0);
        dynamicReturn.view = desc.getInt("view");
        dynamicReturn.reply = (desc.has("comment") ? desc.getInt("comment") : 0);
        Date date = new Date(desc.getLong("timestamp") * 1000);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String action = simpleDateFormat.format(date);

        JSONObject card = new JSONObject(dynamicCard.getString("card"));  //string，但是json

        if(desc.has("user_profile")) {
            JSONObject user_info = desc.getJSONObject("user_profile").getJSONObject("info");
            dynamicReturn.userId = user_info.getLong("uid");
            dynamicReturn.userName = user_info.getString("uname");
            dynamicReturn.userAvatar = user_info.getString("face");
        }

        analyzeCard(dynamicReturn,card);

        if(dynamicCard.has("display")) {
            JSONObject display = dynamicCard.getJSONObject("display");
            if(display.has("emoji_info")){
                JSONArray emoji_details = display.getJSONObject("emoji_info").getJSONArray("emoji_details");
                JSONArray emote = new JSONArray();
                for (int i = 0; i < emoji_details.length(); i++) {
                    JSONObject key = emoji_details.getJSONObject(i);
                    key.put("name",key.getString("text"));
                    key.put("url",key.getString("url"));
                    key.put("size",key.getJSONObject("meta").getInt("size"));
                    emote.put(key);
                }
                dynamicReturn.emote = emote;
            }
            if(display.has("usr_action_txt")) action = action + " | " + display.getString("usr_action_txt");
        }

        dynamicReturn.pubDate = action;

        return dynamicReturn;
    }


    public static void analyzeCard(DynamicOld dynamicReturn, JSONObject card) throws JSONException {
        int type = dynamicReturn.type;
        switch (type){
            case 1:    //分享动态
                JSONObject item_1 = card.getJSONObject("item");
                dynamicReturn.content = item_1.getString("content");

                if(card.has("origin")) {
                    JSONObject origin = new JSONObject(card.getString("origin"));
                    DynamicOld childDynamic = new DynamicOld();
                    childDynamic.type = item_1.getInt("orig_type");
                    childDynamic.dynamicId = item_1.getLong("orig_dy_id");
                    analyzeCard(childDynamic, origin);

                    JSONObject origin_user_info = card.getJSONObject("origin_user").getJSONObject("info");
                    childDynamic.userName = origin_user_info.getString("uname");
                    childDynamic.userId = origin_user_info.getLong("uid");
                    childDynamic.userAvatar = origin_user_info.getString("face");

                    dynamicReturn.childDynamic = childDynamic;

                }
                break;

            case 2:    //图文动态
                JSONObject item_2 = card.getJSONObject("item");
                dynamicReturn.content = item_2.getString("description");
                JSONArray pictures = item_2.getJSONArray("pictures");
                ArrayList<String> pictureList = new ArrayList<>();
                for (int i = 0; i < pictures.length(); i++) {
                    pictureList.add(pictures.getJSONObject(i).getString("img_src"));
                }
                dynamicReturn.pictureList = pictureList;
                break;

            case 4:    //纯文本动态
                JSONObject item_4 = card.getJSONObject("item");
                dynamicReturn.content = item_4.getString("content");
                break;

            case 8:    //投稿视频动态
                VideoCard videoCard_8 = new VideoCard();
                videoCard_8.cover = card.getString("pic");
                videoCard_8.title = card.getString("title");
                videoCard_8.aid = card.getLong("aid");
                JSONObject stat = card.getJSONObject("stat");
                videoCard_8.view = LittleToolsUtil.toWan(stat.getLong("view")) + "观看";
                JSONObject owner = card.getJSONObject("owner");
                videoCard_8.upName = owner.getString("name");
                dynamicReturn.childVideoCard = videoCard_8;
                dynamicReturn.content = card.getString("dynamic");
                break;

            case 4310:    //合集更新动态
                VideoCard videoCard_4310 = new VideoCard();
                videoCard_4310.cover = card.getString("pic");
                videoCard_4310.title = card.getString("title");
                videoCard_4310.aid = card.getLong("aid");
                JSONObject stat_4310 = card.getJSONObject("stat");
                videoCard_4310.view = LittleToolsUtil.toWan(stat_4310.getLong("view")) + "观看";
                JSONObject owner_4310 = card.getJSONObject("owner");
                videoCard_4310.upName = owner_4310.getString("name");
                dynamicReturn.childVideoCard = videoCard_4310;
                dynamicReturn.content = card.getString("dynamic");

                JSONObject collection = card.getJSONObject("collection");
                dynamicReturn.userName = "[合集]" + collection.getString("title");
                dynamicReturn.userAvatar = collection.getString("cover");
                dynamicReturn.userId = collection.getLong("mid");

                break;

            default:
                dynamicReturn.content = "[*哔哩终端暂时无法解析本类型动态，请等待后续版本OwO | 动态类型：" + type +"]";
                break;
        }

        Log.e("动态","----------------");
        if(dynamicReturn.userName!=null) Log.e("动态-发送者",dynamicReturn.userName);
        if(dynamicReturn.content!=null) Log.e("动态-内容",dynamicReturn.content);
        Log.e("动态","----------------");
    }

}
