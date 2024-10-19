package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmotePackage implements Parcelable, Serializable {
    public int id;
    public String text;
    public String url;
    public int type;
    public int attr;
    public int size;
    public int item_id;
    public List<Emote> emotes = new ArrayList<>();
    public boolean permanent;

    public EmotePackage() {

    }

    protected EmotePackage(Parcel in) {
        id = in.readInt();
        text = in.readString();
        url = in.readString();
        type = in.readInt();
        attr = in.readInt();
        size = in.readInt();
        item_id = in.readInt();
        emotes = in.createTypedArrayList(Emote.CREATOR);
        permanent = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(text);
        dest.writeString(url);
        dest.writeInt(type);
        dest.writeInt(attr);
        dest.writeInt(size);
        dest.writeInt(item_id);
        dest.writeTypedList(emotes);
        dest.writeByte((byte) (permanent ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EmotePackage> CREATOR = new Creator<>() {
        @Override
        public EmotePackage createFromParcel(Parcel in) {
            return new EmotePackage(in);
        }

        @Override
        public EmotePackage[] newArray(int size) {
            return new EmotePackage[size];
        }
    };
}
