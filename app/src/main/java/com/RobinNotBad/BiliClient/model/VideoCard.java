package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class VideoCard implements Parcelable, Serializable {
    public String title;
    public String upName;
    public String view;
    public String cover;
    public String type = "video";
    public long aid;
    public String bvid;
    public long cid = 0;

    // 历史记录原始字段，用于识别异常形态
    public String historyBusiness = "";
    public long historyOid = 0;
    public long historyCid = 0;
    public boolean historyPlaceholder = false;

    // 历史记录事件ID（history.kid），用于删除与去重
    public long kid = 0;

    public VideoCard(String title, String upName, String view, String cover, long aid, String bvid, String type) {
        this.title = title;
        this.upName = upName;
        this.view = view;
        this.cover = cover;
        this.aid = aid;
        this.bvid = bvid;
        this.type = type;
    }

    public VideoCard(String title, String upName, String view, String cover, long aid, String bvid, Long cid) {
        this.title = title;
        this.upName = upName;
        this.view = view;
        this.cover = cover;
        this.aid = aid;
        this.bvid = bvid;
        this.cid = cid;
    }

    public VideoCard(String title, String upName, String view, String cover, long aid, String bvid) {
        this.title = title;
        this.upName = upName;
        this.view = view;
        this.cover = cover;
        this.aid = aid;
        this.bvid = bvid;
    }

    public VideoCard() {
    }

    protected VideoCard(Parcel in) {
        title = in.readString();
        upName = in.readString();
        view = in.readString();
        cover = in.readString();
        type = in.readString();
        aid = in.readLong();
        bvid = in.readString();
        cid = in.readLong();
        historyBusiness = in.readString();
        historyOid = in.readLong();
        historyCid = in.readLong();
        historyPlaceholder = in.readByte() != 0;
    }

    public static final Creator<VideoCard> CREATOR = new Creator<>() {
        @Override
        public VideoCard createFromParcel(Parcel in) {
            return new VideoCard(in);
        }

        @Override
        public VideoCard[] newArray(int size) {
            return new VideoCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(upName);
        parcel.writeString(view);
        parcel.writeString(cover);
        parcel.writeString(type);
        parcel.writeLong(aid);
        parcel.writeString(bvid);
        parcel.writeLong(cid);
        parcel.writeString(historyBusiness);
        parcel.writeLong(historyOid);
        parcel.writeLong(historyCid);
        parcel.writeByte((byte) (historyPlaceholder ? 1 : 0));
    }
}
