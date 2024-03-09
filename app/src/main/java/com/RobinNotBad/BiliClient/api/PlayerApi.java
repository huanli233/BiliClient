package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.player.PlayerActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerChooseActivity;
import com.RobinNotBad.BiliClient.activity.video.JumpToPlayerActivity;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PlayerApi {
    public static void startGettingUrl(Context context, VideoInfo videoInfo, int page){
        Intent intent = new Intent()
                .setClass(context, JumpToPlayerActivity.class)
                .putExtra("title", (videoInfo.pagenames.size()==1 ? videoInfo.title : videoInfo.pagenames.get(page)))
                .putExtra("bvid", videoInfo.bvid)
                .putExtra("aid", videoInfo.aid)
                .putExtra("cid", videoInfo.cids.get(page));
        context.startActivity(intent);
    }

    public static void startDownloadingVideo(Context context, VideoInfo videoInfo, int page){
        Intent intent = new Intent()
                .putExtra("aid", videoInfo.aid)
                .putExtra("bvid", videoInfo.bvid)
                .putExtra("cid", videoInfo.cids.get(page))
                .putExtra("title", (videoInfo.pagenames.size()==1 ? videoInfo.title : videoInfo.pagenames.get(page)))
                .putExtra("download", (videoInfo.pagenames.size()==1 ? 1 : 2))  //1：单页  2：分页
                .putExtra("cover", videoInfo.cover)
                .putExtra("parent_title", videoInfo.title)
                .setClass(context, JumpToPlayerActivity.class);
        context.startActivity(intent);
    }



    public static void jumpToPlayer(Context context, String videourl, String danmakuurl, String title, boolean local){
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
                if(local) {
                    intent.setData(getVideoUri(context,videourl));
                }
                else {
                    intent.setData(Uri.parse(videourl));
                    Map<String,String> headers = new HashMap<>();
                    headers.put("Cookie",SharedPreferencesUtil.getString("cookies", ""));
                    headers.put("Referer","https://www.bilibili.com/");
                    intent.putExtra("cookie",(Serializable) headers);
                    intent.putExtra("agent",ConfInfoApi.USER_AGENT_WEB);
                }
                Log.e("uri",intent.getData().toString());
                intent.putExtra("danmaku", danmakuurl);
                intent.putExtra("name", title);
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
