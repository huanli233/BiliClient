package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;
import java.util.List;

public class Collection implements Serializable {
    public int id;
    public String title;
    public String cover;
    public String intro;
    public long mid;
    public List<Section> sections;
    public List<VideoCard> cards;
    public String view;

    public static class Section implements Serializable {
        public int season_id;
        public int id;
        public String title;
        public int type;
        public List<Episode> episodes;
    }

    public static class Episode implements Serializable {
        public int season_id;
        public int section_id;
        public long id;
        public long aid;
        public long cid;
        public String title;
        public String bvid;
        public VideoInfo arc;
    }
}
