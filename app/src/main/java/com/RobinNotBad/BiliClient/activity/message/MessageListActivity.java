package com.RobinNotBad.BiliClient.activity.message;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.MessageAdapter;
import com.RobinNotBad.BiliClient.api.MessageApi;
import com.RobinNotBad.BiliClient.model.MessageCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MessageListActivity extends BaseActivity{
    private RecyclerView recyclerView;
    private ArrayList<MessageCard> messageList;
    private MessageAdapter messageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        findViewById(R.id.top).setOnClickListener(view -> finish());
        
        ((TextView)findViewById(R.id.pageName)).setText("详情");
        
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

                messageAdapter = new MessageAdapter(this, messageList);
                runOnUiThread(()-> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(messageAdapter);
                });
            } catch (IOException e){
                runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,this));
                e.printStackTrace();
            } catch (JSONException e) {
                runOnUiThread(()-> MsgUtil.jsonErr(e,this));
                e.printStackTrace();
            }
        });
    }
}
