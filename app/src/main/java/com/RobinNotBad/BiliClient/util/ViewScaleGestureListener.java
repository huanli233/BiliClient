package com.RobinNotBad.BiliClient.util;

import android.view.ScaleGestureDetector;
import android.view.View;

public class ViewScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    private final View view;
    public ViewScaleGestureListener(View view) {
        super();
        this.view = view;
    }
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if(view == null || view.getVisibility() == View.GONE) return false;
        float scaleFactor = detector.getScaleFactor();
        float currentScale = view.getScaleX();
        float newScale = currentScale * scaleFactor;
        newScale = Math.max(1f, Math.min(5f, newScale));
        view.setScaleX(newScale);
        view.setScaleY(newScale);
        return true;
    }
}
