package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Reply implements Serializable {
    public long rpid;
    public long oid;
    public long root;
    public long parent;
    public boolean forceDelete;
    public String ofBvid = "";
    public String pubTime;
    public UserInfo sender;
    public String message;
    public ArrayList<Emote> emotes;
    public Map<String, Long> atNameToMid;
    public ArrayList<String> pictureList;
    public int likeCount;
    public boolean upLiked;
    public boolean upReplied;
    public boolean liked;
    public int childCount;
    public boolean isDynamic;
    public ArrayList<String> childMsgList;
    public boolean isTop;

    public Reply(){}
}
