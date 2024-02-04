package com.RobinNotBad.BiliClient.model;

import java.util.ArrayList;

public class Bangumi {
    public Info info;
    public ArrayList<Section> sectionList;

    public static class Info {
        public long media_id;
        public long season_id;
        public int type;
        public int count;
        public float score;
        public String title;
        public String cover;
        public String cover_horizontal;
        public String type_name;
        public String area_name;

        public Info(){}
    }

    public static class Section{
        public long id;
        public int type;
        public String title;
        public ArrayList<Episode> episodeList;

        public Section(){}
    }

    public static class Episode{
        public long id;
        public long aid;
        public long cid;
        public String title;
        public String title_long;
        public String cover;
        public String badge;//标记（如会员/限免）

        public Episode(){}
    }
}
