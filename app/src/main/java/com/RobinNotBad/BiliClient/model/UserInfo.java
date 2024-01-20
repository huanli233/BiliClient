package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;

public class UserInfo implements Serializable {
    public long mid;
    public String name;
    public String avatar;
    public String sign;
    public int fans;
    public int level;
    public boolean followed;
    public String notice;

    public int official;
    public String officialDesc;

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int level, boolean followed, String notice, int official, String officialDesc) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
    }

    public UserInfo(){}
}
