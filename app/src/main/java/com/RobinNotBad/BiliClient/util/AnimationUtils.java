package com.RobinNotBad.BiliClient.util;

import static com.RobinNotBad.BiliClient.util.CenterThreadPool.runOnUiThread;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

public class AnimationUtils {

    public static void fadeIn(View view) {
        if (view == null) return;
        runOnUiThread(() -> {
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeIn.setDuration(100);
            fadeIn.start();
            view.setVisibility(View.VISIBLE);
        });
    }

    public static void fadeOut(final View view) {
        if (view == null) return;
        runOnUiThread(() -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOut.setDuration(50);
            fadeOut.start();
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
        });
    }

    public static void crossFade(final View view, final View view2) {
        if (view == null || view2 == null) return;
        if (!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.LOAD_TRANSITION, true)) {
            view.setVisibility(View.GONE);
            view2.setVisibility(View.VISIBLE);
            return;
        }
        runOnUiThread(() -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOut.setDuration(80);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view2, "alpha", 0f, 1f);
            fadeIn.setDuration(70);
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    view2.setVisibility(View.VISIBLE);
                    fadeIn.start();
                }
            });
            fadeOut.start();
        });
    }
}
