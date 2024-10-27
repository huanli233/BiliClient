package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SubtitleLink implements Parcelable {
    long id;
    String lang;
    String url;

    public SubtitleLink(long id, String lang, String url) {
        this.id = id;
        this.lang = lang;
        this.url = url;
    }

    protected SubtitleLink(Parcel in) {
        id = in.readLong();
        lang = in.readString();
        url = in.readString();
    }

    public static final Creator<SubtitleLink> CREATOR = new Creator<>() {
        @Override
        public SubtitleLink createFromParcel(Parcel in) {
            return new SubtitleLink(in);
        }

        @Override
        public SubtitleLink[] newArray(int size) {
            return new SubtitleLink[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(lang);
        dest.writeString(url);
    }
}
