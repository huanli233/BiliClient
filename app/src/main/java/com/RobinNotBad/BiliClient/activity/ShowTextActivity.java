package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

public class ShowTextActivity extends BaseActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_text);

        Intent intent = getIntent();
        String content = intent.getStringExtra("content");

        TextView pagename = findViewById(R.id.pageName);
        pagename.setText(intent.getStringExtra("title"));


        TextView textView = findViewById(R.id.textView);

        if (content != null) {
            content.replace("[extra_insert]", "<extra_insert>");
            int extra_start = content.indexOf("<extra_insert>");
            if (extra_start != -1) {
                try {
                    JSONObject jsonObject = new JSONObject(content.substring(extra_start + 14));
                    if (jsonObject.getString("type").equals("video")) {
                        View videoCard = View.inflate(this, R.layout.cell_message_reply, findViewById(R.id.linearLayout));
                        MaterialCardView cardView = videoCard.findViewById(R.id.cardView);
                        cardView.setOnClickListener((view) -> BiliTerminal.jumpToVideo(this, jsonObject.optString("content")));
                        TextView title_video = videoCard.findViewById(R.id.content);
                        title_video.setText(jsonObject.optString("title"));
                        content = content.substring(0, extra_start);
                    } else {
                        content = content + "\n文本中存在无法识别的附加信息，请更新版本查看";
                    }
                } catch (JSONException e) {
                    content = content + "\n文本中存在无法识别的附加信息，请更新版本查看";
                }
            }
        }
        if (intent.getData() != null) {
            content = intent.getData().toString();
        }

        textView.setText(content);
        ToolsUtil.setCopy(textView, this);


    }
}