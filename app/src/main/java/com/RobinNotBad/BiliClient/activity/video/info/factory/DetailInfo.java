package com.RobinNotBad.BiliClient.activity.video.info.factory;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewbinding.ViewBinding;

public abstract class DetailInfo<VB extends ViewBinding> implements DetailPage {
    protected AppCompatActivity activity;
    protected VB binding;
    private boolean viewInited = false;

    public DetailInfo(AppCompatActivity activity) {
        this.activity = activity;
        binding = createViewBinding(activity);
    }

    protected abstract VB createViewBinding(Context context);

    public View getRootView() {
        return binding.getRoot();
    };

    protected abstract void initView();

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if(!viewInited) {
            initView();
            viewInited = true;
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        viewInited = false;
        activity = null;
    }
}