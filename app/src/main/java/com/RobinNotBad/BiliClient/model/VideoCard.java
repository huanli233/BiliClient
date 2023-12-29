package com.RobinNotBad.BiliClient.model;


public class VideoCard {
    public String title;
    public String upName;
    public String view;
    public String cover,type;
    public long aid;
    public String bvid;
    public VideoCard(String title, String upName, String view, String cover, long aid, String bvid,String type) {
        this.title = title;
        this.upName = upName;
        this.view = view;
        this.cover = cover;
        this.aid = aid;
        this.bvid = bvid;
        this.type = type;
    }
    public VideoCard(String title, String upName, String view, String cover, long aid, String bvid) {
        this.title = title;
        this.upName = upName;
        this.view = view;
        this.cover = cover;
        this.aid = aid;
        this.bvid = bvid;
        this.type = "video";
    }

    public VideoCard(){}
}
