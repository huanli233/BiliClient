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
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.Map;

public class WriteReplyActivity extends BaseActivity {

    boolean sent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_write);

        if(SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,0)==0){
            MsgUtil.toast("还没有登录喵~",this);
            finish();
        }

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
            if(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.cookie_refresh,true)){
                if(!sent) {
                    CenterThreadPool.run(() -> {
                        String text = editText.getText().toString();
                        if(!text.isEmpty()) {
                            try {
                                if(!parentSender.isEmpty()) text = "回复 @" + parentSender + " :" + text;

                                Log.e("debug-评论内容",text);

                                int result = ReplyApi.sendReply(oid, rpid, parent, text);

                                sent = true;

                                if(result==0) {
                                    runOnUiThread(() -> Toast.makeText(WriteReplyActivity.this, "发送成功>w<", Toast.LENGTH_SHORT).show());
                                    finish();
                                }
                                else {
                                    Map<Integer,String> msgMap = new HashMap<Integer,String>(){{
                                        put(-101,"没有登录or登录信息有误？");
                                        put(-102,"账号被封禁！");
                                        put(-509,"请求过于频繁！");
                                        put(12015,"需要评论验证码...？");
                                        put(12016,"包含敏感内容！");
                                        put(12025,"字数过多啦QAQ");
                                        put(12035,"被拉黑了...");
                                    }};
                                    String toast_msg = "评论发送失败：\n" + (msgMap.containsKey(result) ? msgMap.get(result) : result);
                                    runOnUiThread(() -> Toast.makeText(WriteReplyActivity.this, toast_msg, Toast.LENGTH_SHORT).show());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else runOnUiThread(()-> Toast.makeText(this, "你还没输入内容呢~", Toast.LENGTH_SHORT).show());
                    });
                }
                else Toast.makeText(WriteReplyActivity.this, "正在发送中，\n别急嘛~", Toast.LENGTH_SHORT).show();
            }else runOnUiThread(() -> MsgUtil.showDialog(this,"无法发送","上一次的Cookie刷新失败了，您可能需要重新登录以进行敏感操作",-1,false,0));
        });
    }
}