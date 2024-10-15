package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LiveRoom implements Parcelable {
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

    public static class Verify implements Parcelable {
        public int role;
        public String desc;
        public int type;

        public Verify(){}
        protected Verify(Parcel in) {
            role = in.readInt();
            desc = in.readString();
            type = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(role);
            dest.writeString(desc);
            dest.writeInt(type);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Verify> CREATOR = new Creator<Verify>() {
            @Override
            public Verify createFromParcel(Parcel in) {
                return new Verify(in);
            }

            @Override
            public Verify[] newArray(int size) {
                return new Verify[size];
            }
        };
    }

    public static class Watched implements Parcelable {
        public boolean isSwitch;
        public int num;
        public String text_small;
        public String text_large;
        public String icon;
        public int icon_location;
        public String icon_web;

        public Watched(){}
        protected Watched(Parcel in) {
            isSwitch = in.readByte() != 0;
            num = in.readInt();
            text_small = in.readString();
            text_large = in.readString();
            icon = in.readString();
            icon_location = in.readInt();
            icon_web = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (isSwitch ? 1 : 0));
            dest.writeInt(num);
            dest.writeString(text_small);
            dest.writeString(text_large);
            dest.writeString(icon);
            dest.writeInt(icon_location);
            dest.writeString(icon_web);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Watched> CREATOR = new Creator<Watched>() {
            @Override
            public Watched createFromParcel(Parcel in) {
                return new Watched(in);
            }

            @Override
            public Watched[] newArray(int size) {
                return new Watched[size];
            }
        };
    }
    public LiveRoom(){}

    protected LiveRoom(Parcel in) {
        roomid = in.readLong();
        uid = in.readLong();
        title = in.readString();
        uname = in.readString();
        tags = in.readString();
        description = in.readString();
        online = in.readInt();
        user_cover = in.readString();
        user_cover_flag = in.readInt();
        system_cover = in.readString();
        cover = in.readString();
        keyframe = in.readString();
        show_cover = in.readString();
        face = in.readString();
        area_parent_id = in.readInt();
        area_parent_name = in.readString();
        area_id = in.readInt();
        area_name = in.readString();
        session_id = in.readString();
        group_id = in.readLong();
        show_callback = in.readString();
        click_callback = in.readString();
        liveTime = in.readString();
        verify = in.readParcelable(Verify.class.getClassLoader());
        watched_show = in.readParcelable(Watched.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(roomid);
        dest.writeLong(uid);
        dest.writeString(title);
        dest.writeString(uname);
        dest.writeString(tags);
        dest.writeString(description);
        dest.writeInt(online);
        dest.writeString(user_cover);
        dest.writeInt(user_cover_flag);
        dest.writeString(system_cover);
        dest.writeString(cover);
        dest.writeString(keyframe);
        dest.writeString(show_cover);
        dest.writeString(face);
        dest.writeInt(area_parent_id);
        dest.writeString(area_parent_name);
        dest.writeInt(area_id);
        dest.writeString(area_name);
        dest.writeString(session_id);
        dest.writeLong(group_id);
        dest.writeString(show_callback);
        dest.writeString(click_callback);
        dest.writeString(liveTime);
        dest.writeParcelable(verify, flags);
        dest.writeParcelable(watched_show, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LiveRoom> CREATOR = new Creator<LiveRoom>() {
        @Override
        public LiveRoom createFromParcel(Parcel in) {
            return new LiveRoom(in);
        }

        @Override
        public LiveRoom[] newArray(int size) {
            return new LiveRoom[size];
        }
    };
}
