package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.MessageCard;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MessageApi {
    public static ArrayList<MessageCard> getLikeMsg() throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/msgfeed/like?platform=web&build=0&mobi_app=web";
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.webHeaders).body()).string());
        if(all.has("data") && !all.isNull("data")) {
            //所有消息
            ArrayList<MessageCard> totalArray = new ArrayList<>();
            for(int i = 0;i < all.getJSONObject("data").getJSONObject("total").getJSONArray("items").length();i++) {
                JSONObject object = ((JSONObject) all.getJSONObject("data").getJSONObject("total").getJSONArray("items").get(i));
                MessageCard likeInfo = new MessageCard();

                ArrayList<UserInfo> userList = new ArrayList<>();
                for (int j = 0; j < object.getJSONArray("users").length(); j++) {
                    JSONObject userArrayInfo = ((JSONObject) object.getJSONArray("users").get(j));
                    userList.add(new UserInfo(userArrayInfo.getLong("mid"), userArrayInfo.getString("nickname"), userArrayInfo.getString("avatar"), "", userArrayInfo.getInt("fans"), 0, userArrayInfo.getBoolean("follow"), ""));
                }

                likeInfo.id = object.getLong("id");
                likeInfo.user = userList;
                likeInfo.timeStamp = object.getLong("like_time");
                
                if (object.getJSONObject("item").getString("type").equals("video")) {
                    likeInfo.content = "等总共 " + userList.size() + " 人点赞了你的视频";
                    VideoCard videoCard = new VideoCard();
                    videoCard.aid = 0;
                    videoCard.bvid = object.getJSONObject("item").getString("uri").replace("https://www.bilibili.com/video/BV", "");
                    videoCard.upName = "";
                    videoCard.title = object.getJSONObject("item").getString("title");
                    videoCard.cover = object.getJSONObject("item").getString("image");
                    videoCard.view = "";
                    likeInfo.videoCard = videoCard;
                } else if (object.getJSONObject("item").getString("type").equals("reply")) {
                    likeInfo.content = "等总共 " + userList.size() + " 人点赞了你的评论";
                    Reply replyInfo = new Reply();
                    replyInfo.rpid = object.getJSONObject("item").getLong("item_id");
                    replyInfo.sender = null;
                    replyInfo.message = object.getJSONObject("item").getString("title");
                    replyInfo.emote = new JSONArray();
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
                    likeInfo.content = "等总共 " + userList.size() + " 人点赞了你的动态";
                    Reply replyInfo = new Reply();
                    replyInfo.rpid = object.getJSONObject("item").getLong("item_id");
                    replyInfo.sender = null;
                    replyInfo.message = object.getJSONObject("item").getString("title");
                    replyInfo.emote = new JSONArray();
                    replyInfo.pictureList = new ArrayList<>();
                    replyInfo.likeCount = 0;
                    replyInfo.upLiked = false;
                    replyInfo.upReplied = false;
                    replyInfo.liked = false;
                    replyInfo.isDynamic = true;
                    replyInfo.childCount = 0;
                    replyInfo.childMsgList = new ArrayList<>();
                    likeInfo.dynamicInfo = replyInfo;
                }
                totalArray.add(likeInfo);
            }

            return totalArray;
        }else return new ArrayList<>();
    }
    
    
    public static ArrayList<MessageCard> getReplyMsg() throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/msgfeed/reply?platform=web&build=0&mobi_app=web";
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.webHeaders).body()).string());
        if(all.has("data") && !all.isNull("data")) {
            ArrayList<MessageCard> totalArray = new ArrayList<>();
            for(int i = 0;i < all.getJSONObject("data").getJSONArray("items").length();i++) {
                JSONObject object = ((JSONObject) all.getJSONObject("data").getJSONArray("items").get(i));
                MessageCard replyInfo = new MessageCard();

                List<UserInfo> userList = new ArrayList<>();
                userList.add(new UserInfo(object.getJSONObject("user").getLong("mid"), object.getJSONObject("user").getString("nickname"), object.getJSONObject("user").getString("avatar"), "", object.getJSONObject("user").getInt("fans"), 0, object.getJSONObject("user").getBoolean("follow"), ""));
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
                    replyChildInfo.message = object.getJSONObject("item").getString("title");
                    replyChildInfo.emote = new JSONArray();
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
                    replyChildInfo.message = object.getJSONObject("item").getString("title");
                    replyChildInfo.emote = new JSONArray();
                    replyChildInfo.pictureList = new ArrayList<>();
                    replyChildInfo.likeCount = 0;
                    replyChildInfo.upLiked = false;
                    replyChildInfo.upReplied = false;
                    replyChildInfo.liked = false;
                    replyChildInfo.childCount = 0;
                    replyChildInfo.isDynamic = true;
                    replyChildInfo.childMsgList = new ArrayList<>();
                    replyInfo.dynamicInfo = replyChildInfo;
                }
                totalArray.add(replyInfo);
            }

            return totalArray;
        }else return new ArrayList<>();
    }
    
        
    public static ArrayList<MessageCard> getAtMsg() throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/msgfeed/at?platform=web&build=0&mobi_app=web";
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.webHeaders).body()).string());
        if(all.has("data") && !all.isNull("data")) {
            ArrayList<MessageCard> totalArray = new ArrayList<>();
            for(int i = 0;i < all.getJSONObject("data").getJSONArray("items").length();i++) {
                JSONObject object = ((JSONObject) all.getJSONObject("data").getJSONArray("items").get(i));
                MessageCard replyInfo = new MessageCard();

                List<UserInfo> userList = new ArrayList<>();
                userList.add(new UserInfo(object.getJSONObject("user").getLong("mid"), object.getJSONObject("user").getString("nickname"), object.getJSONObject("user").getString("avatar"), "", object.getJSONObject("user").getInt("fans"), 0, object.getJSONObject("user").getBoolean("follow"), ""));
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
                    replyChildInfo.message = object.getJSONObject("item").getString("title");
                    replyChildInfo.emote = new JSONArray();
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
                    replyChildInfo.message = object.getJSONObject("item").getString("title");
                    replyChildInfo.emote = new JSONArray();
                    replyChildInfo.pictureList = new ArrayList<>();
                    replyChildInfo.likeCount = 0;
                    replyChildInfo.upLiked = false;
                    replyChildInfo.upReplied = false;
                    replyChildInfo.liked = false;
                    replyChildInfo.isDynamic = true;
                    replyChildInfo.childCount = 0;
                    replyChildInfo.childMsgList = new ArrayList<>();
                    replyInfo.dynamicInfo = replyChildInfo;
                }
                totalArray.add(replyInfo);
            }

            return totalArray;
        }else return new ArrayList<>();
    }
}
