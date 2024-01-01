package com.RobinNotBad.BiliClient.activity.message;

import android.content.Intent;
import android.os.Bundle;

import android.widget.TextView;
import android.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.MessageAdapter;
import com.RobinNotBad.BiliClient.api.MessageApi;
import com.RobinNotBad.BiliClient.model.MessageCard;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import java.util.ArrayList;

import org.json.JSONException;

import java.io.IOException;

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

        new Thread(()->{
            try {
                Intent intent = getIntent();
                String pageType = intent.getStringExtra("type");
                if(pageType.equals("like")) messageList = MessageApi.getLikeMsg();
                else if(pageType.equals("reply")) messageList = MessageApi.getReplyMsg();
                else if(pageType.equals("at")) messageList = MessageApi.getAtMsg();
                else if(pageType.equals("system")) messageList = MessageApi.getSystemMsg();

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
        }).start();
    }
}
