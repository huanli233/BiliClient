package com.RobinNotBad.BiliClient.activity.video;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.DownloadActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.model.SubtitleLink;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;

import java.io.IOException;

public class JumpToPlayerActivity extends BaseActivity {
    String videourl;
    String danmakuurl;
    String subtitleurl = "";
    String title;
    TextView textView;

    String bvid;
    long aid,cid,mid;

    int qn;

    int download;

    boolean destroyed = false;

    int progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_jump);

        textView = findViewById(R.id.title);

        Intent intent = getIntent();
        Log.e("debug-哔哩终端-跳转页", "已接收数据");
        bvid = intent.getStringExtra("bvid");    //不能删，播放器靠bvid获取在线人数（但我估计aid也行
        aid = intent.getLongExtra("aid", 0);
        cid = intent.getLongExtra("cid", 0);
        mid = intent.getLongExtra("mid", 0);
        progress = intent.getIntExtra("progress", -1);

        title = intent.getStringExtra("title");
        download = intent.getIntExtra("download", 0);

        qn = intent.getIntExtra("qn", -1);

        Log.e("debug-哔哩终端-跳转页", "cid=" + cid);
        danmakuurl = "https://comment.bilibili.com/" + cid + ".xml";

        requestVideo(qn != -1 ? qn : SharedPreferencesUtil.getInt("play_qn", 16));
    }

    @SuppressLint("SetTextI18n")
    private void requestVideo(int qn) {
        CenterThreadPool.run(() -> {
            try {
                if (download == 0 && progress == -1) {
                    Pair<Long, Integer> progressPair = VideoInfoApi.getWatchProgress(aid);
                    progress = progressPair.first == cid ? progressPair.second : 0;
                }
                Pair<String, String> video = PlayerApi.getVideo(aid, cid, qn, download != 0);
                videourl = video.first;

                try {
                    if(download != 0) {
                        jump();
                        return;
                    }
                    SubtitleLink[] subtitleLinks = PlayerApi.getSubtitleLink(aid, cid);
                    if(subtitleLinks.length==0 || (subtitleLinks.length==1 && subtitleLinks[0].isAI)) {
                        jump();
                    }
                    else {
                        String[] choices = new String[subtitleLinks.length+1];
                        int i = 0;
                        for (; i < subtitleLinks.length; i++) {
                            choices[i] = subtitleLinks[i].lang;
                        }
                        choices[i] = "不显示字幕";
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("请选择字幕");
                        builder.setCancelable(false);
                        builder.setSingleChoiceItems(choices, 0, (dialog, which) -> {
                            if(which < subtitleLinks.length) subtitleurl = subtitleLinks[which].url;
                            jump();
                            dialog.dismiss();
                        });
                        runOnUiThread(()->builder.create().show());
                    }
                } catch (Exception e){
                    MsgUtil.showMsg("没有获取到字幕",this);
                    jump();
                    e.printStackTrace();
                }
            } catch (IOException e) {
                runOnUiThread(() -> setClickExit("网络错误！\n请检查你的网络连接是否正常"));
                e.printStackTrace();
            } catch (JSONException e) {
                runOnUiThread(() -> setClickExit("视频获取失败！\n可能的原因：\n1.本视频仅大会员可播放\n2.视频获取接口失效"));
                e.printStackTrace();
            } catch (ActivityNotFoundException e) {
                runOnUiThread(() -> setClickExit("跳转失败！\n请安装对应的播放器\n或在设置中选择正确的播放器\n或将哔哩终端和播放器同时更新到最新版本"));
                e.printStackTrace();
            }
        });
    }

    private void jump(){
        destroyed = true;
        if (download != 0) {
            Intent intent = new Intent();
            intent.setClass(this, DownloadActivity.class);
            intent.putExtra("type", download);
            intent.putExtra("link", videourl);
            intent.putExtra("danmaku", danmakuurl);
            intent.putExtra("title", title);
            intent.putExtra("cover", getIntent().getStringExtra("cover"));
            if (download == 2)
                intent.putExtra("parent_title", getIntent().getStringExtra("parent_title"));
            startActivity(intent);
        } else {
            PlayerApi.jumpToPlayer(JumpToPlayerActivity.this, videourl, danmakuurl, subtitleurl, title, false, aid, bvid, cid, mid, progress, false);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setClickExit(String reason) {
        textView.setText(reason);
        findViewById(R.id.root_layout).setOnClickListener((view) -> finish());
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }
}