package com.RobinNotBad.BiliClient.activity.video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.video.PageChooseAdapter;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.model.PlayerData;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.ui.widget.recycler.CustomLinearManager;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.TerminalContext;

import java.io.File;

//分页视频选集
//2023-07-17

public class MultiPageActivity extends BaseActivity {
    VideoInfo videoInfo;
    PlayerData playerData;

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
        playerData = intent.getParcelableExtra("data");

        TerminalContext.getInstance().getVideoInfoByAidOrBvId(playerData.aid, "").observe(this, result -> result.onSuccess((videoInfo -> {
            this.videoInfo = videoInfo;
            PageChooseAdapter adapter = new PageChooseAdapter(this, videoInfo.pagenames);

            if (intent.getIntExtra("download", 0) == 1) {    //下载模式
                adapter.setOnItemClickListener(position -> {
                    File rootPath = new File(FileUtil.getVideoDownloadPath(), FileUtil.stringToFile(videoInfo.title));
                    File downPath = new File(rootPath, FileUtil.stringToFile(videoInfo.pagenames.get(position)));
                    if (downPath.exists()) {
                        File file_sign = new File(downPath,".DOWNLOADING");
                        MsgUtil.showMsg(file_sign.exists() ? "已在下载队列" : "已下载完成");
                    }
                    else {
                        startActivity(
                                new Intent()
                                        .putExtra("page", position)
                                        .setClass(this, QualityChooserActivity.class)
                                        .putExtra("aid", videoInfo.aid)
                                        .putExtra("bvid", videoInfo.bvid)
                        );
                    }
                });
            } else {        //普通播放模式
                adapter.setOnItemClickListener(position -> {
                    long cid_curr = videoInfo.cids.get(position);
                    if(cid_curr != playerData.cidHistory) {
                        playerData = videoInfo.toPlayerData(position);
                        playerData.cidHistory = cid_curr;
                        playerData.timeStamp = 0;
                    }

                    PlayerApi.startGettingUrl(playerData);
                    playerData.timeStamp = 0;
                });
            }

            recyclerView.setLayoutManager(new CustomLinearManager(this));
            recyclerView.setAdapter(adapter);
        })));

    }

}