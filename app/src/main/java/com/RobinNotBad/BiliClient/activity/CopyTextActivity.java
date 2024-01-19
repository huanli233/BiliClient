package com.RobinNotBad.BiliClient.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.widget.EditText;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;

public class CopyTextActivity extends BaseActivity {
    private String content = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        Intent intent = getIntent();
        
        content = intent.getStringExtra("content");

        EditText edittext = findViewById(R.id.content);
        edittext.setText(content);
        
        findViewById(R.id.copy_all).setOnClickListener(view -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("label",content);
            clipboardManager.setPrimaryClip(clipData);
            MsgUtil.toast("已复制",this);
        });
    }
}
