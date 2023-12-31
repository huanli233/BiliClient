package com.RobinNotBad.BiliClient.activity.message;

import android.content.Intent;
import android.os.Bundle;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.MenuActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;

import com.RobinNotBad.BiliClient.activity.message.MessageListActivity;
import com.google.android.material.card.MaterialCardView;

public class MessageActivity extends BaseActivity {
    public static MessageActivity instance = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        instance = this;

        findViewById(R.id.top).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, MenuActivity.class);
            intent.putExtra("from",5);
            startActivity(intent);
        });
                
        MaterialCardView reply = findViewById(R.id.reply);
        reply.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, MessageListActivity.class);
            intent.putExtra("type","reply");
            startActivity(intent);
        });
        
        MaterialCardView like = findViewById(R.id.like);
        like.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, MessageListActivity.class);
            intent.putExtra("type","like");
            startActivity(intent);
        });
                
        MaterialCardView at = findViewById(R.id.at);
        at.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, MessageListActivity.class);
            intent.putExtra("type","at");
            startActivity(intent);
        });
    }
}
