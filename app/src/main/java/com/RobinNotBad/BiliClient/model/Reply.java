package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Reply implements Parcelable, Serializable {
    public long rpid;
    public long oid;
    public long root;
    public long parent;
    public boolean forceDelete;
    public String ofBvid = "";
    public String pubTime;
    public UserInfo sender;
    public String message;
    public ArrayList<Emote> emotes = new ArrayList<>();
    public Map<String, Long> atNameToMid = new HashMap<>();
    public ArrayList<String> pictureList = new ArrayList<>();
    public int likeCount;
    public boolean upLiked;
    public boolean upReplied;
    public boolean liked;
    public int childCount;
    public boolean isDynamic;
    public ArrayList<Reply> childMsgList = new ArrayList<>();
    public boolean isTop;

    public Reply() {
    }

    protected Reply(Parcel in) {
        rpid = in.readLong();
        oid = in.readLong();
        root = in.readLong();
        parent = in.readLong();
        forceDelete = in.readByte() != 0;
        ofBvid = in.readString();
        pubTime = in.readString();
        sender = in.readParcelable(UserInfo.class.getClassLoader());
        message = in.readString();
        emotes = in.createTypedArrayList(Emote.CREATOR);
        in.readMap(atNameToMid, HashMap.class.getClassLoader());
        pictureList = in.createStringArrayList();
        likeCount = in.readInt();
        upLiked = in.readByte() != 0;
        upReplied = in.readByte() != 0;
        liked = in.readByte() != 0;
        childCount = in.readInt();
        isDynamic = in.readByte() != 0;
        childMsgList = in.createTypedArrayList(Reply.CREATOR);
        isTop = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(rpid);
        dest.writeLong(oid);
        dest.writeLong(root);
        dest.writeLong(parent);
        dest.writeByte((byte) (forceDelete ? 1 : 0));
        dest.writeString(ofBvid);
        dest.writeString(pubTime);
        dest.writeParcelable(sender, flags);
        dest.writeString(message);
        dest.writeTypedList(emotes);
        dest.writeMap(atNameToMid);
        dest.writeStringList(pictureList);
        dest.writeInt(likeCount);
        dest.writeByte((byte) (upLiked ? 1 : 0));
        dest.writeByte((byte) (upReplied ? 1 : 0));
        dest.writeByte((byte) (liked ? 1 : 0));
        dest.writeInt(childCount);
        dest.writeByte((byte) (isDynamic ? 1 : 0));
        dest.writeTypedList(childMsgList);
        dest.writeByte((byte) (isTop ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Reply> CREATOR = new Creator<>() {
        @Override
        public Reply createFromParcel(Parcel in) {
            return new Reply(in);
        }

        @Override
        public Reply[] newArray(int size) {
            return new Reply[size];
        }
    };
}
