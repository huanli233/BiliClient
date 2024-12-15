/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.RobinNotBad.BiliClient.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;

/**
 * 从安卓自带的DigitalClock（被TextClock替代而废弃）复制来的，进行了一些修改
 * 这样的方式貌似要好一些？
 */
@SuppressLint("AppCompatCustomView")
public class TextClock extends TextView {

    public TextClock(Context context) {
        super(context);
        init();
    }

    public TextClock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextClock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TextClock(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private boolean stopped = false;

    public void init(){
        setTypeface(Typeface.DEFAULT_BOLD);
    }

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            removeCallbacks(this);
            if(stopped) return;

            long now = System.currentTimeMillis();
            setText(dateFormat.format(now));

            long next = 60000 - now % 60000;
            postDelayed(this,next);
            //Log.i("debug-clock-tick","now:" + SystemClock.uptimeMillis() + " | next:" + next);
            //再次修改，原先的handler容易被系统杀…
        }
    };


    public void startTick(){
        stopped = false;
        ticker.run();
    }

    public void stopTick(){
        stopped = true;
        removeCallbacks(ticker);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startTick();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTick();
    }


    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);

        if(screenState == SCREEN_STATE_ON) startTick();
        else stopTick();
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);

        if(isVisible) startTick();
        else stopTick();
    }


}
