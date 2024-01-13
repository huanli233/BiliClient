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
                            String toast_msg = "评论发送失败TAT";
                            switch (result){
                                case 0:
                                    runOnUiThread(() -> Toast.makeText(WriteReplyActivity.this, "发送成功>w<", Toast.LENGTH_SHORT).show());
                                    finish();
                                case -101:
                                    toast_msg = "没有登录哦";
                                    break;
                                case -102:
                                    toast_msg = "账号已被封禁";
                                    break;
                                case -509:
                                    toast_msg = "请求太频繁哩TAT";
                                    break;
                                case 12015:
                                    toast_msg = "需要评论验证码TAT";
                                    break;
                                case 12016:
                                    toast_msg = "包含敏感内容";
                                    break;
                                case 12025:
                                    toast_msg = "字数过多啦！QAQ";
                                    break;
                                case 12035:
                                    toast_msg = "被拉黑哩..";
                                    break;
                            }
                            String finalToast_msg = toast_msg;
                            runOnUiThread(() -> Toast.makeText(WriteReplyActivity.this, finalToast_msg, Toast.LENGTH_SHORT).show());
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