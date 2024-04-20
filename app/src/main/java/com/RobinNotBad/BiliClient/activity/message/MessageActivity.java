package com.RobinNotBad.BiliClient.activity.message;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgSessionsAdapter;
import com.RobinNotBad.BiliClient.api.MessageApi;
import com.RobinNotBad.BiliClient.api.PrivateMsgApi;
import com.RobinNotBad.BiliClient.model.PrivateMsgSession;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MessageActivity extends InstanceActivity {
    private RecyclerView sessionsView;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setMenuClick(6);
                
        MaterialCardView reply = findViewById(R.id.reply);
        reply.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, NoticeActivity.class);
            intent.putExtra("type","reply");
            startActivity(intent);
            ((TextView)findViewById(R.id.reply_text)).setText("回复我的");
        });


        MaterialCardView like = findViewById(R.id.like);
        like.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, NoticeActivity.class);
            intent.putExtra("type","like");
            startActivity(intent);
            ((TextView)findViewById(R.id.like_text)).setText("收到的赞");
        });

        MaterialCardView at = findViewById(R.id.at);
        at.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, NoticeActivity.class);
            intent.putExtra("type","at");
            startActivity(intent);
            ((TextView)findViewById(R.id.at_text)).setText("@我");
        });

        MaterialCardView system = findViewById(R.id.system);
        system.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, NoticeActivity.class);
            intent.putExtra("type","system");
            startActivity(intent);
        });

        sessionsView = findViewById(R.id.sessions_list);
        sessionsView.setNestedScrollingEnabled(false);
        
        CenterThreadPool.run(() -> {
            try {
                JSONObject stats = MessageApi.getUnread();
                ArrayList<PrivateMsgSession> sessionsList = PrivateMsgApi.getSessionsList(20);
                ArrayList<Long> uidList = new ArrayList<>();
                for(PrivateMsgSession item :sessionsList) {
                	uidList.add(item.talkerUid);
                }    
                HashMap<Long,UserInfo> userMap = PrivateMsgApi.getUsersInfo(uidList);
                PrivateMsgSessionsAdapter adapter = new PrivateMsgSessionsAdapter(this,sessionsList,userMap);
                runOnUiThread(() -> {
                    try {
                        ((TextView) findViewById(R.id.reply_text)).setText("回复我的" + ((stats.getInt("reply") > 0) ? ("(" + stats.getInt("reply") + "未读)") : ""));
                        ((TextView) findViewById(R.id.like_text)).setText("收到的赞" + ((stats.getInt("like") > 0) ? ("(" + stats.getInt("like") + "未读)") : ""));
                        ((TextView) findViewById(R.id.at_text)).setText("@我" + ((stats.getInt("at") > 0) ? ("(" + stats.getInt("at") + "未读)") : ""));
                        sessionsView.setLayoutManager(new LinearLayoutManager(this));
                        sessionsView.setAdapter(adapter);
                    } catch (Exception e) {MsgUtil.err(e,this);}
                });
            } catch (Exception e) {runOnUiThread(()->MsgUtil.err(e,this));}
        });
    }
}
