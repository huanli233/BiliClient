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
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;

import java.io.File;
import java.util.ArrayList;

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
        ArrayList<Integer> cids = intent.getIntegerArrayListExtra("cids");
        ArrayList<String> pages = intent.getStringArrayListExtra("pages");
        String bvid = intent.getStringExtra("bvid");
        long aid = intent.getLongExtra("aid", 0);
        //long mid = intent.getLongExtra("mid",0);

        PageChooseAdapter adapter = new PageChooseAdapter(this,pages);

        if(intent.getIntExtra("download",0) == 1) {    //下载模式
            adapter.setOnItemClickListener(position -> {
                File rootPath = new File(ConfInfoApi.getDownloadPath(this), LittleToolsUtil.stringToFile(intent.getStringExtra("title")));
                File downPath = new File(rootPath, LittleToolsUtil.stringToFile(pages.get(position)));
                if(downPath.exists()) MsgUtil.toast("已经缓存过了~",this);
                else{
                    Intent intent1 = new Intent()
                            .putExtra("aid", aid)
                            .putExtra("bvid", bvid)
                            .putExtra("cid", cids.get(position))
                            .putExtra("title", pages.get(position))
                            .putExtra("download", 2)
                            .putExtra("cover", intent.getStringExtra("cover"))
                            .putExtra("parent_title", intent.getStringExtra("title"))
                            .setClass(this, JumpToPlayerActivity.class);
                    startActivity(intent1);
                }
            });
        }
        else{        //普通播放模式
            adapter.setOnItemClickListener(position -> {
                Intent intent1 = new Intent()
                        .putExtra("aid", aid)
                        .putExtra("bvid", bvid)
                        .putExtra("cid", cids.get(position))
                        .putExtra("title", pages.get(position));
                intent1.setClass(this, JumpToPlayerActivity.class);
                startActivity(intent1);
            });
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

}