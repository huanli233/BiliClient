package com.RobinNotBad.BiliClient.activity.video.info;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.google.android.material.card.MaterialCardView;

public class WriteReplyActivity extends BaseActivity {

    boolean sent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_write);


        Intent intent = getIntent();
        long oid = intent.getLongExtra("oid",0);
        long rpid = intent.getLongExtra("rpid",0);
        long parent = intent.getLongExtra("parent",0);
        String parentSender = intent.getStringExtra("parentSender");

        findViewById(R.id.top).setOnClickListener(view -> finish());

        EditText editText = findViewById(R.id.editText);
        MaterialCardView send = findViewById(R.id.send);

        Log.e("debug-发送评论",String.valueOf(rpid));

        send.setOnClickListener(view -> {
            if(!sent) {
                CenterThreadPool.run(() -> {
                    String text = editText.getText().toString();
                    if(!text.isEmpty()) {
                        try {
                            if(!parentSender.isEmpty()) text = "回复 @" + parentSender + " :" + text;

                            Log.e("debug-评论内容",text);

                            int result = ReplyApi.sendReply(oid, rpid, parent, text);

                            sent = true;
                            if (result == 0)
                                runOnUiThread(() -> Toast.makeText(WriteReplyActivity.this, "发送成功>w<", Toast.LENGTH_SHORT).show());
                            else
                                runOnUiThread(() -> Toast.makeText(WriteReplyActivity.this, "发送失败TAT\n错误码："+result, Toast.LENGTH_SHORT).show());
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else runOnUiThread(()-> Toast.makeText(this, "你还没输入内容呢~", Toast.LENGTH_SHORT).show());
                });
            }
            else Toast.makeText(WriteReplyActivity.this, "正在发送中，\n别急嘛~", Toast.LENGTH_SHORT).show();
        });
    }
}