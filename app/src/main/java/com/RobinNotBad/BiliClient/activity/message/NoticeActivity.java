package com.RobinNotBad.BiliClient.activity.message;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.message.NoticeAdapter;
import com.RobinNotBad.BiliClient.api.MessageApi;
import com.RobinNotBad.BiliClient.model.MessageCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;

public class NoticeActivity extends BaseActivity{
    private RecyclerView recyclerView;
    private ArrayList<MessageCard> messageList;
    private NoticeAdapter noticeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        setPageName("详情");

        recyclerView = findViewById(R.id.recyclerView);

        messageList = new ArrayList<>();

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

                noticeAdapter = new NoticeAdapter(this, messageList);
                runOnUiThread(()-> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(noticeAdapter);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
