package com.RobinNotBad.BiliClient.model;
import java.util.List;

public class MessageCard {
    public long id;
    public List<UserInfo> user;
    public long timeStamp = 0;
    public String timeDesc = "";
    public String content;
    public VideoCard videoCard = null;
    public Reply replyInfo = null;
    public Reply dynamicInfo = null;
    
    public MessageCard(){}
}
