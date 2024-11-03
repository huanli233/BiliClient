package com.RobinNotBad.BiliClient.activity.video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.video.PageChooseAdapter;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.TerminalContext;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import java.io.File;

//分页视频选集
//2023-07-17

public class MultiPageActivity extends BaseActivity {
    private static final String TAG = "MultiPageActivity";

    boolean play_clicked;
    VideoInfo videoInfo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        findViewById(R.id.top).setOnClickListener(view -> finish());

        TextView textView = findViewById(R.id.pageName);
        textView.setText("请选择分页");

        Intent intent = getIntent();
        try {
            videoInfo = TerminalContext.getInstance().getCurrentVideoInfo();
        } catch (TerminalContext.IllegalTerminalStateException e) {
            Log.wtf(TAG, e);
            MsgUtil.toast("找不到当前视频信息QAQ", this);
        }

        PageChooseAdapter adapter = new PageChooseAdapter(this, videoInfo.pagenames);

        if (intent.getIntExtra("download", 0) == 1) {    //下载模式
            adapter.setOnItemClickListener(position -> {
                File rootPath = new File(FileUtil.getDownloadPath(this), ToolsUtil.stringToFile(videoInfo.title));
                File downPath = new File(rootPath, ToolsUtil.stringToFile(videoInfo.pagenames.get(position)));
                if (downPath.exists()) MsgUtil.showMsg("已经缓存过了~", this);
                else {
                    startActivity(new Intent().putExtra("page", position).setClass(this, QualityChooserActivity.class));
                }
            });
        } else {        //普通播放模式
            int progress = intent.getIntExtra("progress", -1);
            long progress_cid = intent.getLongExtra("progress_cid", 0);
            adapter.setOnItemClickListener(position -> {
                long cid = videoInfo.cids.get(position);
                PlayerApi.startGettingUrl(this, videoInfo, position, (progress_cid == cid && !play_clicked) ? progress : -1);
                play_clicked = true;
            });
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

}