package com.RobinNotBad.BiliClient.api;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.model.ContentType;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.Result;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

//腕上哔哩那边注释里写了一连串的麻烦麻烦麻烦，顿时预感不妙
//其实还好
//用Log直接替代注释了，应该都能看懂吧
//2023-07-22

public class ReplyApi {

    public static final int REPLY_TYPE_VIDEO_CHILD = 0;
    public static final int REPLY_TYPE_VIDEO = 1;
    public static final int REPLY_TYPE_ARTICLE = 12;
    public static final int REPLY_TYPE_DYNAMIC_CHILD = 11;
    public static final int REPLY_TYPE_DYNAMIC = 17;
    public static final String TOP_TIP = "[置顶]";

    /**
     * @param originId 评论区id，为评论所属内容的id，例如视频aid
     * @param rpid 父评论的id，无父评论则为0
     * @param pageNumber 分页，需要拉取的评论的页号
     * @param type 评论所属内容类型
     * @param sort 评论区排序方式，0=时间；1=点赞数量；2=回复数量
     * @param replyArrayList 填充数组
     * @return -1错误,0正常，1到底了
     * @throws JSONException json解析异常
     * @throws IOException 网络异常
     */
    public static int getReplies(long originId, long rpid, int pageNumber, ContentType type, int sort, List<Reply> replyArrayList) throws JSONException, IOException {

        String url = "https://api.bilibili.com/x/v2/reply" + (rpid == 0 ? "" : "/reply") + "?pn=" + pageNumber
                + "&type=" + type.getTypeCode() + "&oid=" + originId + "&sort=" + sort + (rpid == 0 ? "" : ("&root=" + rpid));
        JSONObject all = NetWorkUtil.getJson(url);

        //Log.e("debug-评论区",all.toString());

        int size = replyArrayList.size();
        if (all.getInt("code") == 0 && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            JSONObject page = data.getJSONObject("page");
            if (!data.isNull("replies") && page.getInt("size") > 0) {
                if (rpid == 0 && data.has("top_replies") && page.getInt("num") == 1)
                    analyzeReplyArray(true, data.getJSONArray("top_replies"), replyArrayList);
                JSONArray replies = data.getJSONArray("replies");
                analyzeReplyArray(rpid == 0, replies, replyArrayList);
                if (replyArrayList.size() == size) return 1;
                else return 0;
            } else return 1;
        } else return -1;
    }

    public static Result<Reply> getRootReply(ContentType contentType, long originId, long rpid) {
        String url = "https://api.bilibili.com/x/v2/reply/reply" + "?type=" + contentType.getTypeCode() + "&oid=" + originId + "&root=" + rpid;
        try {
            JSONObject json = NetWorkUtil.getJson(url);
            if (json.getInt("code") != 0 || json.isNull("data")) {
                return Result.failure(new Exception(json.getString("message")));
            }
            JSONObject data = json.getJSONObject("data");
            if (data.isNull("root")) {
                return Result.failure(new Exception("未找到根评论"));
            }
            return Result.success(new Reply(true, data.getJSONObject("root")));
        } catch (Exception e) {
            return Result.failure(e);
        }
    }


    /**
     * 获取评论列表（/x/v2/reply/wbi/main）
     *
     * @param oid        评论区oid
     * @param rpid       指定要寻找的评论的rpid
     * @param pagination 页
     * @param type       评论区类型
     * @param sort       排序方式
     * @return 返回码（0=继续加载，1=到底了，-1=错误）与下一页的pagination
     */
    @NonNull
    public static Pair<Integer, String> getRepliesLazy(long oid, long rpid, String pagination, int type, int sort, List<Reply> replyArrayList) throws JSONException, IOException {

        NetWorkUtil.FormData reqData = new NetWorkUtil.FormData()
                .setUrlParam(true)
                .put("type", type)
                .put("oid", oid)
                .put("plat", 1)
                .put("web_location", "1315875")
                .put("mode", sort);
        reqData.put("pagination_str", new JSONObject().put("offset", TextUtils.isEmpty(pagination) ? "" : pagination));
        if (rpid > 0) reqData.put("seek_rpid", rpid);
        String url = "https://api.bilibili.com/x/v2/reply/wbi/main" + reqData;

        //Log.e("debug-评论区链接", url);

        JSONObject all = NetWorkUtil.getJson(ConfInfoApi.signWBI(url));

        //Log.e("debug-评论区",all.toString());

        if (all.getInt("code") == 0 && !all.isNull("data")) {
            JSONObject data = all.getJSONObject("data");
            JSONObject cursor = data.getJSONObject("cursor");
            if (!data.isNull("replies") && data.getJSONArray("replies").length() > 0) {
                if (rpid <= 0 && data.has("top_replies") && !data.isNull("top_replies") && cursor.getBoolean("is_begin"))
                    analyzeReplyArray(true, data.getJSONArray("top_replies"), replyArrayList);
                JSONArray replies = data.getJSONArray("replies");
                analyzeReplyArray(true, replies, replyArrayList);
                JSONObject paginationReply = cursor.optJSONObject("pagination_reply");
                String nextOffset = paginationReply == null ? null : paginationReply.optString("next_offset");
                if (cursor.optBoolean("is_end", false) || TextUtils.isEmpty(nextOffset)) {
                    return new Pair<>(1, "");
                } else {
                    return new Pair<>(0, nextOffset);
                }
            } else if (rpid <= 0 && data.has("top_replies") && !data.isNull("top_replies") && cursor.getBoolean("is_begin")) {
                analyzeReplyArray(true, data.getJSONArray("top_replies"), replyArrayList);
                return new Pair<>(1, "");
            } else return new Pair<>(1, "");
        } else return new Pair<>(-1, "");
    }  //-1错误,0正常，1到底了

    public static void analyzeReplyArray(boolean isRoot, JSONArray replies, List<Reply> replyArrayList) throws JSONException {
        for (int i = 0; i < replies.length(); i++) {
            JSONObject reply = replies.getJSONObject(i);
            Reply replyReturn = new Reply(isRoot, reply);
            replyArrayList.add(replyReturn);
        }
    }

    public static Pair<Integer, Reply> sendReply(long oid, long root, long parent, String text, int type) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/reply/add";
        String arg = "oid=" + oid + "&type=" + type + (root == 0 ? "" : ("&root=" + root + "&parent=" + parent))
                + "&message=" + text + "&jsonp=jsonp&csrf=" + SharedPreferencesUtil.getString("csrf", "");
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, arg, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-发送评论", result.toString());
        JSONObject reply = null;
        if (result.has("data") && !result.isNull("data") && result.getJSONObject("data").has("reply") && !result.getJSONObject("data").isNull("reply")) {
            reply = result.getJSONObject("data").getJSONObject("reply");
        }
        return new Pair<>(result.getInt("code"), reply == null ? null : new Reply(root != 0, reply));
    }

    public static Pair<Integer, Reply> sendReply(long oid, long root, long parent, String text) throws IOException, JSONException {
        return sendReply(oid, root, parent, text, REPLY_TYPE_VIDEO);
    }

    public static Pair<Integer, Reply> sendDynamicReply(long oid, long root, long parent, String text) throws IOException, JSONException {
        return sendReply(oid, root, parent, text, REPLY_TYPE_DYNAMIC);
    }

    public static int likeReply(long oid, long root, boolean action) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/reply/action";
        String arg = "oid=" + oid + "&type=1&rpid=" + root + "&action=" + (action ? "1" : "0") + "&jsonp=jsonp&csrf=" + SharedPreferencesUtil.getString("csrf", "");
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, arg, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-点赞评论", result.toString());
        return result.getInt("code");
    }

    /**
     * 删除评论
     *
     * @param oid  oid
     * @param rpid rpid
     * @param type 评论区类型
     * @return 返回码
     */
    public static int deleteReply(long oid, long rpid, int type) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/reply/del";
        String reqBody = new NetWorkUtil.FormData()
                .put("type", type)
                .put("oid", oid)
                .put("rpid", rpid)
                .put("csrf", SharedPreferencesUtil.getString("csrf", ""))
                .toString();
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, reqBody, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-点赞评论", result.toString());
        return result.getInt("code");
    }

    public static long getReplyCount(long oid, int type) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/v2/reply/count?oid=" + oid + "&type=" + type;
        JSONObject all = NetWorkUtil.getJson(url);
        if (all.has("data") && (!all.isNull("data"))) {
            return all.getJSONObject("data").getLong("count");
        }
        return 0;
    }
}
