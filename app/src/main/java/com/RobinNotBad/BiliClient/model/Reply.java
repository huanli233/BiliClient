package com.RobinNotBad.BiliClient.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.RobinNotBad.BiliClient.util.JsonUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static android.webkit.ConsoleMessage.MessageLevel.TIP;
import static com.RobinNotBad.BiliClient.api.ReplyApi.TOP_TIP;

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

    /**
     *
     * @param isRoot 是否是根评论
     * @param replyJson 评论json对象
     * @throws JSONException json解析异常
     */
    public Reply(boolean isRoot, JSONObject replyJson) throws JSONException {
        this.rpid = replyJson.getLong("rpid");
        this.oid = replyJson.getLong("oid");
        this.root = replyJson.getLong("root");
        this.parent = replyJson.getLong("parent");
        this.sender = new UserInfo(replyJson.getJSONObject("member"));
        JSONObject content = replyJson.getJSONObject("content");
        this.message = ToolsUtil.htmlToString(content.getString("message"));
        this.likeCount = replyJson.getInt("like");
        this.likeCount = replyJson.getInt("like");
        this.liked = replyJson.getInt("action") == 1;

        if (content.has("emote") && !content.isNull("emote")) {
            ArrayList<Emote> emoteList = new ArrayList<>();
            JSONObject emoteJson = content.getJSONObject("emote");
            ArrayList<String> emoteKeys = JsonUtil.getJsonKeys(emoteJson);

            for (String emoteKey : emoteKeys) {
                JSONObject key = emoteJson.getJSONObject(emoteKey);
                emoteList.add(new Emote(
                        emoteKey,
                        key.getString("url"),
                        key.getJSONObject("meta").getInt("size")
                ));
            }
            this.emotes = emoteList;
        }

        if (content.has("at_name_to_mid") && !content.isNull("at_name_to_mid")) {
            Map<String, Long> atNameToMid = new HashMap<>();
            JSONObject jsonObject = content.getJSONObject("at_name_to_mid");
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                long val = jsonObject.getLong(key);
                atNameToMid.put(key, val);
            }
            this.atNameToMid = atNameToMid;
        }

        JSONObject upAction = replyJson.getJSONObject("up_action");
        this.upLiked = upAction.getBoolean("like");
        this.upReplied = upAction.getBoolean("reply");

        JSONObject replyCtrl = replyJson.getJSONObject("reply_control");
        long ctime = replyJson.getLong("ctime") * 1000;

        String time;
        if (System.currentTimeMillis() - ctime < 3 * 24 * 60 * 60 * 1000 && replyCtrl.has("time_desc")) {
            time = replyCtrl.getString("time_desc");
        } else {
            time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.SIMPLIFIED_CHINESE).format(ctime);
        }

        if (replyCtrl.has("location")) {
            time += " | IP:" + replyCtrl.getString("location").substring(5);  //这字符串还是切割一下吧不然太长了，只留个地址，前缀去了
        }
        this.pubTime = time;

        if (replyCtrl.has("is_up_top")) {
            if (replyCtrl.getBoolean("is_up_top")) {
                this.message = TOP_TIP + this.message;
                this.isTop = true;
            }
        }

        if (isRoot) {
            if (content.has("pictures") && !content.isNull("pictures")) {
                ArrayList<String> pictureList = new ArrayList<>();
                JSONArray pictures = content.getJSONArray("pictures");
                for (int j = 0; j < pictures.length(); j++) {
                    JSONObject picture = pictures.getJSONObject(j);
                    pictureList.add(picture.getString("img_src"));
                }
                this.pictureList = pictureList;
            }

            this.childCount = replyJson.getInt("rcount");

            if (replyJson.has("replies") && !replyJson.isNull("replies")) {
                ArrayList<Reply> childMsgList = new ArrayList<>();
                JSONArray childReplies = replyJson.getJSONArray("replies");
                for (int j = 0; j < childReplies.length(); j++) {
                    JSONObject childReply = childReplies.getJSONObject(j);
                    childMsgList.add(new Reply(false, childReply));
                }
                this.childMsgList = childMsgList;
            }
        }
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
