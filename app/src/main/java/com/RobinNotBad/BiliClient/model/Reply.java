package com.RobinNotBad.BiliClient.model;

import org.json.JSONArray;

import java.util.ArrayList;

public class Reply {
    public long rpid;
    public String ofBvid = "";
    public String pubTime;
    public UserInfo sender;
    public String message;
    public JSONArray emote;
    public ArrayList<String> pictureList;
    public int likeCount;
    public boolean upLiked;
    public boolean upReplied;
    public boolean liked;
    public int childCount;
    public boolean isDynamic = false;
    public ArrayList<String> childMsgList;

    public Reply(){}
}
