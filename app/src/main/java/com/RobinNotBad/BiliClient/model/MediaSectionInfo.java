package com.RobinNotBad.BiliClient.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MediaSectionInfo {
    public SectionInfo mainSection;
    public SectionInfo[] sections;

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
        public EpisodeInfo(){}

        public EpisodeInfo(VideoCard videoCard){
            title = videoCard.title;
            cover = videoCard.cover;
            cid = videoCard.cid;
            aid = videoCard.aid;
        }
        public EpisodeInfo(JSONObject jsonObject) throws JSONException {
                aid = jsonObject.getLong("aid");
                cid = jsonObject.getLong("cid");
                cover = jsonObject.getString("cover");
                title = jsonObject.getString("long_title");
        }
        public long aid;
        public long cid;
        public String cover;
        public String title;
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
            title = jsonObject.getString("title");
        }
        public EpisodeInfo[] episodes;
        public long id;
        public String title;
    }
}
