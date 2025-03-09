package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UserInfo implements Parcelable, Serializable {
    public long mid;
    public String name;
    public String avatar;
    public String sign;
    public int fans;
    public int level;
    public int following;
    public boolean followed;
    public String notice;

    public int official;
    public String officialDesc;
    public long mtime;

    public int vip_role = 0;
    public String vip_nickname_color = "";

    public long current_exp = 0;
    public long next_exp = 0;

    public String medal_name = "";
    public int medal_level = 0;

    public String sys_notice = "";

    public LiveRoom live_room = null;

    public int is_senior_member = 0;

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc, int is_senior_member) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
        this.is_senior_member = is_senior_member;
    }

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc, String sys_notice, LiveRoom live_room, int is_senior_member) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
        this.sys_notice = sys_notice;
        this.live_room = live_room;
        this.is_senior_member = is_senior_member;
    }

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc, long current_exp, long next_exp, int is_senior_member) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
        this.current_exp = current_exp;
        this.next_exp = next_exp;
        this.is_senior_member = is_senior_member;
    }

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc, int vip_role, String sys_notice, LiveRoom live_room, int is_senior_member) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
        this.vip_role = vip_role;
        this.sys_notice = sys_notice;
        this.live_room = live_room;
        this.is_senior_member = is_senior_member;
    }

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc, long mtime, int is_senior_member) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
        this.mtime = mtime;
        this.is_senior_member = is_senior_member;
    }

    public UserInfo() {
    }
    public UserInfo(JSONObject userInfoJson) throws JSONException {
        this.level = userInfoJson.getJSONObject("level_info").getInt("current_level");
        this.mid = userInfoJson.getLong("mid");
        this.name = userInfoJson.getString("uname");
        this.avatar = userInfoJson.getString("avatar");
        this.is_senior_member = userInfoJson.getInt("is_senior_member");
        JSONObject vip = userInfoJson.getJSONObject("vip");
        this.vip_role = vip.getInt("vipStatus");
        this.vip_nickname_color = vip.getString("nickname_color");
        if ((!userInfoJson.isNull("fans_detail")) && (!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.NO_MEDAL, false))) {
            JSONObject fans_detail = userInfoJson.getJSONObject("fans_detail");
            this.medal_name = fans_detail.getString("medal_name");
            this.medal_level = fans_detail.getInt("level");
        }
    }

    protected UserInfo(Parcel in) {
        mid = in.readLong();
        name = in.readString();
        avatar = in.readString();
        sign = in.readString();
        fans = in.readInt();
        level = in.readInt();
        following = in.readInt();
        followed = in.readByte() != 0;
        notice = in.readString();
        official = in.readInt();
        officialDesc = in.readString();
        mtime = in.readLong();
        vip_role = in.readInt();
        vip_nickname_color = in.readString();
        current_exp = in.readLong();
        next_exp = in.readLong();
        medal_name = in.readString();
        medal_level = in.readInt();
        sys_notice = in.readString();
        live_room = in.readParcelable(LiveRoom.class.getClassLoader());
        is_senior_member = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mid);
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeString(sign);
        dest.writeInt(fans);
        dest.writeInt(level);
        dest.writeInt(following);
        dest.writeByte((byte) (followed ? 1 : 0));
        dest.writeString(notice);
        dest.writeInt(official);
        dest.writeString(officialDesc);
        dest.writeLong(mtime);
        dest.writeInt(vip_role);
        dest.writeString(vip_nickname_color);
        dest.writeLong(current_exp);
        dest.writeLong(next_exp);
        dest.writeString(medal_name);
        dest.writeInt(medal_level);
        dest.writeString(sys_notice);
        dest.writeParcelable(live_room, flags);
        dest.writeInt(is_senior_member);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserInfo> CREATOR = new Creator<>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}
