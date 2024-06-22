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

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.player.PlayerActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerChooseActivity;
import com.RobinNotBad.BiliClient.activity.video.JumpToPlayerActivity;
import com.RobinNotBad.BiliClient.model.VideoInfo;
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
    public static void startGettingUrl(Context context, VideoInfo videoInfo, int page, int progress){
        Intent intent = new Intent()
                .setClass(context, JumpToPlayerActivity.class)
                .putExtra("title", (videoInfo.pagenames.size()==1 ? videoInfo.title : videoInfo.pagenames.get(page)))
                .putExtra("bvid", videoInfo.bvid)
                .putExtra("aid", videoInfo.aid)
                .putExtra("cid", videoInfo.cids.get(page))
                .putExtra("mid", videoInfo.staff.get(0).mid)
                .putExtra("progress", progress);
        context.startActivity(intent);
    }

    public static void startDownloadingVideo(Context context, VideoInfo videoInfo, int page){
        startDownloadingVideo(context, videoInfo, page, -1);
    }

    public static void startDownloadingVideo(Context context, VideoInfo videoInfo, int page, int qn){
        Intent intent = new Intent()
                .putExtra("aid", videoInfo.aid)
                .putExtra("bvid", videoInfo.bvid)
                .putExtra("cid", videoInfo.cids.get(page))
                .putExtra("title", (videoInfo.pagenames.size()==1 ? videoInfo.title : videoInfo.pagenames.get(page)))
                .putExtra("download", (videoInfo.pagenames.size()==1 ? 1 : 2))  //1：单页  2：分页
                .putExtra("cover", videoInfo.cover)
                .putExtra("parent_title", videoInfo.title)
                .putExtra("qn", qn)
                .putExtra("mid", videoInfo.staff.get(0).mid)
                .setClass(context, JumpToPlayerActivity.class);
        context.startActivity(intent);
    }

    /**
     * 解析视频，从JumpToPlayerActivity弄出来的，懒得改很多所以搞了个莫名其妙的Pair
     * @param aid aid
     * @param bvid bvid
     * @param cid cid
     * @param html5 html5(boolean)
     * @param qn qn
     * @return 视频url与完整返回信息
     */
    public static Pair<String, String> getVideo(long aid, String bvid, long cid, boolean html5, int qn) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/player/wbi/playurl?"
                + (aid == 0 ? ("bvid=" + bvid): ("avid=" + aid))
                + "&cid=" + cid + "&type=mp4"
                + (html5 ? "&high_quality=1&qn=" + qn : "&qn=" + qn)
                + "&platform=" + (html5 ? "html5" : "pc");
        //顺便把platform html5给删了,实测删除后放和番剧相关的东西不会404了 (不到为啥)
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

    public static void jumpToPlayer(Context context, String videourl, String danmakuurl, String title, boolean local, long aid, String bvid, long cid, long mid, int progress,boolean live_mode){
        Log.e("debug-准备跳转","--------");
        Log.e("debug-视频标题",title);
        Log.e("debug-视频地址",videourl);
        Log.e("debug-弹幕地址",danmakuurl);
        Log.e("debug-准备跳转","--------");

        Intent intent = new Intent();
        switch (SharedPreferencesUtil.getString("player", "null")) {
            case "terminalPlayer":
                intent.setClass(context,PlayerActivity.class);
                intent.putExtra("mode", (local ? 2 : 0));
                intent.putExtra("url", videourl);
                intent.putExtra("danmaku", danmakuurl);
                intent.putExtra("title", title);
                intent.putExtra("aid", aid);
                intent.putExtra("bvid", bvid);
                intent.putExtra("cid", cid);
                intent.putExtra("mid", mid);
                intent.putExtra("progress",progress);
                intent.putExtra("live_mode",live_mode);
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
                intent.putExtra("progress",0);
                intent.putExtra("danmaku", danmakuurl);
                if(local) {
                    intent.setData(getVideoUri(context,videourl));
                }
                else {
                    intent.setData(Uri.parse(videourl));
                    Map<String,String> headers = new HashMap<>();
                    headers.put("Cookie",SharedPreferencesUtil.getString("cookies", ""));
                    headers.put("Referer","https://www.bilibili.com/");
                    intent.putExtra("cookie",(Serializable) headers);
                    intent.putExtra("agent", NetWorkUtil.USER_AGENT_WEB);
                    intent.putExtra("progress",progress * 1000L);
                }
                Log.e("uri",intent.getData().toString());
                intent.setAction(Intent.ACTION_VIEW);

                break;

            default:
                intent.setClass(context, SettingPlayerChooseActivity.class);
                break;
        }
        context.startActivity(intent);
    }

    public static Uri getVideoUri(Context context, String path){
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID},
                MediaStore.Video.Media.DATA + "=? ",
                new String[]{path},null);
        if(cursor!=null && cursor.moveToFirst()){
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/video/media");
            cursor.close();
            return Uri.withAppendedPath(baseUri,String.valueOf(id));
        }
        else {
            if(cursor!=null) cursor.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA,path);
            return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);
        }
    }
}
