package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.MessageLikeInfo;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageApi {
    public static JSONObject getLikeMsg() throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/msgfeed/like?platform=web&build=0&mobi_app=web";
        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.webHeaders).body()).string());
        if(all.has("data") && !all.isNull("data")) {
            JSONObject jsonObject = new JSONObject();

            //未读消息
            List<MessageLikeInfo> latestArray = new ArrayList<>();
            for(int i = 0;i < all.getJSONObject("data").getJSONObject("latest").getJSONArray("items").length();i++){
                JSONObject object = ((JSONObject) all.getJSONObject("data").getJSONObject("latest").getJSONArray("items").get(i));
                MessageLikeInfo likeInfo = new MessageLikeInfo();

                List<UserInfo> userList = new ArrayList<UserInfo>();
                for (int j = 0;j < object.getJSONArray("users").length();j++){
                    JSONObject userArrayInfo = ((JSONObject) object.getJSONArray("users").get(j));
                    userList.add(new UserInfo(userArrayInfo.getLong("mid"),userArrayInfo.getString("nickname"),userArrayInfo.getString("avatar"),"",userArrayInfo.getInt("fans"),0,userArrayInfo.getBoolean("follow"),""));
                }

                likeInfo.id = object.getLong("id");
                likeInfo.userList = userList;
                likeInfo.count = object.getInt("counts");
                likeInfo.timeStamp = object.getLong("like_time");
                likeInfo.type = object.getJSONObject("item").getString("type");
                if(likeInfo.type.equals("video")) {
                    VideoCard videoCard = new VideoCard();
                    videoCard.aid = 0;
                    videoCard.bvid = object.getJSONObject("item").getString("uri").replace("https://www.bilibili.com/video/","");
                    videoCard.upName = "";
                    videoCard.title = object.getJSONObject("item").getString("title");
                    videoCard.cover = object.getJSONObject("item").getString("image");
                    videoCard.view = String.valueOf(likeInfo.count);
                    likeInfo.videoCard = videoCard;
                }else if(likeInfo.type.equals("reply")){
                    Reply replyInfo = new Reply();
                    replyInfo.rpid = object.getJSONObject("item").getLong("id");
                    replyInfo.sender = null;
                    replyInfo.message = object.getJSONObject("item").getString("title");
                    replyInfo.emote = new JSONArray();
                    replyInfo.pictureList = new ArrayList<>();
                    replyInfo.likeCount = likeInfo.count;
                    replyInfo.upLiked = false;
                    replyInfo.upReplied = false;
                    replyInfo.liked = false;
                    replyInfo.childCount = 0;
                    replyInfo.childMsgList = new ArrayList<>();
                    likeInfo.replyInfo = replyInfo;
                }
                latestArray.add(likeInfo);
            }


            //所有消息
            List<MessageLikeInfo> totalArray = new ArrayList<>();
            for(int i = 0;i < all.getJSONObject("data").getJSONObject("total").getJSONArray("items").length();i++) {
                JSONObject object = ((JSONObject) all.getJSONObject("data").getJSONObject("total").getJSONArray("items").get(i));
                MessageLikeInfo likeInfo = new MessageLikeInfo();

                List<UserInfo> userList = new ArrayList<UserInfo>();
                for (int j = 0; j < object.getJSONArray("users").length(); j++) {
                    JSONObject userArrayInfo = ((JSONObject) object.getJSONArray("users").get(j));
                    userList.add(new UserInfo(userArrayInfo.getLong("mid"), userArrayInfo.getString("nickname"), userArrayInfo.getString("avatar"), "", userArrayInfo.getInt("fans"), 0, userArrayInfo.getBoolean("follow"), ""));
                }

                likeInfo.id = object.getLong("id");
                likeInfo.userList = userList;
                likeInfo.count = object.getInt("counts");
                likeInfo.timeStamp = object.getLong("like_time");
                likeInfo.type = object.getJSONObject("item").getString("type");
                if (likeInfo.type.equals("video")) {
                    VideoCard videoCard = new VideoCard();
                    videoCard.aid = 0;
                    videoCard.bvid = object.getJSONObject("item").getString("uri").replace("https://www.bilibili.com/video/BV", "");
                    videoCard.upName = "";
                    videoCard.title = object.getJSONObject("item").getString("title");
                    videoCard.cover = object.getJSONObject("item").getString("image");
                    videoCard.view = "";
                    likeInfo.videoCard = videoCard;
                } else if (likeInfo.type.equals("reply")) {
                    Reply replyInfo = new Reply();
                    replyInfo.rpid = object.getJSONObject("item").getLong("item_id");
                    replyInfo.sender = null;
                    replyInfo.message = object.getJSONObject("item").getString("title");
                    replyInfo.emote = new JSONArray();
                    replyInfo.pictureList = new ArrayList<>();
                    replyInfo.likeCount = likeInfo.count;
                    replyInfo.upLiked = false;
                    replyInfo.upReplied = false;
                    replyInfo.liked = false;
                    replyInfo.childCount = 0;
                    replyInfo.childMsgList = new ArrayList<>();
                    likeInfo.replyInfo = replyInfo;
                }
                totalArray.add(likeInfo);
            }

            jsonObject.put("latest",latestArray);
            jsonObject.put("total",totalArray);
            return jsonObject;
        }else return new JSONObject();
    }
}
