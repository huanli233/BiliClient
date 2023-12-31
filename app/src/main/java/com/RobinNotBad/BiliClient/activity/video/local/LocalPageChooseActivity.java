package com.RobinNotBad.BiliClient.activity.video.local;

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
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.io.File;
import java.util.ArrayList;

//分页视频选集
//2023-07-17

public class LocalPageChooseActivity extends BaseActivity {

    private int longClickPosition = -1;
    private boolean deleted = false;

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
        String title = intent.getStringExtra("title");
        ArrayList<String> pageList = intent.getStringArrayListExtra("pageList");
        ArrayList<String> videoFileList = intent.getStringArrayListExtra("videoFileList");
        ArrayList<String> danmakuFileList = intent.getStringArrayListExtra("danmakuFileList");

        PageChooseAdapter adapter = new PageChooseAdapter(this,pageList);
        adapter.setOnItemClickListener(position -> PlayerApi.jumpToPlayer(LocalPageChooseActivity.this, videoFileList.get(position), danmakuFileList.get(position), pageList.get(position), true));
        adapter.setOnItemLongClickListener(position -> {
            if(longClickPosition == position){
                File workPath = ConfInfoApi.getDownloadPath(this);
                File videoPath = new File(workPath, title);
                File pagePath = new File(videoPath,pageList.get(position));

                FileUtil.deleteFolder(pagePath);
                pageList.remove(position);
                videoFileList.remove(position);
                danmakuFileList.remove(position);
                adapter.notifyItemRemoved(position);

                if(pageList.isEmpty()){
                    FileUtil.deleteFolder(videoPath);
                }

                MsgUtil.toast("删除成功",this);
                longClickPosition = -1;

                deleted = true;
            }
            else{
                longClickPosition = position;
                MsgUtil.toast("再次长按删除",this);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        if(deleted && LocalListActivity.instance!=null) LocalListActivity.instance.refresh();
        super.onDestroy();
    }
}