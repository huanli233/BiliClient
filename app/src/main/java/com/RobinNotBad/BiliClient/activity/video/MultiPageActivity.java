package com.RobinNotBad.BiliClient.activity.video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.PageChooseAdapter;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import java.io.File;

//分页视频选集
//2023-07-17

public class MultiPageActivity extends BaseActivity {

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
        VideoInfo videoInfo = (VideoInfo) intent.getSerializableExtra("videoInfo");

        PageChooseAdapter adapter = new PageChooseAdapter(this,videoInfo.pagenames);

        if(intent.getIntExtra("download",0) == 1) {    //下载模式
            adapter.setOnItemClickListener(position -> {
                File rootPath = new File(ConfInfoApi.getDownloadPath(this), ToolsUtil.stringToFile(videoInfo.title));
                File downPath = new File(rootPath, ToolsUtil.stringToFile(videoInfo.pagenames.get(position)));
                if(downPath.exists()) MsgUtil.toast("已经缓存过了~",this);
                else PlayerApi.startDownloadingVideo(this,videoInfo,position);
            });
        }
        else{        //普通播放模式
            adapter.setOnItemClickListener(position -> PlayerApi.startGettingUrl(this,videoInfo,position));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

}