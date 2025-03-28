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

    public static void crossFade(final View toShow, final View toHide) {
        crossFade(toShow, toHide, 100);
    }

    public static void crossFade(final View toHide, final View toShow, final int duration) {
        runOnUiThread(() -> {
            if (!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.LOAD_TRANSITION, true)) {
                if(toHide!=null) toHide.setVisibility(View.GONE);
                if(toShow!=null) toShow.setVisibility(View.VISIBLE);
                return;
            }
            if(toHide != null) {
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(toHide, "alpha", 1f, 0f);
                fadeOut.setDuration(duration);
                fadeOut.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animation) {}
                    @Override
                    public void onAnimationEnd(@NonNull Animator animation) {
                        toHide.setVisibility(View.GONE);
                    }
                    @Override
                    public void onAnimationCancel(@NonNull Animator animation) {}
                    @Override
                    public void onAnimationRepeat(@NonNull Animator animation) {}
                });
                fadeOut.start();
            }

            if(toShow != null) {
                ObjectAnimator showUp = ObjectAnimator.ofFloat(toShow, "alpha", 0f, 1f);
                showUp.setDuration(duration);
                toShow.setVisibility(View.VISIBLE);
                showUp.start();
            }

        });
    }
}
