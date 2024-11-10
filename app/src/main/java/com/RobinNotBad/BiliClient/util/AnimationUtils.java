package com.RobinNotBad.BiliClient.util;

import static com.RobinNotBad.BiliClient.util.CenterThreadPool.runOnUiThread;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

import androidx.annotation.NonNull;

public class AnimationUtils {

    public static void fadeIn(View view, final int duration) {
        if (view == null) return;
        if (!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.LOAD_TRANSITION, true)) {
            view.setVisibility(View.VISIBLE);
            return;
        }
        runOnUiThread(() -> {
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeIn.setDuration(duration);
            view.setVisibility(View.VISIBLE);
            fadeIn.start();
        });
    }

    public static void fadeOut(final View view, final int duration) {
        if (view == null) return;
        runOnUiThread(() -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOut.setDuration(duration);
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
        crossFade(view, view2, 100, 100);
    }

    public static void crossFade(final View view, final View view2, final int duration1, final int duration2) {
        if (view == null || view2 == null) return;
        runOnUiThread(() -> {
            if (!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.LOAD_TRANSITION, true)) {
                view.setVisibility(View.GONE);
                view2.setVisibility(View.VISIBLE);
                return;
            }
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOut.setDuration(duration1);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view2, "alpha", 0f, 1f);
            fadeIn.setDuration(duration2);
            fadeOut.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {}
                @Override
                public void onAnimationEnd(@NonNull Animator animation) {

                    view.setVisibility(View.GONE);
                }
                @Override
                public void onAnimationCancel(@NonNull Animator animation) {}
                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {}
            });
            fadeOut.addUpdateListener(valueAnimator -> {
                float fraction = valueAnimator.getAnimatedFraction();
                if (fraction >= 0.5f && !fadeIn.isStarted()) {
                    view2.setVisibility(View.VISIBLE);
                    fadeIn.start();
                }
            });
            fadeOut.start();
        });
    }
}
