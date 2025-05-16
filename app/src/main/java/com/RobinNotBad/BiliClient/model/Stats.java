package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Stats implements Parcelable, Serializable {
    public int view;
    public int like;
    public int reply;
    public int coin;
    public int share;
    public int danmaku;
    public int favorite;

    public boolean followed;
    public boolean liked;
    public boolean disliked;
    public boolean favoured;
    public int coined;

    public boolean like_disabled;
    public boolean coin_disabled;
    public boolean reply_disabled;
    public boolean share_disabled;
    public int coin_limit;

    public Stats() {

    }

    protected Stats(Parcel in) {
        view = in.readInt();
        like = in.readInt();
        reply = in.readInt();
        coin = in.readInt();
        share = in.readInt();
        danmaku = in.readInt();
        favorite = in.readInt();
        followed = in.readByte() != 0;
        liked = in.readByte() != 0;
        disliked = in.readByte() != 0;
        favoured = in.readByte() != 0;
        coined = in.readInt();
        like_disabled = in.readByte() != 0;
        coin_disabled = in.readByte() != 0;
        reply_disabled = in.readByte() != 0;
        share_disabled = in.readByte() != 0;
        coin_limit = in.readInt();
    }

    public static final Creator<Stats> CREATOR = new Creator<>() {
        @Override
        public Stats createFromParcel(Parcel in) {
            return new Stats(in);
        }

        @Override
        public Stats[] newArray(int size) {
            return new Stats[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(view);
        dest.writeInt(like);
        dest.writeInt(reply);
        dest.writeInt(coin);
        dest.writeInt(share);
        dest.writeInt(danmaku);
        dest.writeInt(favorite);
        dest.writeByte((byte) (followed ? 1 : 0));
        dest.writeByte((byte) (liked ? 1 : 0));
        dest.writeByte((byte) (disliked ? 1 : 0));
        dest.writeByte((byte) (favoured ? 1 : 0));
        dest.writeInt(coined);
        dest.writeByte((byte) (like_disabled ? 1 : 0));
        dest.writeByte((byte) (coin_disabled ? 1 : 0));
        dest.writeByte((byte) (reply_disabled ? 1 : 0));
        dest.writeByte((byte) (share_disabled ? 1 : 0));
        dest.writeInt(coin_limit);
    }
}
