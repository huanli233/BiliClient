package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ArticleInfo implements Serializable, Parcelable {
    public long id;
    public String title;
    public String summary; //摘要
    public String banner; //头图
    public long upMid;
    public String upName;
    public String upAvatar;
    public int upFans;
    public int upLevel;
    public long ctime;
    public String view;
    public int favourite;
    public int like;
    public int reply;
    public int share;
    public int coin;
    public int wordCount; //字数
    public boolean isLike;
    public String keywords;
    public String content; //文章内容

    public ArticleInfo(){}

    protected ArticleInfo(Parcel in) {
        id = in.readLong();
        title = in.readString();
        summary = in.readString();
        banner = in.readString();
        upMid = in.readLong();
        upName = in.readString();
        upAvatar = in.readString();
        upFans = in.readInt();
        upLevel = in.readInt();
        ctime = in.readLong();
        view = in.readString();
        favourite = in.readInt();
        like = in.readInt();
        reply = in.readInt();
        share = in.readInt();
        coin = in.readInt();
        wordCount = in.readInt();
        isLike = in.readInt() != 0;
        keywords = in.readString();
        content = in.readString();
    }

    public static final Creator<ArticleInfo> CREATOR = new Creator<ArticleInfo>() {
        @Override
        public ArticleInfo createFromParcel(Parcel in) {
            return new ArticleInfo(in);
        }

        @Override
        public ArticleInfo[] newArray(int size) {
            return new ArticleInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(summary);
        parcel.writeString(banner);
        parcel.writeLong(upMid);
        parcel.writeString(upName);
        parcel.writeString(upAvatar);
        parcel.writeInt(upFans);
        parcel.writeInt(upLevel);
        parcel.writeLong(ctime);
        parcel.writeInt(favourite);
        parcel.writeInt(like);
        parcel.writeInt(reply);
        parcel.writeInt(share);
        parcel.writeInt(coin);
        parcel.writeInt((isLike ? 1 : 0));
        parcel.writeString(keywords);
        parcel.writeString(content);
    }
}
