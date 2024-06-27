package com.RobinNotBad.BiliClient.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.util.MsgUtil;

public class GetIntentActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");

        if (type != null) switch (intent.getStringExtra("type")) {
            case "video_av":
                BiliTerminal.jumpToVideo(this, intent.getLongExtra("content", 0));
                break;
            case "video_bv":
                BiliTerminal.jumpToVideo(this, intent.getStringExtra("content"));
                break;
            case "article":
                BiliTerminal.jumpToArticle(this, intent.getLongExtra("content", 0));
                break;
            case "user":
                BiliTerminal.jumpToUser(this, intent.getLongExtra("content", 0));
                break;
            default:
                MsgUtil.toastLong("不支持打开：" + type, this);
                break;
        }

        Uri uri = intent.getData();
        if (uri != null) {
            String host = uri.getHost();
            Log.e("debug-host", host);

            switch (host) {
                case "video":
                    BiliTerminal.jumpToVideo(this, Long.parseLong(uri.getLastPathSegment()));
                    break;
                case "article":
                    BiliTerminal.jumpToArticle(this, Long.parseLong(uri.getLastPathSegment()));
                    break;
                default:
                    MsgUtil.toastLong("不支持打开：" + host, this);
                    break;
            }
        }

        finish();
    }
}
