package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Collection implements Parcelable {
    public int id;
    public String title;
    public String cover;
    public String intro;
    public long mid;
    public List<Section> sections;
    public List<VideoCard> cards;
    public String view;

    public static class Section implements Parcelable {
        public int season_id;
        public int id;
        public String title;
        public int type;
        public List<Episode> episodes;
        public Section(){}

        protected Section(Parcel in) {
            season_id = in.readInt();
            id = in.readInt();
            title = in.readString();
            type = in.readInt();
            episodes = in.createTypedArrayList(Episode.CREATOR);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(season_id);
            dest.writeInt(id);
            dest.writeString(title);
            dest.writeInt(type);
            dest.writeTypedList(episodes);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Section> CREATOR = new Creator<Section>() {
            @Override
            public Section createFromParcel(Parcel in) {
                return new Section(in);
            }

            @Override
            public Section[] newArray(int size) {
                return new Section[size];
            }
        };
    }

    public static class Episode implements Parcelable {
        public int season_id;
        public int section_id;
        public long id;
        public long aid;
        public long cid;
        public String title;
        public String bvid;
        public VideoInfo arc;

        public Episode(){}

        protected Episode(Parcel in) {
            season_id = in.readInt();
            section_id = in.readInt();
            id = in.readLong();
            aid = in.readLong();
            cid = in.readLong();
            title = in.readString();
            bvid = in.readString();
            arc = in.readParcelable(VideoInfo.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(season_id);
            dest.writeInt(section_id);
            dest.writeLong(id);
            dest.writeLong(aid);
            dest.writeLong(cid);
            dest.writeString(title);
            dest.writeString(bvid);
            dest.writeParcelable(arc, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Episode> CREATOR = new Creator<Episode>() {
            @Override
            public Episode createFromParcel(Parcel in) {
                return new Episode(in);
            }

            @Override
            public Episode[] newArray(int size) {
                return new Episode[size];
            }
        };
    }
    public Collection(){}
    protected Collection(Parcel in) {
        id = in.readInt();
        title = in.readString();
        cover = in.readString();
        intro = in.readString();
        mid = in.readLong();
        sections = in.createTypedArrayList(Section.CREATOR);
        cards = in.createTypedArrayList(VideoCard.CREATOR);
        view = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(cover);
        dest.writeString(intro);
        dest.writeLong(mid);
        dest.writeTypedList(sections);
        dest.writeTypedList(cards);
        dest.writeString(view);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Collection> CREATOR = new Creator<Collection>() {
        @Override
        public Collection createFromParcel(Parcel in) {
            return new Collection(in);
        }

        @Override
        public Collection[] newArray(int size) {
            return new Collection[size];
        }
    };
}
