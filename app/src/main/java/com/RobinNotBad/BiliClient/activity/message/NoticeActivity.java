package com.RobinNotBad.BiliClient.activity.message;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.MessageAdapter;
import com.RobinNotBad.BiliClient.api.MessageApi;
import com.RobinNotBad.BiliClient.model.MessageCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;

public class NoticeActivity extends BaseActivity{
    private RecyclerView recyclerView;
    private ArrayList<MessageCard> messageList;
    private MessageAdapter messageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        setPageName("详情");

        recyclerView = findViewById(R.id.recyclerView);

        messageList = new ArrayList<>();

        if(!SharedPreferencesUtil.getBoolean("tutorial_message_list",false)){
            MsgUtil.showTutorial(this,"使用教程","点击头像打开打开用户的主页，点击下方卡片可以跳转到对应视频（无法跳转动态）",R.mipmap.tutorial_message_list);
            SharedPreferencesUtil.putBoolean("tutorial_message_list",true);
        }

        CenterThreadPool.run(()->{
            try {
                Intent intent = getIntent();
                String pageType = intent.getStringExtra("type");
                switch (pageType) {
                    case "like":
                        messageList = MessageApi.getLikeMsg();
                        break;
                    case "reply":
                        messageList = MessageApi.getReplyMsg();
                        break;
                    case "at":
                        messageList = MessageApi.getAtMsg();
                        break;
                    case "system":
                        messageList = MessageApi.getSystemMsg();
                        break;
                }

                messageAdapter = new MessageAdapter(this, messageList);
                runOnUiThread(()-> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(messageAdapter);
                });
            } catch (Exception e) {runOnUiThread(()-> MsgUtil.err(e,this));}
        });
    }
}
