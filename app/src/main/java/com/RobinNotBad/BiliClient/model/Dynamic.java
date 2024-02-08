package com.RobinNotBad.BiliClient.model;

import java.util.ArrayList;

public class Dynamic {
    public long dynamicId;
    public String type;
    public long comment_id;
    public int comment_type;

    public UserInfo userInfo;
    public String content;
    public ArrayList<Emote> emotes;
    public String pubDate;

    public int view;
    public int like;
    public int reply;
    public boolean liked;

    public String major_type;
    public Object major_object;
    public Dynamic dynamic_forward;

    public Dynamic(){}
}
