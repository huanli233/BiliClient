package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

public class At implements Parcelable {
    public final long rid;
    public int textStartIndex;
    public int textEndIndex;
    public String name;

    public At(long rid, int startIndex, int endIndex) {
        this.rid = rid;
        this.textStartIndex = startIndex;
        this.textEndIndex = endIndex;
    }

    public At(long rid, String name) {
        this.rid = rid;
        this.name = name;
    }

    protected At(Parcel in) {
        rid = in.readLong();
        textStartIndex = in.readInt();
        textEndIndex = in.readInt();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(rid);
        dest.writeInt(textStartIndex);
        dest.writeInt(textEndIndex);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<At> CREATOR = new Creator<At>() {
        @Override
        public At createFromParcel(Parcel in) {
            return new At(in);
        }

        @Override
        public At[] newArray(int size) {
            return new At[size];
        }
    };
}
