package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;

public class LiveRoom implements Serializable {
    public long roomid;
    public long uid;
    public String title;
    public String uname;
    public String tags;
    public String description;
    public int online;
    public String user_cover;
    public int user_cover_flag;
    public String system_cover;
    public String cover;
    public String keyframe;
    public String show_cover;
    public String face;
    public int area_parent_id;
    public String area_parent_name;
    public int area_id;
    public String area_name;
    public String session_id;
    public long group_id;
    public String show_callback;
    public String click_callback;
    public String liveTime;
    public Verify verify;
    public Watched watched_show;

    public static class Verify implements Serializable {
        public int role;
        public String desc;
        public int type;
    }

    public static class Watched implements Serializable {
        public boolean isSwitch;
        public int num;
        public String text_small;
        public String text_large;
        public String icon;
        public int icon_location;
        public String icon_web;
    }
}
