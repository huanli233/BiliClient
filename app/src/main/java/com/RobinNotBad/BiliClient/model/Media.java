package com.RobinNotBad.BiliClient.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Media {
    public String cover;
    public String horizontalCover;
    public long mediaId;

    public NewEp newEP;

    public Rating rating;

    public long seasonId;
    public String shareUrl;

    public String title;

    public MediaType type;

    public String typeName;

    public Media(JSONObject jsonObject) throws JSONException {
        jsonObject = jsonObject.getJSONObject("media");
        this.cover = jsonObject.getString("cover");
        this.horizontalCover = jsonObject.getString("horizontal_picture");
        this.mediaId = jsonObject.getLong("media_id");
        switch (jsonObject.getInt("type")) {
            case 1:
                this.type = MediaType.BANGUMI;
                break;
            case 2:
                this.type = MediaType.MOVIE;
                break;
            case 3:
                this.type = MediaType.RECORD;
                break;
            case 4:
                this.type = MediaType.CHINA_MADE;
                break;
            case 5:
                this.type = MediaType.TV;
                break;
            case 6:
                this.type = MediaType.MANGA;
                break;
        }
        this.seasonId = jsonObject.getLong("season_id");
        this.shareUrl = jsonObject.getString("share_url");
        this.typeName = jsonObject.getString("type_name");
        this.newEP = new NewEp(jsonObject.getJSONObject("new_ep"));
        this.rating = new Rating(jsonObject.getJSONObject("rating"));
    }

    enum MediaType {
        BANGUMI,
        MOVIE,
        RECORD,
        CHINA_MADE,
        TV,
        MANGA,

    }

    class NewEp {
        public NewEp(JSONObject jsonObject) throws JSONException {
            this.epid = jsonObject.getLong("id");
            this.index = jsonObject.getString("index");
            this.indexShow = jsonObject.getString("index_show");
        }

        public NewEp(long epid, String index, String indexShow) {
            this.epid = epid;
            this.index = index;
            this.indexShow = indexShow;
        }

        public long epid;
        public String index;
        public String indexShow;
    }

    class Rating {
        public Rating(JSONObject jsonObject) throws JSONException {
            this.count = jsonObject.getLong("count");
            this.Score = jsonObject.getLong("score");
        }

        public Rating(Long count, float score) {
            this.count = count;
            Score = score;
        }

        public Long count;
        public float Score;
    }
}
