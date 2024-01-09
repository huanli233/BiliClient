package com.RobinNotBad.BiliClient.api;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.model.PrivateMsgSession;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PrivateMsgApi {

    public static final int MSG_TYPE_TEXT = 1;
    public static final int MSG_TYPE_PIC = 2;
    public static final int MSG_TYPE_RETRACT = 5;

    // 返回的是倒序的消息列表，使用时记得列表倒置
    public static JSONObject getPrivateMsg(long talkerId, int size)
            throws IOException, JSONException {
        String url =
                "https://api.vc.bilibili.com/svr_sync/v1/svr_sync/fetch_session_msgs?session_type=1&talker_id="
                        + talkerId
                        + "&size="
                        + size;
        JSONObject allMsgJson =
                new JSONObject(
                        Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.webHeaders).body())
                                .string());
        if (allMsgJson.has("data") && !allMsgJson.isNull("data"))
            return allMsgJson.getJSONObject("data");
        else return new JSONObject();
    }

    public static ArrayList<PrivateMessage> getPrivateMsgList(JSONObject allMsgJson)
            throws IOException, JSONException {
        ArrayList<PrivateMessage> list = new ArrayList<>();
        JSONArray messages = allMsgJson.getJSONArray("messages");
        UserInfo myInfo = UserInfoApi.getCurrentUserInfo();
        UserInfo targetInfo = new UserInfo();

        // list.add(new PrivateMessage(114514,1,new JSONObject("{\"content\":\"
        // 。\"}"),11111111,"aaaaa",111));

        boolean isReqTargetInfo = false;
        if (messages != null) {
            for (int i = 0; i < messages.length(); i++) {
                PrivateMessage msgObject = new PrivateMessage();
                JSONObject msgJson = messages.getJSONObject(i);
                msgObject.uid = msgJson.getLong("sender_uid");
                msgObject.type = msgJson.getInt("msg_type");
                if (msgObject.uid == myInfo.mid) {
                    msgObject.name = myInfo.name;
                } else {
                    if (!isReqTargetInfo) {
                        targetInfo = UserInfoApi.getUserInfo(msgObject.uid);
                        isReqTargetInfo = true;
                    }
                    msgObject.name = targetInfo.name;
                }
                msgObject.content = new JSONObject("{\"content\":\" .\"}"); // 防止内容不为json时解析错误
                if (msgJson.getString("content").endsWith("}")
                        && msgJson.getString("content").startsWith("{")) {
                    msgObject.content =
                            new JSONObject(msgJson.getString("content") /*.replace("\\","")*/);
                }
                msgObject.timestamp = msgJson.getLong("timestamp");
                msgObject.msgId = msgJson.getLong("msg_key");
                boolean isPuted = list.add(msgObject);
                Log.e("puted?", String.valueOf(isPuted));
                Log.e(
                        "msg",
                        msgObject.name
                                + "."
                                + msgObject.uid
                                + "."
                                + msgObject.msgId
                                + "."
                                + msgObject.timestamp
                                + "."
                                + msgObject.content
                                + "."
                                + msgObject.type);
            }
            Log.e("", "返回msgList");
            for (PrivateMessage i : list) {
                Log.e(
                        "msg",
                        i.name
                                + "."
                                + i.uid
                                + "."
                                + i.msgId
                                + "."
                                + i.timestamp
                                + "."
                                + i.content
                                + "."
                                + i.type);
            }
            return list;
        } else return new ArrayList<PrivateMessage>();
    }

    public static JSONArray getEmoteJsonArray(JSONObject allMsgJson) throws JSONException {
        if (allMsgJson.has("e_infos") && !allMsgJson.isNull("e_infos")) {
            return allMsgJson.getJSONArray("e_infos");
        } else {
            return new JSONArray();
        }
    }

    public static HashMap<Long, UserInfo> getUsersInfo(ArrayList<Long> uidList)
            throws IOException, JSONException {
        String userString = "";
        for (Long uid : uidList) {
            userString = userString + uid + ",";
        }
        String url =
                "https://api.vc.bilibili.com/account/v1/user/cards?uids="
                        + userString.substring(0, userString.length() - 1);
        HashMap<Long, UserInfo> userMap = new HashMap<>();
        JSONObject root =
                new JSONObject(
                        Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.webHeaders).body())
                                .string());
        if (root.has("data") && !root.isNull("data")) {
            JSONArray data = root.getJSONArray("data");
            for (int i = 0; i < data.length(); ++i) {
                JSONObject userJson = data.getJSONObject(i);
                UserInfo user = new UserInfo();
                user.mid = userJson.getLong("mid");
                user.name = userJson.getString("name");
                user.avatar = userJson.getString("face");
                Log.e("", user.mid + user.name + user.avatar);
                userMap.put(user.mid, user);
            }
        }
        return userMap;
    }

    public static ArrayList<PrivateMsgSession> getSessionsList(int size)
            throws IOException, JSONException {
        String url =
                "https://api.vc.bilibili.com/session_svr/v1/session_svr/get_sessions?session_type=1&size="
                        + size;
        JSONObject root =
                new JSONObject(
                        Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.webHeaders).body())
                                .string());
        ArrayList<PrivateMsgSession> sessionList = new ArrayList<>();
        if (root.has("data") && !root.isNull("data")) {
            JSONArray sessions = root.getJSONObject("data").getJSONArray("session_list");
            for (int i = 0; i < sessions.length(); ++i) {
                PrivateMsgSession session = new PrivateMsgSession();
                JSONObject sessionJson = sessions.getJSONObject(i);
                session.talkerUid = sessionJson.getLong("talker_id");
                session.contentType = sessionJson.getJSONObject("last_msg").getInt("msg_type");
                session.content = new JSONObject("{\"content\":\" .\"}");
                if (sessionJson.getJSONObject("last_msg").getString("content").endsWith("}")
                        && sessionJson
                                .getJSONObject("last_msg")
                                .getString("content")
                                .startsWith("{")) {
                    session.content =
                            new JSONObject(
                                    sessionJson
                                            .getJSONObject("last_msg")
                                            .getString("content") /*.replace("\\","")*/);
                }
                session.unread = sessionJson.getInt("unread_count");
                if (!sessionJson.has("account_info") && sessionJson.isNull("account_info")) {
                    sessionList.add(session);
                }
            }
        }
        return sessionList;
    }

    public static int sendMsg(
            long senderUid, long receiverUid, int msgType, long timestamp, String content)
            throws IOException, JSONException {
        String url = "https://api.vc.bilibili.com/web_im/v1/web_im/send_msg";
        String per =
                "dev_id="
                        + getDevId()
                        + "&msg_type="
                        + msgType
                        + "&content="
                        + content
                        + "&receiver_type=1&csrf="
                        + SharedPreferencesUtil.getString("csrf", "")
                        + "&sender_id="
                        + senderUid
                        + "&receiver_id"
                        + receiverUid
                        + "&timestamp="
                        + System.currentTimeMillis();

        JSONObject result =
                new JSONObject(
                        Objects.requireNonNull(
                                        NetWorkUtil.post(url, per, ConfInfoApi.webHeaders).body())
                                .string());
        Log.e("debug-发送私信", result.toString());
        return result.getInt("code");
    }

    private static String getDevId() {
        char[] b = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] s = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".toCharArray();
        for (int i = 0; i < s.length; i++) {
            if ('-' == s[i] || '4' == s[i]) {
                continue;
            }
            int randomInt = (int) (16 * Math.random());
            if ('x' == s[i]) {
                s[i] = b[randomInt];
            } else {
                s[i] = b[3 & randomInt | 8];
            }
        }
        return new String(s);
    }

    // 由于接口特殊性定制的textReplaceEmote
    public static SpannableString textReplaceEmote(
            String text, JSONArray emote, float scale, Context context)
            throws JSONException, ExecutionException, InterruptedException {
        SpannableString result = new SpannableString(text);
        if (emote != null && emote.length() > 0) {
            for (int i = 0; i < emote.length(); i++) { // 遍历每一个表情包
                JSONObject key = emote.getJSONObject(i);

                String name = key.getString("text");
                String emoteUrl = key.getString("url");
                int size = key.getInt("size"); // B站十分贴心的帮你把表情包大小都写好了，快说谢谢蜀黍

                Drawable drawable =
                        Glide.with(context)
                                .asDrawable()
                                .load(emoteUrl)
                                .submit()
                                .get(); // 获得url并通过glide得到一张图片

                drawable.setBounds(
                        0,
                        0,
                        (int) (size * LittleToolsUtil.sp2px(18, context) * scale),
                        (int)
                                (size
                                        * LittleToolsUtil.sp2px(18, context)
                                        * scale)); // 参考了隔壁腕上哔哩并进行了改进

                int start = text.indexOf(name); // 检测此字符串的起始位置
                while (start >= 0) {
                    int end = start + name.length(); // 计算得出结束位置
                    ImageSpan imageSpan =
                            new ImageSpan(
                                    drawable,
                                    ImageSpan
                                            .ALIGN_BOTTOM); // 获得一个imagespan
                                                            // 这句不能放while上面，imagespan不可以复用，我也不知道为什么
                    result.setSpan(
                            imageSpan,
                            start,
                            end,
                            SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE); // 替换
                    start = text.indexOf(name, end); // 重新检测起始位置，直到找不到，然后开启下一个循环
                }
            }
        }
        return result;
    }
}
