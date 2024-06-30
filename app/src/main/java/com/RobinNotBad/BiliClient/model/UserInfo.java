package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class UserInfo implements Serializable, Parcelable {
    public long mid;
    public String name;
    public String avatar;
    public String sign;
    public int fans;
    public int level;
    public int following;
    public boolean followed;
    public String notice;

    public int official;
    public String officialDesc;
    public long mtime;

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
    }

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc, long mtime) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
        this.mtime = mtime;
    }

    public UserInfo() {
    }

    protected UserInfo(Parcel in) {
        mid = in.readLong();
        name = in.readString();
        avatar = in.readString();
        sign = in.readString();
        fans = in.readInt();
        level = in.readInt();
        followed = in.readInt() != 0;
        notice = in.readString();
        official = in.readInt();
        officialDesc = in.readString();
        mtime = in.readLong();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mid);
        parcel.writeString(name);
        parcel.writeString(avatar);
        parcel.writeString(sign);
        parcel.writeInt(fans);
        parcel.writeInt(level);
        parcel.writeInt((followed ? 1 : 0));
        parcel.writeString(notice);
        parcel.writeInt(official);
        parcel.writeString(officialDesc);
        parcel.writeLong(mtime);
    }
}
