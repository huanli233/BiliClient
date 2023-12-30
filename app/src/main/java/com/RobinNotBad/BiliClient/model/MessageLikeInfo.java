package com.RobinNotBad.BiliClient.model;

import java.util.List;

public class MessageLikeInfo {
    public long id;
    public List<UserInfo> userList;
    public int count;
    public long timeStamp;
    public String type;
    public VideoCard videoCard = null;
    public Reply replyInfo = null;

    public MessageLikeInfo(){}
}
