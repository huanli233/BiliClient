/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.RobinNotBad.BiliClient.activity.player;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;


public class ScaleGestureDetector {

    public interface OnScaleGestureListener {
        boolean onScale(ScaleGestureDetector detector);

        boolean onScaleBegin(ScaleGestureDetector detector);

        void onScaleEnd(ScaleGestureDetector detector);
    }

    public static class SimpleOnScaleGestureListener implements OnScaleGestureListener {

        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {}
    }

    private final Context mContext;
    private final OnScaleGestureListener mListener;

    public float mFocusX;
    public float mFocusY;

    private boolean mQuickScaleEnabled;

    public float mCurrSpan;
    public float mPrevSpan;
    public float mInitialSpan;
    public float mCurrSpanX;
    public float mCurrSpanY;
    public float mPrevSpanX;
    public float mPrevSpanY;
    public long mCurrTime;
    public long mPrevTime;
    public boolean mInProgress;
    public int mSpanSlop;
    public int mMinSpan;

    private final Handler mHandler;

    private float mAnchoredScaleStartX;
    private float mAnchoredScaleStartY;
    private int mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE;

    private static final float SCALE_FACTOR = .5f;
    private static final int ANCHORED_SCALE_MODE_NONE = 0;
    private static final int ANCHORED_SCALE_MODE_DOUBLE_TAP = 1;
    private static final int ANCHORED_SCALE_MODE_STYLUS = 2;

    private GestureDetector mGestureDetector;

    private boolean mEventBeforeOrAboveStartingGestureEvent;

    /**
     * Creates a ScaleGestureDetector with the supplied listener.
     * You may only use this constructor from a {@link android.os.Looper Looper} thread.
     *
     * @param context the application's context
     * @param listener the listener invoked for all the callbacks, this must
     * not be null.
     *
     * @throws NullPointerException if {@code listener} is null.
     */
    public ScaleGestureDetector(Context context, OnScaleGestureListener listener) {
        this(context, listener, null);
    }

    public ScaleGestureDetector(Context context, OnScaleGestureListener listener,
                                Handler handler) {
        mContext = context;
        mListener = listener;
        final ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mSpanSlop = viewConfiguration.getScaledTouchSlop() * 2;
        mMinSpan = 1;
        mHandler = handler;

        final int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        if (targetSdkVersion > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setQuickScaleEnabled(true);
        }
    }

    public void onTouchEvent(MotionEvent event) {

        mCurrTime = event.getEventTime();

        final int action = event.getActionMasked();

        // Forward the event to check for double tap gesture
        if (mQuickScaleEnabled) {
            mGestureDetector.onTouchEvent(event);
        }

        final int count = event.getPointerCount();

        final boolean anchoredScaleCancelled =
                mAnchoredScaleMode == ANCHORED_SCALE_MODE_STYLUS;
        final boolean streamComplete = action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_CANCEL || anchoredScaleCancelled;

        if (action == MotionEvent.ACTION_DOWN || streamComplete) {
            // Reset any scale in progress with the listener.
            // If it's an ACTION_DOWN we're beginning a new event stream.
            // This means the app probably didn't give us all the events. Shame on it.
            if (mInProgress) {
                mListener.onScaleEnd(this);
                mInProgress = false;
                mInitialSpan = 0;
                mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE;
            } else if (inAnchoredScaleMode() && streamComplete) {
                mInitialSpan = 0;
                mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE;
            }

            if (streamComplete) {
                return;
            }
        }

        final boolean configChanged = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_POINTER_DOWN;

        final boolean pointerUp = action == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? event.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0, sumY = 0;
        final int div = pointerUp ? count - 1 : count;
        final float focusX;
        final float focusY;
        if (inAnchoredScaleMode()) {
            // In anchored scale mode, the focal pt is always where the double tap
            // or button down gesture started
            focusX = mAnchoredScaleStartX;
            focusY = mAnchoredScaleStartY;
            mEventBeforeOrAboveStartingGestureEvent = event.getY() < focusY;
        } else {
            for (int i = 0; i < count; i++) {
                if (skipIndex == i) continue;
                sumX += event.getX(i);
                sumY += event.getY(i);
            }

            focusX = sumX / div;
            focusY = sumY / div;
        }

        // Determine average deviation from focal point
        float devSumX = 0, devSumY = 0;
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;

            // Convert the resulting diameter into a radius.
            devSumX += Math.abs(event.getX(i) - focusX);
            devSumY += Math.abs(event.getY(i) - focusY);
        }
        final float devX = devSumX / div;
        final float devY = devSumY / div;

        // Span is the average distance between touch points through the focal point;
        // i.e. the diameter of the circle with a radius of the average deviation from
        // the focal point.
        final float spanX = devX * 2;
        final float spanY = devY * 2;
        final float span;
        if (inAnchoredScaleMode()) {
            span = spanY;
        } else {
            span = (float) Math.hypot(spanX, spanY);
        }

        // Dispatch begin/end events as needed.
        // If the configuration changes, notify the app to reset its current state by beginning
        // a fresh scale event stream.
        final boolean wasInProgress = mInProgress;
        mFocusX = focusX;
        mFocusY = focusY;
        if (!inAnchoredScaleMode() && mInProgress && (span < mMinSpan || configChanged)) {
            mListener.onScaleEnd(this);
            mInProgress = false;
            mInitialSpan = span;
        }
        if (configChanged) {
            mPrevSpanX = mCurrSpanX = spanX;
            mPrevSpanY = mCurrSpanY = spanY;
            mInitialSpan = mPrevSpan = mCurrSpan = span;
        }

        final int minSpan = inAnchoredScaleMode() ? mSpanSlop : mMinSpan;
        if (!mInProgress && span >=  minSpan &&
                (wasInProgress || Math.abs(span - mInitialSpan) > mSpanSlop)) {
            mPrevSpanX = mCurrSpanX = spanX;
            mPrevSpanY = mCurrSpanY = spanY;
            mPrevSpan = mCurrSpan = span;
            mPrevTime = mCurrTime;
            mInProgress = mListener.onScaleBegin(this);
        }

        // Handle motion; focal point and span/scale factor are changing.
        if (action == MotionEvent.ACTION_MOVE) {
            mCurrSpanX = spanX;
            mCurrSpanY = spanY;
            mCurrSpan = span;

            boolean updatePrev = true;

            if (mInProgress) {
                updatePrev = mListener.onScale(this);
            }

            if (updatePrev) {
                mPrevSpanX = mCurrSpanX;
                mPrevSpanY = mCurrSpanY;
                mPrevSpan = mCurrSpan;
                mPrevTime = mCurrTime;
            }
        }

    }

    private boolean inAnchoredScaleMode() {
        return mAnchoredScaleMode != ANCHORED_SCALE_MODE_NONE;
    }

    public void setQuickScaleEnabled(boolean scales) {
        mQuickScaleEnabled = scales;
        if (mQuickScaleEnabled && mGestureDetector == null) {
            GestureDetector.SimpleOnGestureListener gestureListener =
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            // Double tap: start watching for a swipe
                            mAnchoredScaleStartX = e.getX();
                            mAnchoredScaleStartY = e.getY();
                            mAnchoredScaleMode = ANCHORED_SCALE_MODE_DOUBLE_TAP;
                            return true;
                        }
                    };
            mGestureDetector = new GestureDetector(mContext, gestureListener, mHandler);
        }
    }


    public float getScaleFactor() {
        if (inAnchoredScaleMode()) {
            final boolean scaleUp =
                    (mEventBeforeOrAboveStartingGestureEvent && (mCurrSpan < mPrevSpan)) ||
                            (!mEventBeforeOrAboveStartingGestureEvent && (mCurrSpan > mPrevSpan));
            final float spanDiff = (Math.abs(1 - (mCurrSpan / mPrevSpan)) * SCALE_FACTOR);
            return mPrevSpan <= mSpanSlop ? 1 : scaleUp ? (1 + spanDiff) : (1 - spanDiff);
        }
        return mPrevSpan > 0 ? mCurrSpan / mPrevSpan : 1;
    }

}