package com.RobinNotBad.BiliClient.activity.player;

import android.view.View;

public class ViewScaleGestureListener extends com.RobinNotBad.BiliClient.activity.player.ScaleGestureDetector.SimpleOnScaleGestureListener {

    private final View view;
    public boolean scaling;
    public boolean can_reset;

    public ViewScaleGestureListener(View view) {
        super();
        this.view = view;
    }
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if(view == null || view.getVisibility() == View.GONE) {return false;}
        float scaleFactor = detector.getScaleFactor();

        float currentScale = view.getScaleX();
        float newScale = currentScale * scaleFactor;
        newScale = Math.max(1f, Math.min(5f, newScale));
        view.setScaleX(newScale);
        view.setScaleY(newScale);

        can_reset = newScale != 1.0f;
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        scaling = true;
        return super.onScaleBegin(detector);
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        scaling = false;
        super.onScaleEnd(detector);
    }
}

