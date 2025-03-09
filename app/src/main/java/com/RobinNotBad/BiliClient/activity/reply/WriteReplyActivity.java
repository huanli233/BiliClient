package com.RobinNotBad.BiliClient.activity.reply;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.EmoteActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.EmoteApi;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.event.ReplyEvent;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class WriteReplyActivity extends BaseActivity {

    private static final Map<Integer, String> msgMap = new HashMap<>() {{
        put(-101, "没有登录or登录信息有误？");
        put(-102, "账号被封禁！");
        put(-509, "请求过于频繁！");
        put(12015, "需要评论验证码...？");
        put(12016, "包含敏感内容！");
        put(12025, "字数过多啦QAQ");
        put(12035, "被拉黑了...");
        put(12051, "重复评论，请勿刷屏！");
    }};

    EditText editText;
    private final ActivityResultLauncher<Intent> emoteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        int code = result.getResultCode();
        Intent data = result.getData();
        if (code == RESULT_OK && data != null && data.hasExtra("text")) {
            editText.append(data.getStringExtra("text"));
        }
    });

    boolean sent = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_write);

        if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
            MsgUtil.showMsg("还没有登录喵~");
            finish();
        }

        Intent intent = getIntent();
        long oid = intent.getLongExtra("oid", 0);
        long rpid = intent.getLongExtra("rpid", 0);
        long parent = intent.getLongExtra("parent", 0);
        int replyType = intent.getIntExtra("replyType", ReplyApi.REPLY_TYPE_VIDEO);
        String parentSender = intent.getStringExtra("parentSender");
        int pos = intent.getIntExtra("pos", -1);

        editText = findViewById(R.id.editText);
        MaterialCardView send = findViewById(R.id.send);

        Log.e("debug-发送评论", String.valueOf(rpid));

        if (parentSender != null && !parentSender.isEmpty()) {
            editText.setText("回复 @" + parentSender + " :");
            editText.setSelection(editText.getText().length());
        }

        send.setOnClickListener(view -> {
            if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.cookie_refresh, true)) {
                if (!sent) {
                    CenterThreadPool.run(() -> {
                        String text = editText.getText().toString();
                        if (!text.isEmpty()) {
                            try {
                                Log.e("debug-评论内容", text);

                                Pair<Integer, Reply> result = ReplyApi.sendReply(oid, rpid, parent, text, replyType);
                                int resultCode = result.first;
                                Reply resultReply = result.second;

                                sent = true;

                                if (resultCode == 0) {
                                    runOnUiThread(() -> MsgUtil.showMsg("发送成功>w<"));
                                    resultReply.forceDelete = true;
                                    resultReply.pubTime = "刚刚";
                                    EventBus.getDefault().post(new ReplyEvent(1, resultReply, pos, oid));
                                    finish();
                                } else {
                                    String toast_msg = "评论发送失败：\n" + (msgMap.containsKey(resultCode) ? msgMap.get(resultCode) : resultCode);
                                    runOnUiThread(() -> MsgUtil.showMsg(toast_msg));
                                    sent = false;
                                }
                            } catch (Exception e) {
                                runOnUiThread(() -> MsgUtil.err(e));
                            }
                        } else runOnUiThread(() -> MsgUtil.showMsg("还没输入内容呢~"));
                    });
                } else MsgUtil.showMsg("正在发送中");
            } else
                MsgUtil.showDialog("无法发送", "上一次的Cookie刷新失败了，\n您可能需要重新登录以进行敏感操作", -1);
        });

        findViewById(R.id.emote).setOnClickListener(view ->
                emoteLauncher.launch(new Intent(this, EmoteActivity.class).putExtra("from", EmoteApi.BUSINESS_REPLY)));
    }
}