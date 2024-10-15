package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Emote implements Parcelable {
    public int id;
    public int packageId;
    public String name;
    public String alias;
    public String url;
    public int size;

    public Emote() {
    }

    public Emote(String name, String url, int size) {
        this.name = name;
        this.url = url;
        this.size = size;
    }

    protected Emote(Parcel in) {
        id = in.readInt();
        packageId = in.readInt();
        name = in.readString();
        alias = in.readString();
        url = in.readString();
        size = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(packageId);
        dest.writeString(name);
        dest.writeString(alias);
        dest.writeString(url);
        dest.writeInt(size);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Emote> CREATOR = new Creator<Emote>() {
        @Override
        public Emote createFromParcel(Parcel in) {
            return new Emote(in);
        }

        @Override
        public Emote[] newArray(int size) {
            return new Emote[size];
        }
    };
}
