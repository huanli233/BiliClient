package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Series implements Parcelable, Serializable {
    public String type = "series";
    public int id;
    public String title;
    public String cover;
    public String intro;
    public long mid;
    public String total;

    public Series() {
    }

    protected Series(Parcel in) {
        type = in.readString();
        id = in.readInt();
        title = in.readString();
        cover = in.readString();
        intro = in.readString();
        mid = in.readLong();
        total = in.readString();
    }

    public static final Creator<Series> CREATOR = new Creator<Series>() {
        @Override
        public Series createFromParcel(Parcel in) {
            return new Series(in);
        }

        @Override
        public Series[] newArray(int size) {
            return new Series[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(cover);
        dest.writeString(intro);
        dest.writeLong(mid);
        dest.writeString(total);
    }
}
