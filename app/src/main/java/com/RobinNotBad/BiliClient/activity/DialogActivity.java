package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Timer;
import java.util.TimerTask;

public class DialogActivity extends BaseActivity {

    int wait_time = 0;
    
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        MaterialButton close_btn = findViewById(R.id.close_btn);

        Intent intent = getIntent();

        ((TextView)findViewById(R.id.tip_title)).setText(intent.getStringExtra("title"));
        ((TextView)findViewById(R.id.content)).setText(intent.getStringExtra("content"));

        if(intent.getIntExtra("wait_time",-1) > 0){
            findViewById(R.id.close_btn).setEnabled(false);
            wait_time = intent.getIntExtra("wait_time",0);
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        if(wait_time-- > 0){
                            close_btn.setText("知道了(" + wait_time + "s)");
                            findViewById(R.id.close_btn).setEnabled(false);
                        }else{
                            close_btn.setText("知道了");
                            findViewById(R.id.close_btn).setEnabled(true);
                            timer.cancel();
                        }
                    });
                }
            },0,1000);
        }else close_btn.setEnabled(true);
        close_btn.setOnClickListener(view -> finish());
    }

    @Override
    public void onBackPressed() {}
}