package com.RobinNotBad.BiliClient.model;
import java.util.List;

public class MessageCard {
    public long id;
    public List<UserInfo> user;
    public long timeStamp;
    public String content;
    public VideoCard videoCard = null;
    public Reply replyInfo = null;
    public Reply dynamicInfo = null;
    
    public MessageCard(){}
}
