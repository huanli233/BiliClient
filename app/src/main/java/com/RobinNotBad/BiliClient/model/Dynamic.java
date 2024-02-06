package com.RobinNotBad.BiliClient.model;

import org.json.JSONArray;

public class Dynamic {
    public String dynamicId;
    public String type;
    public String comment_id;
    public int comment_type;

    public UserInfo userInfo;
    public String content;
    public String pubDate;

    public int view;
    public int like;
    public int reply;
    public JSONArray emote;
    public boolean liked;

    public Dynamic(){}
}
