package com.RobinNotBad.BiliClient.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MediaSectionInfo {
    SectionInfo mainSection;
    SectionInfo[] sections;

    public MediaSectionInfo(SectionInfo mainSection, SectionInfo[] sections){
        this.mainSection = mainSection;
        this.sections = sections;
    }

    public MediaSectionInfo(JSONObject jsonObject) throws JSONException {
        mainSection = new SectionInfo(jsonObject.getJSONObject("main_section"));
        JSONArray sectionsJson = jsonObject.getJSONArray("section");
        sections = new SectionInfo[sectionsJson.length()];
        for(int i = 0; i < sectionsJson.length(); i++){
            sections[i] = new SectionInfo(sectionsJson.getJSONObject(i));
        }
    }

    public static class EpisodeInfo{
        public EpisodeInfo(){

        }

        public EpisodeInfo(VideoCard videoCard){
            longTitle = videoCard.title;
            badge = videoCard.upName;
            cover = videoCard.cover;
            cid = videoCard.cid;
            aid = videoCard.aid;



        }
        public EpisodeInfo(JSONObject jsonObject) throws JSONException {
                aid = jsonObject.getLong("aid");
                badge = jsonObject.getString("badge");
                badgeInfo = new BadgeInfo(jsonObject.getJSONObject("badge_info"));
                badgeType = jsonObject.getInt("badge_type");
                cid = jsonObject.getLong("cid");
                cover = jsonObject.getString("cover");
                from = jsonObject.getString("from");
                epid = jsonObject.getLong("epid");
                isPremiere = jsonObject.getInt("is_premiere");
                longTitle = jsonObject.getString("long_title");
                shareUrl = jsonObject.getString("share_url");
                status = jsonObject.getInt("status");
                title = jsonObject.getString("title");
                vid = jsonObject.getString("vid");
        }
        public long aid;
        public String badge;
        public BadgeInfo badgeInfo;
        public int badgeType;
        public long cid;
        String cover;
        String from;
        long epid;
        int isPremiere;
        String longTitle;
        String shareUrl;
        int status;
        String title;
        String vid;
    }
    static class BadgeInfo{
        public BadgeInfo(){

        }
        public BadgeInfo(JSONObject jsonObject) throws JSONException {
            text = jsonObject.getString("text");
            bgColor = jsonObject.getString("bg_color");
            bgColorNight = jsonObject.getString("bg_color_night");
        }
        String text;
        String bgColor;
        String bgColorNight;
    }
    public static class SectionInfo{
        public SectionInfo(){

        }
        public SectionInfo(JSONObject jsonObject) throws JSONException {
            JSONArray episodesJson = jsonObject.getJSONArray("episodes");
            episodes = new EpisodeInfo[episodesJson.length()];
            for(int i = 0; i < episodesJson.length(); i++){
                episodes[i] = new EpisodeInfo(episodesJson.getJSONObject(i));
            }
            id = jsonObject.getLong("id");
            type = jsonObject.getInt("type");
            title = jsonObject.getString("title");
        }
        public EpisodeInfo[] episodes;
        public long id;
        public int type;
        public String title;
    }
}
