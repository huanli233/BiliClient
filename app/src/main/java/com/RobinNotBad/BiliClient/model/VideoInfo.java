package com.RobinNotBad.BiliClient.model;


import java.io.Serializable;
import java.util.ArrayList;

public class VideoInfo implements Serializable {    //自定义类需要加这个才能传输
    public String bvid;
    public long aid;
    public String title;
    public String upName;
    public String cover;
    public String description;
    public String upAvatar;
    public long upMid;
    public int view;
    public int like;
    public int coin;
    public int reply;
    public String timeDesc;
    public int danmaku;
    public int favorite;
    public ArrayList<String> pagenames;
    public ArrayList<Integer> cids;


    public VideoInfo(){}


}
