package com.RobinNotBad.BiliClient.activity.video.info;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.MessageAdapter;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.model.MessageCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


//AI视频总结
//2024-01-14
public class AiSummaryActivity extends BaseActivity {
    private long aid;
    private String bvid;
    private int cid;
    private long up_mid;
    private JSONObject summaryObject;
    private RecyclerView recyclerView;
    private ArrayList<MessageCard> messageList;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        Intent intent = getIntent();
        up_mid = intent.getLongExtra("mid", 0);
        aid = intent.getLongExtra("aid", 0);
        cid = intent.getIntExtra("cid", 0);
        bvid = intent.getStringExtra("bvid");

        recyclerView = findViewById(R.id.recyclerView);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        ((TextView)findViewById(R.id.pageName)).setText("AI总结");

        messageList = new ArrayList<>();

        CenterThreadPool.run(() -> {
            try {
                if (bvid == null || TextUtils.isEmpty(bvid)) summaryObject = VideoInfoApi.getAiSummary(aid, cid, up_mid);
                else summaryObject = VideoInfoApi.getAiSummary(bvid, cid, up_mid);
                if (summaryObject.has("code") && summaryObject.getInt("code") == 0) {
                    if(summaryObject.has("model_result")){
                        for (int i = 0;i<summaryObject.getJSONObject("model_result").getJSONArray("outline").length();i++){
                            JSONObject outline = summaryObject.getJSONObject("model_result").getJSONArray("outline").getJSONObject(i);
                            MessageCard messageCard = new MessageCard();
                            messageCard.user = new ArrayList<>();
                            messageCard.timeDesc = "对应时间：" + LittleToolsUtil.timestampToTime(outline.getLong("timestamp"));
                            messageCard.id = i;
                            messageCard.content = outline.getString("title") + "\n\n----------\n";
                            for(int j = 0;j<outline.getJSONArray("part_outline").length();j++){
                                JSONObject part = outline.getJSONArray("part_outline").getJSONObject(j);
                                messageCard.content += "时间：" + LittleToolsUtil.timestampToTime(part.getLong("timestamp")) + "\n" + part.getString("content") + "\n----------\n";
                            }
                            messageList.add(messageCard);
                        }
                        messageAdapter = new MessageAdapter(this, messageList);
                        runOnUiThread(()-> {
                            recyclerView.setLayoutManager(new LinearLayoutManager(this));
                            recyclerView.setAdapter(messageAdapter);
                        });
                    }
                    else if (summaryObject.getInt("code") == 0) {
                        runOnUiThread(() -> MsgUtil.toast("敏感内容，不支持AI摘要",getApplicationContext()));
                        finish();
                    } else {
                        runOnUiThread(() -> MsgUtil.toast("无摘要，未识别到语音",getApplicationContext()));
                        finish();
                    }
                } else {
                    runOnUiThread(() -> MsgUtil.toast("无效请求",getApplicationContext()));
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> MsgUtil.toast("发生错误",getApplicationContext()));
                finish();
            }
        });
    }
}
