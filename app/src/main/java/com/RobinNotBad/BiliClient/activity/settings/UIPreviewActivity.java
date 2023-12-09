package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;

public class UIPreviewActivity extends BaseActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_ui_preview);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        LinearLayout layout = findViewById(R.id.previewList);
        View videoCard = getLayoutInflater().inflate(R.layout.cell_video_list,null);
        layout.addView(videoCard);
        View replyCard = getLayoutInflater().inflate(R.layout.cell_reply_list,null);
        layout.addView(replyCard);
        View userCard = getLayoutInflater().inflate(R.layout.cell_user_list,null);
        layout.addView(userCard);
    }
}