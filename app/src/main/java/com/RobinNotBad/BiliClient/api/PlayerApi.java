package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.player.PlayerActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerChooseActivity;
import com.RobinNotBad.BiliClient.activity.video.JumpToPlayerActivity;
import com.RobinNotBad.BiliClient.model.Subtitle;
import com.RobinNotBad.BiliClient.model.SubtitleLink;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.service.DownloadService;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Response;

public class PlayerApi {
    public static void startGettingUrl(Context context, VideoInfo videoInfo, int page, int progress) {
        long mid;
        try {
            mid = SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0);
        } catch (Throwable ignored) {
            mid = 0;
        }
        Intent intent = new Intent()
                .setClass(context, JumpToPlayerActivity.class)
                .putExtra("title", (videoInfo.pagenames.size() == 1 ? videoInfo.title : videoInfo.pagenames.get(page)))
                .putExtra("bvid", videoInfo.bvid)
                .putExtra("aid", videoInfo.aid)
                .putExtra("cid", videoInfo.cids.get(page))
                .putExtra("mid", mid)
                .putExtra("progress", progress);
        context.startActivity(intent);
    }

    public static void startDownloading(VideoInfo videoInfo, int page, int qn) {
        if(SharedPreferencesUtil.getBoolean("dev_download_old",false)) {
            Context context = BiliTerminal.context;

            Intent intent = new Intent(context, JumpToPlayerActivity.class)
                    .putExtra("aid", videoInfo.aid)
                    .putExtra("bvid", videoInfo.bvid)
                    .putExtra("cid", videoInfo.cids.get(page))
                    .putExtra("title", (videoInfo.pagenames.size() == 1 ? videoInfo.title : videoInfo.pagenames.get(page)))
                    .putExtra("download", (videoInfo.pagenames.size() == 1 ? 1 : 2))  //1：单页  2：分页
                    .putExtra("cover", videoInfo.cover)
                    .putExtra("parent_title", videoInfo.title)
                    .putExtra("qn", qn)
                    .putExtra("mid", videoInfo.staff.get(0).mid)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }

        if(videoInfo.cids.size() == 1)
            DownloadService.startDownload(videoInfo.title,
                    videoInfo.aid, videoInfo.cids.get(0),
                    ("https://comment.bilibili.com/" + videoInfo.cids.get(0) + ".xml"),
                    videoInfo.cover,
                    qn);
        else DownloadService.startDownload(videoInfo.title, videoInfo.pagenames.get(page),
                videoInfo.aid, videoInfo.cids.get(page),
                ("https://comment.bilibili.com/" + videoInfo.cids.get(page) + ".xml"),
                videoInfo.cover,
                qn);
    }

    /**
     * 解析视频，从JumpToPlayerActivity弄出来的，懒得改很多所以搞了个莫名其妙的Pair
     *
     * @param aid      aid
     * @param cid      cid
     * @param qn       qn
     * @param download 是否下载
     * @return 视频url与完整返回信息
     */
    public static Pair<String, String> getVideo(long aid, long cid, int qn, boolean download) throws JSONException, IOException {
        boolean html5 = !download && SharedPreferencesUtil.getString("player", "").equals("mtvPlayer");
        //html5方式现在已经仅对小电视播放器保留了

        String url = "https://api.bilibili.com/x/player/wbi/playurl?"
                + "avid=" + aid
                + "&cid=" + cid
                + (html5 ? "&high_quality=1&qn=" + qn : "&qn=" + qn)
                + "&platform=" + (html5 ? "html5" : "pc");

        url = ConfInfoApi.signWBI(url);

        Response response = NetWorkUtil.get(url, NetWorkUtil.webHeaders);

        String body = Objects.requireNonNull(response.body()).string();
        Log.e("debug-body", body);
        JSONObject body1 = new JSONObject(body);
        JSONObject data = body1.getJSONObject("data");
        JSONArray durl = data.getJSONArray("durl");
        JSONObject video_url = durl.getJSONObject(0);
        String videourl = video_url.getString("url");
        return new Pair<>(videourl, body);
    }

    public static void jumpToPlayer(Context context, String videourl, String danmakuurl, String subtitleurl, String title, boolean local, long aid, String bvid, long cid, long mid, int progress, boolean live_mode) {
        Log.e("debug-准备跳转", "--------");
        Log.e("debug-视频标题", title);
        Log.e("debug-视频地址", videourl);
        Log.e("debug-弹幕地址", danmakuurl);
        Log.e("debug-字幕地址", subtitleurl);
        Log.e("debug-准备跳转", "--------");

        Intent intent = new Intent();
        switch (SharedPreferencesUtil.getString("player", "null")) {
            case "terminalPlayer":
                intent.setClass(context, PlayerActivity.class);
                intent.putExtra("url", videourl);
                intent.putExtra("danmaku", danmakuurl);
                intent.putExtra("subtitle", subtitleurl);
                intent.putExtra("title", title);
                intent.putExtra("aid", aid);
                intent.putExtra("bvid", bvid);
                intent.putExtra("cid", cid);
                intent.putExtra("mid", mid);
                intent.putExtra("progress", progress);
                intent.putExtra("live_mode", live_mode);
                break;

            case "mtvPlayer":
                intent.setClassName(context.getString(R.string.player_mtv_package), "com.xinxiangshicheng.wearbiliplayer.cn.player.PlayerActivity");
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra("cookie", SharedPreferencesUtil.getString("cookies", ""));
                intent.putExtra("mode", (local ? "2" : "0"));
                intent.putExtra("url", videourl);
                intent.putExtra("danmaku", danmakuurl);
                intent.putExtra("title", title);
                break;

            case "aliangPlayer":
                intent.setClassName(context.getString(R.string.player_aliang_package), "com.aliangmaker.media.PlayVideoActivity");
                intent.putExtra("name", title);
                intent.putExtra("danmaku", danmakuurl);
                intent.putExtra("live_mode", live_mode);
                if (local) {
                    intent.setData(getVideoUri(context, videourl));
                } else {
                    intent.setData(Uri.parse(videourl));
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Cookie", SharedPreferencesUtil.getString("cookies", ""));
                    headers.put("Referer", "https://www.bilibili.com/");
                    intent.putExtra("cookie", (Serializable) headers);
                    intent.putExtra("agent", NetWorkUtil.USER_AGENT_WEB);
                    intent.putExtra("progress", progress * 1000L);
                }
                Log.e("uri", intent.getData().toString());
                intent.setAction(Intent.ACTION_VIEW);

                break;

            default:
                intent.setClass(context, SettingPlayerChooseActivity.class);
                break;
        }
        context.startActivity(intent);
    }

    public static Uri getVideoUri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID},
                MediaStore.Video.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/video/media");
            cursor.close();
            return Uri.withAppendedPath(baseUri, String.valueOf(id));
        } else {
            if (cursor != null) cursor.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA, path);
            return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    public static SubtitleLink[] getSubtitleLink(long aid, long cid) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/player/wbi/v2?aid=" + aid
                + "&cid=" + cid;
        url = ConfInfoApi.signWBI(url);
        JSONObject data = NetWorkUtil.getJson(url).getJSONObject("data");

        JSONArray subtitles = data.getJSONObject("subtitle").getJSONArray("subtitles");
        Log.d("debug-subtitle",subtitles.toString());

        SubtitleLink[] links = new SubtitleLink[subtitles.length()];
        for (int i = 0; i < subtitles.length(); i++) {
            JSONObject subtitle = subtitles.getJSONObject(i);

            long id = subtitle.getLong("id");
            boolean isAI = subtitle.getInt("type")==1;
            String lang = subtitle.getString("lan_doc");
            String subtitle_url = "https:" + subtitle.getString("subtitle_url");

            SubtitleLink link = new SubtitleLink(id,lang,subtitle_url,isAI);
            links[i] = link;
        }
        return links;
    }

    public static Subtitle[] getSubtitle(String url) throws JSONException, IOException {
        JSONArray body = NetWorkUtil.getJson(url).getJSONArray("body");
        Subtitle[] subtitles = new Subtitle[body.length()];
        for (int i = 0; i < body.length(); i++) {
            JSONObject single = body.getJSONObject(i);
            subtitles[i] = new Subtitle(
                    single.getString("content"),
                    single.getDouble("from"),
                    single.getDouble("to"));
        }
        return subtitles;
    }
}
