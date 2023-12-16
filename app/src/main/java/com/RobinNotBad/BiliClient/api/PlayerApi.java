package com.RobinNotBad.BiliClient.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.io.File;

public class PlayerApi {
    public static void jumpToPlayer(Context context, String videourl, String danmakuurl, String title, boolean local){
        Log.e("debug-准备跳转","--------");
        Log.e("debug-视频标题",title);
        Log.e("debug-视频地址",videourl);
        Log.e("debug-弹幕地址",danmakuurl);
        Log.e("debug-准备跳转","--------");

        Intent intent = new Intent();
        switch (SharedPreferencesUtil.getString("player", "null")) {
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
                if(local) {
                    intent.setClassName(context.getString(R.string.player_aliang_package), "com.aliangmaker.meida.VideoPlayerActivity");
                    File videoFile = new File(videourl);
                    intent.putExtra("getVideoPath",videoFile.getPath());
                    intent.putExtra("internet",false);
                    intent.putExtra("activity",true);
                    intent.putExtra("videoName", title);
                }
                else {
                    intent.setClassName(context.getString(R.string.player_aliang_package), "com.aliangmaker.meida.GetIntentActivity");
                    intent.setData(Uri.parse(videourl));
                    intent.putExtra("cookie", SharedPreferencesUtil.getString("cookies", ""));
                    intent.putExtra("danmaku_url", danmakuurl);
                    intent.putExtra("title", title);
                }
                intent.setAction(Intent.ACTION_VIEW);

                break;

            default:
                intent.setClass(context, SettingPlayerActivity.class);
                break;
        }
        context.startActivity(intent);
    }
}
