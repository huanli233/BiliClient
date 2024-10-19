package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class ArticleInfo implements Parcelable, Serializable {
    public long id;
    public String title;
    public String summary; //摘要
    public String banner; //头图
    public UserInfo upInfo;
    public long ctime;
    public Stats stats;
    public int wordCount; //字数
    public String keywords;
    public String content; //文章内容

    public ArticleInfo() {
    }


    protected ArticleInfo(Parcel in) {
        id = in.readLong();
        title = in.readString();
        summary = in.readString();
        banner = in.readString();
        upInfo = in.readParcelable(UserInfo.class.getClassLoader());
        ctime = in.readLong();
        stats = in.readParcelable(Stats.class.getClassLoader());
        wordCount = in.readInt();
        keywords = in.readString();
        content = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(summary);
        dest.writeString(banner);
        dest.writeParcelable(upInfo, flags);
        dest.writeLong(ctime);
        dest.writeParcelable(stats, flags);
        dest.writeInt(wordCount);
        dest.writeString(keywords);
        dest.writeString(content);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ArticleInfo> CREATOR = new Creator<>() {
        @Override
        public ArticleInfo createFromParcel(Parcel in) {
            return new ArticleInfo(in);
        }

        @Override
        public ArticleInfo[] newArray(int size) {
            return new ArticleInfo[size];
        }
    };
}
