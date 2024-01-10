package com.RobinNotBad.BiliClient.activity.video.info.factory;

import androidx.appcompat.app.AppCompatActivity;

public class DetailInfoFactory {
    public static DetailPage get(AppCompatActivity activity, Data data) {
        switch (data.type) {
            case "video":
                return new VideoDetailInfo(activity, data.bvid, data.aid);
            case "media":
                return new MediaDetailInfo(activity, data.mediaId);
            default:
                return null;
        }
    }
    public static class Data {
        private String type = "video";
        private String bvid;
        private long aid;

        private long mediaId;
        public Data(){

        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getBvid() {
            return bvid;
        }

        public void setBvid(String bvid) {
            this.bvid = bvid;
        }

        public long getAid() {
            return aid;
        }

        public void setAid(long aid) {
            this.aid = aid;
        }
        public void setMediaId(long mediaId){
            this.mediaId = mediaId;
        }
    }
}

