package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Dynamic implements Parcelable {
    public final static String DYNAMIC_TYPE_UGC_SEASON = "DYNAMIC_TYPE_UGC_SEASON";
    public long dynamicId;
    public String type;
    public long comment_id;
    public int comment_type;

    public UserInfo userInfo;
    public String content;
    public ArrayList<Emote> emotes;
    public String pubTime;

    public Stats stats;

    public String major_type;
    public Object major_object;
    public Dynamic dynamic_forward;
    public List<At> ats;
    public boolean canDelete;

    public Dynamic() {
    }

    protected Dynamic(Parcel in) {
        dynamicId = in.readLong();
        type = in.readString();
        comment_id = in.readLong();
        comment_type = in.readInt();
        userInfo = in.readParcelable(UserInfo.class.getClassLoader());
        content = in.readString();
        emotes = in.createTypedArrayList(Emote.CREATOR);
        pubTime = in.readString();
        major_type = in.readString();
        dynamic_forward = in.readParcelable(Dynamic.class.getClassLoader());
        ats = in.createTypedArrayList(At.CREATOR);
        canDelete = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(dynamicId);
        dest.writeString(type);
        dest.writeLong(comment_id);
        dest.writeInt(comment_type);
        dest.writeParcelable(userInfo, flags);
        dest.writeString(content);
        dest.writeTypedList(emotes);
        dest.writeString(pubTime);
        dest.writeString(major_type);
        dest.writeParcelable(dynamic_forward, flags);
        dest.writeTypedList(ats);
        dest.writeByte((byte) (canDelete ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Dynamic> CREATOR = new Creator<Dynamic>() {
        @Override
        public Dynamic createFromParcel(Parcel in) {
            return new Dynamic(in);
        }

        @Override
        public Dynamic[] newArray(int size) {
            return new Dynamic[size];
        }
    };
}
