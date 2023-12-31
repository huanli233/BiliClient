package com.RobinNotBad.BiliClient.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class VideoInfo implements Parcelable, Serializable {    //自定义类需要加这个才能传输
    public String bvid;
    public long aid;
    public String title;
    public String upName;
    public String cover;
    public String description;
    public String upAvatar;
    public String duration;
    public long upMid;
    public int view;
    public int like;
    public int coin;
    public int reply;
    public String timeDesc;
    public String tagsDesc;
    public int danmaku;
    public int favorite;
    public ArrayList<String> pagenames;
    public ArrayList<Integer> cids;


    public VideoInfo(){}


    protected VideoInfo(Parcel in) {
        bvid = in.readString();
        aid = in.readLong();
        title = in.readString();
        upName = in.readString();
        cover = in.readString();
        description = in.readString();
        upAvatar = in.readString();
        duration = in.readString();
        upMid = in.readLong();
        view = in.readInt();
        like = in.readInt();
        coin = in.readInt();
        reply = in.readInt();
        timeDesc = in.readString();
        tagsDesc = in.readString();
        danmaku = in.readInt();
        favorite = in.readInt();
        pagenames = in.createStringArrayList();
    }


    public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
        @Override
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(bvid);
        dest.writeLong(aid);
        dest.writeString(title);
        dest.writeString(upName);
        dest.writeString(cover);
        dest.writeString(description);
        dest.writeString(upAvatar);
        dest.writeString(duration);
        dest.writeLong(upMid);
        dest.writeInt(view);
        dest.writeInt(like);
        dest.writeInt(coin);
        dest.writeInt(reply);
        dest.writeString(timeDesc);
        dest.writeString(tagsDesc);
        dest.writeInt(danmaku);
        dest.writeInt(favorite);
        dest.writeStringList(pagenames);
    }
}
