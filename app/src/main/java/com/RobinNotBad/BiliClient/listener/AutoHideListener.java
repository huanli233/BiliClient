package com.RobinNotBad.BiliClient.listener;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import androidx.recyclerview.widget.RecyclerView;
import java.lang.ref.WeakReference;



//from csdn
//给的代码不全，浅浅补了一下—dudu

public class AutoHideListener extends RecyclerView.OnScrollListener {
    // 定义自动隐藏监听内部类，实现recycleView滑动监听
    WeakReference<Activity> _ref;
    Animation _hide, _show;
    View _view;
    long _hideDuration, _showDuration;
    Thread _idleThread;
    int Hide = 0;
    int Show = 1;
    int _idleTime = 0;
    int DefaultIdleTime = 400;
    boolean _onScrollChange = false;
    int _state = -1;

    public AutoHideListener(
            Activity activity,
            View view,
            Animation hide,
            int hideDuration,
            Animation show,
            int showDuration) {
        _ref = new WeakReference<>(activity);
        _view = view;
        _hide = hide;
        _hideDuration = hideDuration;
        _show = show;
        _showDuration = showDuration;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        final Activity activity = _ref.get();
        if (activity == null) return;

        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                doOnIdle(activity);
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
            case RecyclerView.SCROLL_STATE_SETTLING:
                Log.d("AutoHideListener", "SCROLL_STATE_SETTLING");
                doOnScroll(activity);
                break;
        }
        // _onScrollChange = false;
    }

    private void doOnScroll(final Activity activity) {
        if (_state != Hide) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    if (_onScrollChange) {
                        activity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        _state = Hide;
                                        _view.setClickable(false);
                                        _view.startAnimation(_hide);
                                    }
                                });
                    }
                }
            }.start();
        }
    }

    private void doOnIdle(final Activity activity) {
        _onScrollChange = false;
        if (_state != Show) {
            if (_idleThread != null) {
                synchronized (this) {
                    _idleTime = DefaultIdleTime;
                }
            } else {
                _idleTime = DefaultIdleTime;
                _idleThread =
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                synchronized (this) {
                                    while (_idleTime > 0) {
                                        _idleTime -= 100;
                                        SystemClock.sleep(100);
                                    }
                                }
                                activity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                _view.setClickable(true);
                                                _view.startAnimation(_show);
                                            }
                                        });
                                SystemClock.sleep(_showDuration);
                                _state = Show;
                                _idleThread = null;
                            }
                        };
                _idleThread.start();
            }
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        _onScrollChange = true;
    }
}
