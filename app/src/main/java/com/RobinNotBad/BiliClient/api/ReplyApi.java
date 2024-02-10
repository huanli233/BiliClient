package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.util.Log;

import com.RobinNotBad.BiliClient.model.Emote;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.JsonUtil;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

//腕上哔哩那边注释里写了一连串的麻烦麻烦麻烦，顿时预感不妙
//其实还好
//用Log直接替代注释了，应该都能看懂吧
//2023-07-22

public class ReplyApi {

    public static int getReplies(long oid, long rpid, int pn, int type, ArrayList<Reply> replyArrayList) throws JSONException, IOException {
        String sort = rpid == 0 ? "2" : "0";

        String url = "http://api.bilibili.com/x/v2/reply" + (rpid == 0 ? "" : "/reply") + "?pn=" + pn
                + "&type=" + type + "&oid=" + oid + "&sort=" + sort + (rpid == 0 ? "" : ("&root=" + rpid));

        Log.e("debug-评论区链接", url);

        JSONObject all = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.webHeaders).body()).string());

        Log.e("debug-评论区",all.toString());

        int size = replyArrayList.size();
        if (all.getInt("code") == 0 && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            JSONObject page = data.getJSONObject("page");
            if (!data.isNull("replies") && page.getInt("size") > 0) {
                if (rpid == 0 && data.has("top_replies") && page.getInt("num") == 1)
                    analizeReplyArray(true, data.getJSONArray("top_replies"), replyArrayList);
                JSONArray replies = data.getJSONArray("replies");
                analizeReplyArray(rpid == 0, replies, replyArrayList);
                if(replyArrayList.size() == size)return 1;
                else return 0;
            } else return 1;
        } else return -1;
    }  //-1错误,0正常，1到底了

    public static void analizeReplyArray(boolean isRoot, JSONArray replies,ArrayList<Reply> replyArrayList) throws JSONException {
        long timeCurr = System.currentTimeMillis();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (int i = 0; i < replies.length(); i++) {
            Reply replyReturn = new Reply();

            Log.e("debug-第" + i + "条评论", "--------");
            JSONObject reply = replies.getJSONObject(i);
            long rpid = reply.getLong("rpid");    //这玩意不能用int，太长了数值溢出！！！有被坑到！！！
            Log.e("debug-评论id", String.valueOf(rpid));
            replyReturn.rpid = rpid;
            JSONObject member = reply.getJSONObject("member");
            String uname = member.getString("uname");
            long mid = member.getLong("mid");
            String avatar = member.getString("avatar");
            int level = member.getJSONObject("level_info").getInt("current_level");

            UserInfo userInfo = new UserInfo();
            userInfo.level = level;
            userInfo.mid = mid;
            userInfo.name = uname;
            userInfo.avatar = avatar;
            replyReturn.sender = userInfo;    //发送者

            JSONObject content = reply.getJSONObject("content");
            String message = LittleToolsUtil.htmlToString(content.getString("message"));
            replyReturn.message = message;
            Log.e("debug-评论内容", message);

            int likeCount = reply.getInt("like");
            Log.e("debug-点赞数", String.valueOf(likeCount));
            replyReturn.likeCount = likeCount;
            replyReturn.liked = reply.getInt("action") == 1;

            if(content.has("emote") && !content.isNull("emote")) {
                ArrayList<Emote> emoteList = new ArrayList<>();
                JSONObject emoteJson = content.getJSONObject("emote");
                Log.e("debug-emote",emoteJson.toString());
                ArrayList<String> emoteKeys = JsonUtil.getJsonKeys(emoteJson);
                //LsonObject lsonEmote = LsonUtil.parseAsObject(emote.toString());    //最终还是用了Lson的一个功能，因为原生方式确实难以解决，还好这个功能是github版已有的
                //String[] emoteKeys = lsonEmote.getKeys();                           //你看这多简单，唉，但是仅用这一处却引用整个库确实有些浪费，如果我有时间就自己写了。就当是致敬了罢（
                //最终结果是自己写了函数，luern的库彻底扔掉了
                for (String emoteKey:emoteKeys) {
                    Log.e("debug-key",emoteKey);
                    JSONObject key = emoteJson.getJSONObject(emoteKey);
                    emoteList.add(new Emote(
                            emoteKey,
                            key.getString("url"),
                            key.getJSONObject("meta").getInt("size")
                    ));
                }
                replyReturn.emote = emoteList;
            }

            //表情包列表 不知道咋办就直接传json了  显示部分见EmoteUtil

            JSONObject upAction = reply.getJSONObject("up_action");
            replyReturn.upLiked = upAction.getBoolean("like");
            replyReturn.upReplied = upAction.getBoolean("reply");

            //up主觉得很淦

            JSONObject replyCtrl = reply.getJSONObject("reply_control");
            long ctime = reply.getLong("ctime") * 1000;
            Log.e("debug-评论时间戳", String.valueOf(ctime));

            String timeStr;
            if(timeCurr - ctime < 259200000 && replyCtrl.has("time_desc")) timeStr = replyCtrl.getString("time_desc");  //大于3天变成日期
            else timeStr = sdf.format(ctime);

            if(replyCtrl.has("location")) timeStr += " | IP:" + replyCtrl.getString("location").substring(5);  //这字符串还是切割一下吧不然太长了，只留个地址，前缀去了

            if(replyCtrl.has("is_up_top")){
                if(replyCtrl.getBoolean("is_up_top")) replyReturn.message = "[置顶]" + replyReturn.message;
            }
            Log.e("debug-时间和IP",timeStr);
            replyReturn.pubTime = timeStr;

            if (isRoot) {
                if (content.has("pictures") && !content.isNull("pictures")) {
                    ArrayList<String> pictureList = new ArrayList<>();
                    JSONArray pictures = content.getJSONArray("pictures");
                    for (int j = 0; j < pictures.length(); j++) {
                        JSONObject picture = pictures.getJSONObject(j);
                        Log.e("debug-第" + j + "张图片", picture.getString("img_src"));
                        pictureList.add(picture.getString("img_src"));
                    }
                    replyReturn.pictureList = pictureList;
                }

                replyReturn.childCount = reply.getInt("rcount");

                if (reply.has("replies") && !reply.isNull("replies")) {
                    ArrayList<String> childMsgList = new ArrayList<>();
                    JSONArray childReplies = reply.getJSONArray("replies");
                    for (int j = 0; j < childReplies.length(); j++) {
                        JSONObject childReply = childReplies.getJSONObject(j);
                        String childUName = childReply.getJSONObject("member").getString("uname");
                        String childMessage = childReply.getJSONObject("content").getString("message");
                        Log.e("debug-第" + j + "条子评论", childUName + "：" + childMessage);
                        childMsgList.add(childUName + "：" + childMessage);
                    }
                    replyReturn.childMsgList = childMsgList;
                }
            }
            replyArrayList.add(replyReturn);
        }
    }


    public static int sendReply(long oid, long root, long parent, String text) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/reply/add";
        String arg = "oid=" + oid + "&type=1" + (root == 0 ? "" : ("&root=" + root + "&parent=" + parent))
                + "&message=" + text + "&jsonp=jsonp&csrf=" + SharedPreferencesUtil.getString("csrf","");
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, arg, ConfInfoApi.webHeaders).body()).string());
        Log.e("debug-发送评论",result.toString());
        return result.getInt("code");
    }

    public static int likeReply(long oid, long root, boolean action) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/reply/action";
        String arg = "oid=" + oid + "&type=1&rpid=" + root + "&action=" + (action ? "1" : "0") + "&jsonp=jsonp&csrf=" + SharedPreferencesUtil.getString("csrf","");
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, arg, ConfInfoApi.webHeaders).body()).string());
        Log.e("debug-点赞评论",result.toString());
        return result.getInt("code");
    }
}
