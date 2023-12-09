package com.RobinNotBad.BiliClient.model;

public class UserInfo {
    public long mid;
    public String name;
    public String avatar;
    public String sign;
    public int fans;
    public int level;
    public boolean followed;

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int level, boolean followed) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.followed = followed;
    }

    public UserInfo(){}
}
