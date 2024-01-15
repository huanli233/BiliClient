package com.RobinNotBad.BiliClient.activity.video.info.factory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * 详情页的基类， 通过继承该类， 可以实现不同的详情页
 * 比如视频详情页， 番剧详情页， 专栏详情页等等, 通过继承这个类
 * 实现一个Activity, 承载各种各样不同的布局， 从而实现不同的详情页
 */
public abstract class DetailInfo implements DefaultLifecycleObserver {

    protected AppCompatActivity activity;

    // 用来标记是否已经初始化了视图
    private boolean viewInited = false;

    // 根视图, 通过getRootView().findViewById()获取子视图
    private final View rootView;

    /**
     * 构造方法， 传入一个Activity， 用来创建视图
     * @param activity 传入一个Activity
     */

    public DetailInfo(AppCompatActivity activity) {
        this.activity = activity;
        rootView = createView(activity.getLayoutInflater());
    }

    /**
     * 创建视图的抽象方法, 由子类实现
     * 在这里创建视图， 通过LayoutInflater.from(context).inflate()创建视图
     * 同时可以在这里做网络请求, 先把数据保存着，
     * 在initView()方法中再将数据设置到视图上。这样用户的卡顿会小一点
     * @param inflater Activity 的 LayoutInflater
     * @return
     */

    protected abstract View createView(LayoutInflater inflater);

    /**
     * 获取根视图. 通过getRootView().findViewById()获取子视图
     * 就能拿到各种控件了
     * @return 根视图
     */
    public View getRootView() {
        return rootView;
    };

    /**
     * 初始化视图的抽象方法, 由子类实现
     * 在这里做setText啊， setImage等等
     */
    protected abstract void initView();

    /**
     * 在Activity的onCreate调用时， 通过lifeCycle，
     * 会调用到该方法， 该方法需要初始化视图
     * 同时, 也需要将viewInited设置为true， 以便下次重新在Activity创建视图时不再初始化layout.
     */
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if(!viewInited) {
            initView();
            viewInited = true;
        }
    }

    /**
     * 在Activity的onDestroy调用时， 通过lifeCycle，
     * 也会调用到该方法， 该方法需要释放资源， 防止内存泄漏
     * 同时, 也需要将viewInited设置为false， 以便下次重新在Activity试图被销毁时重建layout.
     */

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        viewInited = false;
        activity = null;
    }
}