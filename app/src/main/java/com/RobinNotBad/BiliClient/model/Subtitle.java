package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Subtitle implements Parcelable {
    String content;
    float from;
    float to;

    public Subtitle(String content, float from, float to) {
        this.content = content;
        this.from = from;
        this.to = to;
    }

    protected Subtitle(Parcel in) {
        content = in.readString();
        from = in.readFloat();
        to = in.readFloat();
    }

    public static final Creator<Subtitle> CREATOR = new Creator<Subtitle>() {
        @Override
        public Subtitle createFromParcel(Parcel in) {
            return new Subtitle(in);
        }

        @Override
        public Subtitle[] newArray(int size) {
            return new Subtitle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeFloat(from);
        dest.writeFloat(to);
    }
}
