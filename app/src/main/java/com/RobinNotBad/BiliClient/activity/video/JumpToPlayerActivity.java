package com.RobinNotBad.BiliClient.activity.video;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.DownloadActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.HistoryApi;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.model.PlayerData;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;

import java.io.IOException;

public class JumpToPlayerActivity extends BaseActivity {
    String title;
    TextView textView;

    PlayerData playerData;

    int download;

    final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            int code = o.getResultCode();
            Intent result = o.getData();
            if(code == RESULT_OK && result != null){
                int progress = result.getIntExtra("progress",0);
                Log.d("debug-进度回调", String.valueOf(progress));

                CenterThreadPool.run(() -> {
                    if (playerData.mid != 0 && playerData.aid != 0) try {
                        HistoryApi.reportHistory(playerData.aid, playerData.cid, playerData.mid, progress / 1000);
                    }
                    catch (Exception e) {MsgUtil.err("进度上报：", e);}
                });
            }
            finish();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_jump);

        textView = findViewById(R.id.text_title);

        Intent intent = getIntent();
        Log.e("debug-哔哩终端-跳转页", "已接收数据");

        playerData = (PlayerData) intent.getParcelableExtra("data");

        title = playerData.title;

        download = intent.getIntExtra("download", 0);

        playerData.qn = playerData.qn != -1 ? playerData.qn : SharedPreferencesUtil.getInt("play_qn", 16);

        requestVideo();
    }

    @SuppressLint("SetTextI18n")
    private void requestVideo() {
        CenterThreadPool.run(() -> {
            try {
                if (download == 0 && playerData.progress == -1) {
                    Pair<Long, Integer> progressPair = VideoInfoApi.getWatchProgress(playerData.aid);
                    playerData.progress = progressPair.first == playerData.cid ? progressPair.second : 0;
                }

                if(playerData.isBangumi()) PlayerApi.getBangumi(playerData);
                else PlayerApi.getVideo(playerData, download != 0);

                try {
                    if(download != 0) {
                        jump();
                        return;
                    }
                    jump();
                } catch (Exception e){
                    MsgUtil.showMsg("没有获取到字幕");
                    jump();
                    e.printStackTrace();
                }
            } catch (IOException e) {
                setClickExit("网络错误！\n请检查你的网络连接是否正常");
                e.printStackTrace();
            } catch (JSONException e) {
                setClickExit("视频获取失败！\n可能的原因：\n1.本视频仅大会员可播放\n2.视频获取接口失效");
                e.printStackTrace();
            } catch (ActivityNotFoundException e) {
                setClickExit("跳转失败！\n请安装对应的播放器\n或在设置中选择正确的播放器\n或将哔哩终端和播放器同时更新到最新版本");
                e.printStackTrace();
            }
        });
    }

    private void jump(){
        if(isDestroyed()) return;
        if (download == 0) {
            Intent intent = PlayerApi.jumpToPlayer(playerData);
            launcher.launch(intent);
            setClickExit("等待退出播放后上报进度\n（点击跳过）");
        }
        else {
            Intent intent = new Intent();
            intent.setClass(this, DownloadActivity.class);
            intent.putExtra("type", download);
            intent.putExtra("link", playerData.videoUrl);
            intent.putExtra("danmaku", playerData.danmakuUrl);
            intent.putExtra("title", title);
            intent.putExtra("cover", getIntent().getStringExtra("cover"));
            if (download == 2)
                intent.putExtra("parent_title", getIntent().getStringExtra("parent_title"));
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setClickExit(String reason) {
        runOnUiThread(()->{
            textView.setText(reason);
            textView.setOnClickListener((view) -> finish());
        });
    }
}