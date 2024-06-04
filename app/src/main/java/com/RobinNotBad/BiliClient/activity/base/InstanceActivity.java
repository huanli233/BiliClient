package com.RobinNotBad.BiliClient.activity.base;

import android.content.Intent;
import android.os.Bundle;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.MenuActivity;

public class InstanceActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BiliTerminal.setInstance(this);
        super.onCreate(savedInstanceState);
    }

    public void setMenuClick() {
        findViewById(R.id.top).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, MenuActivity.class);
            if (getIntent().hasExtra("from")) intent.putExtra("from", getIntent().getIntExtra("from", 0));
            startActivity(intent);
            overridePendingTransition(R.anim.anim_activity_in_down, 0);
        });
    }
}
