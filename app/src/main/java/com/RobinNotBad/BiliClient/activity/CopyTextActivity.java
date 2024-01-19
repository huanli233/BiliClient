package com.RobinNotBad.BiliClient.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;

public class CopyTextActivity extends BaseActivity {
    
    private boolean text_changing = false;
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
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!text_changing) edittext.setText(content);
                text_changing = !text_changing;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        
        findViewById(R.id.copy_all).setOnClickListener(view -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("label",content);
            clipboardManager.setPrimaryClip(clipData);
            MsgUtil.toast("已复制",this);
        });
    }
}
