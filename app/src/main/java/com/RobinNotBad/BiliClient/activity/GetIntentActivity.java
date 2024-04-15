package com.RobinNotBad.BiliClient.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.RobinNotBad.BiliClient.BiliTerminal;

public class GetIntentActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        switch (intent.getStringExtra("type")){
            case "video_av":
                BiliTerminal.jumpToVideo(this,intent.getLongExtra("content",0));
                break;
            case "video_bv":
                BiliTerminal.jumpToVideo(this,intent.getStringExtra("content"));
                break;
            case "article":
                BiliTerminal.jumpToArticle(this,intent.getLongExtra("content",0));
                break;
            case "user":
                BiliTerminal.jumpToUser(this,intent.getLongExtra("content",0));
                break;
            default:
                finish();
        }
    }
}
