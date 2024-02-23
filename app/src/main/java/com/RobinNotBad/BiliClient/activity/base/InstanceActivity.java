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

    public void setMenuClick(int from){
        findViewById(R.id.top).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, MenuActivity.class);
            intent.putExtra("from",from);
            startActivity(intent);
        });
    }
}
