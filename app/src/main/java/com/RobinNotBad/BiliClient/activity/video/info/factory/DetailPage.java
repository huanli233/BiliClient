package com.RobinNotBad.BiliClient.activity.video.info.factory;

import android.app.Application;
import android.view.View;
import androidx.lifecycle.DefaultLifecycleObserver;

public interface DetailPage extends DefaultLifecycleObserver {
    public View getRootView();
}
