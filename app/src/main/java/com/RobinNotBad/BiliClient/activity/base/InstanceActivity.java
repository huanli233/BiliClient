package com.RobinNotBad.BiliClient.activity.base;

import android.os.Bundle;
import com.RobinNotBad.BiliClient.BiliClient;

import java.util.*;

public class InstanceActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BiliClient.addInstance(this);
        super.onCreate(savedInstanceState);
    }
    public static InstanceActivity getInstance(Class<? extends InstanceActivity> cls) {
        return BiliClient.getInstance(cls);
    }

    @Override
    protected void onDestroy() {
        BiliClient.removeInstance(this);
        super.onDestroy();
    }
}
