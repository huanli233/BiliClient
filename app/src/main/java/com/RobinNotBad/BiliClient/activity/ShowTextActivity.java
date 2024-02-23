package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;

public class ShowTextActivity extends BaseActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_text);

        Intent intent = getIntent();

        TextView pagename = findViewById(R.id.pageName);
        pagename.setText(intent.getStringExtra("title"));

        TextView textView = findViewById(R.id.textView);
        textView.setText(intent.getStringExtra("content"));

        if(intent.getData() != null){
            textView.setText(intent.getData().toString());
        }
    }
}