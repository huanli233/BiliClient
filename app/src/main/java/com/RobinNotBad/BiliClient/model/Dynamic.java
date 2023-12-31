package com.RobinNotBad.BiliClient.model;

import org.json.JSONArray;

import java.util.ArrayList;

public class Dynamic {
    public int type;
    public long dynamicId;
    public long rid;
    public String content;
    public long userId;
    public String userName;
    public String userAvatar;
    public String pubDate;
    public int view;
    public int like;
    public int reply;
    public VideoCard childVideoCard;
    public Dynamic childDynamic;
    public ArrayList<String> pictureList;
    public JSONArray emote;
    public boolean liked;

    public Dynamic(){}
}
