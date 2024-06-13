package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.MessageCard;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageApi {
    public static JSONObject getUnread() throws IOException, JSONException{
        String url = "https://api.bilibili.com/x/msgfeed/unread";
        JSONObject all = NetWorkUtil.getJson(url);
        JSONObject jsonObject = new JSONObject();
        if(all.has("data") && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            jsonObject.put("at",data.getInt("at"));
            jsonObject.put("like",data.getInt("like"));
            jsonObject.put("reply",data.getInt("reply"));
            jsonObject.put("system",data.getInt("sys_msg"));
        }else{
            jsonObject.put("at",0);
            jsonObject.put("like",0);
            jsonObject.put("reply",0);
            jsonObject.put("system",0);
        }
        return jsonObject;
    }
    public static ArrayList<MessageCard> getLikeMsg() throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/msgfeed/like?platform=web&build=0&mobi_app=web";
        JSONObject all = NetWorkUtil.getJson(url);
        if(all.has("data") && !all.isNull("data")) {
            //所有消息
            ArrayList<MessageCard> totalArray = new ArrayList<>();
            for(int i = 0;i < all.getJSONObject("data").getJSONObject("total").getJSONArray("items").length();i++) {
                JSONObject object = ((JSONObject) all.getJSONObject("data").getJSONObject("total").getJSONArray("items").get(i));
                MessageCard likeInfo = new MessageCard();

                ArrayList<UserInfo> userList = new ArrayList<>();
                for (int j = 0; j < object.getJSONArray("users").length(); j++) {
                    JSONObject userArrayInfo = ((JSONObject) object.getJSONArray("users").get(j));
                    userList.add(new UserInfo(userArrayInfo.getLong("mid"), userArrayInfo.getString("nickname"), userArrayInfo.getString("avatar"), "", userArrayInfo.getInt("fans"), 0, userArrayInfo.getBoolean("follow"), "",0,""));
                }

                likeInfo.id = object.getLong("id");
                likeInfo.user = userList;
                likeInfo.timeStamp = object.getLong("like_time");
                
                if (object.getJSONObject("item").getString("type").equals("video")) {
                    likeInfo.content = "等总共 " + object.getLong("counts") + " 人点赞了你的视频";
                    VideoCard videoCard = new VideoCard();
                    videoCard.aid = 0;
                    videoCard.bvid = object.getJSONObject("item").getString("uri").replace("https://www.bilibili.com/video/BV", "");
                    videoCard.upName = "";
                    videoCard.title = object.getJSONObject("item").getString("title");
                    videoCard.cover = object.getJSONObject("item").getString("image");
                    videoCard.view = "";
                    likeInfo.videoCard = videoCard;
                } else if (object.getJSONObject("item").getString("type").equals("reply")) {
                    likeInfo.content = "等总共 " + object.getLong("counts") + " 人点赞了你的评论";
                    Reply replyInfo = new Reply();
                    replyInfo.rpid = object.getJSONObject("item").getLong("item_id");
                    replyInfo.sender = null;
                    replyInfo.message = object.getJSONObject("item").getString("title");
                    replyInfo.emotes = new ArrayList<>();
                    replyInfo.pictureList = new ArrayList<>();
                    replyInfo.likeCount = 0;
                    replyInfo.upLiked = false;
                    replyInfo.upReplied = false;
                    replyInfo.liked = false;
                    replyInfo.childCount = 0;
                    replyInfo.ofBvid = object.getJSONObject("item").getString("uri").replace("https://www.bilibili.com/video/","");
                    replyInfo.childMsgList = new ArrayList<>();
                    likeInfo.replyInfo = replyInfo;
                } else if (object.getJSONObject("item").getString("type").equals("dynamic")) {
                    likeInfo.content = "等总共 " + object.getLong("counts") + " 人点赞了你的动态";
                    Reply replyInfo = new Reply();
                    replyInfo.rpid = object.getJSONObject("item").getLong("item_id");
                    replyInfo.sender = null;
                    replyInfo.message = object.getJSONObject("item").getString("title");
                    replyInfo.emotes = new ArrayList<>();
                    replyInfo.pictureList = new ArrayList<>();
                    replyInfo.likeCount = 0;
                    replyInfo.upLiked = false;
                    replyInfo.upReplied = false;
                    replyInfo.liked = false;
                    replyInfo.isDynamic = true;
                    replyInfo.childCount = 0;
                    replyInfo.childMsgList = new ArrayList<>();
                    likeInfo.dynamicInfo = replyInfo;
                } else if (object.getJSONObject("item").getString("type").equals("article")) {
                    likeInfo.content = "等总共 " + object.getLong("counts") + " 人点赞了你的专栏";
                    // 实在是抽象 但是我没时间改那么多
                    Reply replyChildInfo = new Reply();
                    replyChildInfo.rpid = object.getJSONObject("item").getLong("target_id");
                    replyChildInfo.message = object.getJSONObject("item").getString("title");
                    replyChildInfo.childCount = 0;
                    likeInfo.replyInfo = replyChildInfo;
                }
                JSONObject item = object.getJSONObject("item");
                likeInfo.businessId = item.getInt("business_id");
                likeInfo.subjectId = item.getLong("item_id");
                likeInfo.itemType = item.getString("type");

                likeInfo.getType = MessageCard.GET_TYPE_LIKE;
                totalArray.add(likeInfo);
            }

            return totalArray;
        }else return new ArrayList<>();
    }
    
    
    public static ArrayList<MessageCard> getReplyMsg() throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/msgfeed/reply?platform=web&build=0&mobi_app=web";
        JSONObject all = NetWorkUtil.getJson(url);
        if(all.has("data") && !all.isNull("data")) {
            ArrayList<MessageCard> totalArray = new ArrayList<>();
            for(int i = 0;i < all.getJSONObject("data").getJSONArray("items").length();i++) {
                JSONObject object = ((JSONObject) all.getJSONObject("data").getJSONArray("items").get(i));
                MessageCard replyInfo = new MessageCard();

                List<UserInfo> userList = new ArrayList<>();
                userList.add(new UserInfo(object.getJSONObject("user").getLong("mid"), object.getJSONObject("user").getString("nickname"), object.getJSONObject("user").getString("avatar"), "", object.getJSONObject("user").getInt("fans"), 0, object.getJSONObject("user").getBoolean("follow"), "",0,""));
                replyInfo.user = userList;
                
                replyInfo.id = object.getLong("id");
                replyInfo.timeStamp = object.getLong("reply_time");
                replyInfo.content = object.getJSONObject("item").getString("source_content");
                
                if (object.getJSONObject("item").getString("type").equals("video")) {
                    VideoCard videoCard = new VideoCard();
                    videoCard.aid = 0;
                    videoCard.bvid = object.getJSONObject("item").getString("uri").replace("https://www.bilibili.com/video/BV", "");
                    videoCard.upName = "";
                    videoCard.title = object.getJSONObject("item").getString("title");
                    videoCard.cover = object.getJSONObject("item").getString("image");
                    videoCard.view = "";
                    replyInfo.videoCard = videoCard;
                } else if (object.getJSONObject("item").getString("type").equals("reply")) {
                    Reply replyChildInfo = new Reply();
                    replyChildInfo.rpid = object.getJSONObject("item").getLong("target_id");
                    replyChildInfo.sender = null;
                    replyChildInfo.message = "[评论] " + object.getJSONObject("item").getString("title");
                    replyChildInfo.emotes = new ArrayList<>();
                    replyChildInfo.pictureList = new ArrayList<>();
                    replyChildInfo.likeCount = 0;
                    replyChildInfo.upLiked = false;
                    replyChildInfo.upReplied = false;
                    replyChildInfo.liked = false;
                    replyChildInfo.childCount = 0;
                    replyChildInfo.ofBvid = object.getJSONObject("item").getString("uri").replace("https://www.bilibili.com/video/","");
                    replyChildInfo.childMsgList = new ArrayList<>();
                    replyInfo.replyInfo = replyChildInfo;
                } else if (object.getJSONObject("item").getString("type").equals("dynamic")) {
                    Reply replyChildInfo = new Reply();
                    replyChildInfo.rpid = object.getJSONObject("item").getLong("target_id");
                    replyChildInfo.sender = null;
                    replyChildInfo.message = "[动态] " + object.getJSONObject("item").getString("title");
                    replyChildInfo.emotes = new ArrayList<>();
                    replyChildInfo.pictureList = new ArrayList<>();
                    replyChildInfo.likeCount = 0;
                    replyChildInfo.upLiked = false;
                    replyChildInfo.upReplied = false;
                    replyChildInfo.liked = false;
                    replyChildInfo.childCount = 0;
                    replyChildInfo.isDynamic = true;
                    replyChildInfo.childMsgList = new ArrayList<>();
                    replyInfo.dynamicInfo = replyChildInfo;
                } else if (object.getJSONObject("item").getString("type").equals("article")) {
                    // TODO 实在是抽象 但是我没时间改那么多
                    Reply replyChildInfo = new Reply();
                    replyChildInfo.rpid = object.getJSONObject("item").getLong("target_id");
                    replyChildInfo.message = "[专栏] " + object.getJSONObject("item").getString("title");
                    replyChildInfo.childCount = 0;
                    replyInfo.replyInfo = replyChildInfo;
                }
                JSONObject item = object.getJSONObject("item");
                replyInfo.businessId = item.getInt("business_id");
                replyInfo.subjectId = item.getLong("subject_id");
                replyInfo.itemType = item.getString("type");
                replyInfo.getType = MessageCard.GET_TYPE_REPLY;

                totalArray.add(replyInfo);
            }

            return totalArray;
        }else return new ArrayList<>();
    }


    public static ArrayList<MessageCard> getAtMsg() throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/msgfeed/at?platform=web&build=0&mobi_app=web";
        JSONObject all = NetWorkUtil.getJson(url);
        if(all.has("data") && !all.isNull("data")) {
            ArrayList<MessageCard> totalArray = new ArrayList<>();
            for(int i = 0;i < all.getJSONObject("data").getJSONArray("items").length();i++) {
                JSONObject object = ((JSONObject) all.getJSONObject("data").getJSONArray("items").get(i));
                MessageCard replyInfo = new MessageCard();

                List<UserInfo> userList = new ArrayList<>();
                userList.add(new UserInfo(object.getJSONObject("user").getLong("mid"), object.getJSONObject("user").getString("nickname"), object.getJSONObject("user").getString("avatar"), "", object.getJSONObject("user").getInt("fans"), 0, object.getJSONObject("user").getBoolean("follow"), "",0,""));
                replyInfo.user = userList;

                replyInfo.id = object.getLong("id");
                replyInfo.timeStamp = object.getLong("at_time");
                replyInfo.content = "提到了我";

                if (object.getJSONObject("item").getString("type").equals("video")) {
                    VideoCard videoCard = new VideoCard();
                    videoCard.aid = 0;
                    videoCard.bvid = object.getJSONObject("item").getString("uri").replace("https://www.bilibili.com/video/BV", "");
                    videoCard.upName = "";
                    videoCard.title = object.getJSONObject("item").getString("title");
                    videoCard.cover = object.getJSONObject("item").getString("image");
                    videoCard.view = "";
                    replyInfo.videoCard = videoCard;
                } else if (object.getJSONObject("item").getString("type").equals("reply")) {
                    Reply replyChildInfo = new Reply();
                    replyChildInfo.rpid = object.getJSONObject("item").getLong("target_id");
                    replyChildInfo.sender = null;
                    replyChildInfo.message = "[评论] " + object.getJSONObject("item").getString("title");
                    replyChildInfo.emotes = new ArrayList<>();
                    replyChildInfo.pictureList = new ArrayList<>();
                    replyChildInfo.likeCount = 0;
                    replyChildInfo.upLiked = false;
                    replyChildInfo.upReplied = false;
                    replyChildInfo.liked = false;
                    replyChildInfo.childCount = 0;
                    replyChildInfo.ofBvid = object.getJSONObject("item").getString("uri").replace("https://www.bilibili.com/video/","");
                    replyChildInfo.childMsgList = new ArrayList<>();
                    replyInfo.replyInfo = replyChildInfo;
                } else if (object.getJSONObject("item").getString("type").equals("dynamic")) {
                    Reply replyChildInfo = new Reply();
                    replyChildInfo.rpid = object.getJSONObject("item").getLong("target_id");
                    replyChildInfo.sender = null;
                    replyChildInfo.message = "[动态] " + object.getJSONObject("item").getString("title");
                    replyChildInfo.emotes = new ArrayList<>();
                    replyChildInfo.pictureList = new ArrayList<>();
                    replyChildInfo.likeCount = 0;
                    replyChildInfo.upLiked = false;
                    replyChildInfo.upReplied = false;
                    replyChildInfo.liked = false;
                    replyChildInfo.isDynamic = true;
                    replyChildInfo.childCount = 0;
                    replyChildInfo.childMsgList = new ArrayList<>();
                    replyInfo.dynamicInfo = replyChildInfo;
                } else if (object.getJSONObject("item").getString("type").equals("article")) {
                    Reply replyChildInfo = new Reply();
                    replyChildInfo.rpid = object.getJSONObject("item").getLong("target_id");
                    replyChildInfo.message = "[专栏] " + object.getJSONObject("item").getString("title");
                    replyChildInfo.childCount = 0;
                    replyInfo.replyInfo = replyChildInfo;
                }
                JSONObject item = object.getJSONObject("item");
                replyInfo.businessId = item.getInt("business_id");
                replyInfo.subjectId = item.getLong("subject_id");
                replyInfo.itemType = item.getString("type");
                replyInfo.getType = MessageCard.GET_TYPE_AT;

                totalArray.add(replyInfo);
            }

            return totalArray;
        }else return new ArrayList<>();
    }

    public static ArrayList<MessageCard> getSystemMsg() throws IOException, JSONException {
        String url = "https://message.bilibili.com/x/sys-msg/query_user_notify?csrf=" + NetWorkUtil.getInfoFromCookie("bili_jct", SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""))  + "&page_size=35&build=0&mobi_app=web";
        JSONObject all = NetWorkUtil.getJson(url);
        if(all.has("data") && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            ArrayList<MessageCard> totalArray = new ArrayList<>();
            if(data.has("system_notify_list") && !data.isNull("system_notify_list")){
                for(int i = 0;i < data.getJSONArray("system_notify_list").length();i++) {
                    JSONObject object = data.getJSONArray("system_notify_list").getJSONObject(i);
                    MessageCard replyInfo = new MessageCard();
    
                    replyInfo.user = new ArrayList<>();
    
                    replyInfo.id = object.getLong("id");
                    replyInfo.timeDesc = object.getString("time_at");
                    replyInfo.content = object.getString("title") + "\n" + object.getString("content");
    
                    totalArray.add(replyInfo);
                }
            }

            return totalArray;
        }else return new ArrayList<>();
    }
}
