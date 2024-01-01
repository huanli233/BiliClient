package com.RobinNotBad.BiliClient.activity.message;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.MenuActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.MessageApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.google.android.material.card.MaterialCardView;
import org.json.JSONObject;


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
            ((TextView)findViewById(R.id.reply_text)).setText("回复我的(0未读)");
        });


        MaterialCardView like = findViewById(R.id.like);
        like.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, MessageListActivity.class);
            intent.putExtra("type","like");
            startActivity(intent);
            ((TextView)findViewById(R.id.like_text)).setText("收到的赞(0未读)");
        });

        MaterialCardView at = findViewById(R.id.at);
        at.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, MessageListActivity.class);
            intent.putExtra("type","at");
            startActivity(intent);
            ((TextView)findViewById(R.id.at_text)).setText("@我(0未读)");
        });

        MaterialCardView system = findViewById(R.id.system);
        system.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, MessageListActivity.class);
            intent.putExtra("type","system");
            startActivity(intent);
            ((TextView)findViewById(R.id.system_text)).setText("系统通知(0未读)");
        });

        CenterThreadPool.run(() -> {
            try {
                JSONObject stats = MessageApi.getUnread();
                runOnUiThread(() -> {
                    try {
                        ((TextView) findViewById(R.id.reply_text)).setText("回复我的(" + stats.getInt("reply") + "未读)");
                        ((TextView) findViewById(R.id.like_text)).setText("收到的赞(" + stats.getInt("like") + "未读)");
                        ((TextView) findViewById(R.id.at_text)).setText("@我(" + stats.getInt("at") + "未读)");
                        ((TextView) findViewById(R.id.system_text)).setText("系统通知(" + stats.getInt("system") + "未读)");
                    }catch (Exception e){
                        Toast.makeText(this, "解析消息数据失败!\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
