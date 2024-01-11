package com.RobinNotBad.BiliClient.activity.base;

import android.os.Bundle;

public class InstanceActivity extends BaseActivity {
    private static InstanceActivity instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
    }
    public static InstanceActivity getInstance() {
        return instance;
    }

    @Override
    protected void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
