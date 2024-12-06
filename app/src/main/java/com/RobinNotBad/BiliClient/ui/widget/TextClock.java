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
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;

/**
 * 从安卓自带的DigitalClock（被TextClock替代而废弃）复制来的，进行了一些修改
 * 这样的方式貌似要好一些？
 */
@SuppressLint("AppCompatCustomView")
public class TextClock extends TextView {
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

    private Handler mHandler;

    private static final long delta = System.currentTimeMillis() - SystemClock.uptimeMillis();

    @Override
    public void onScreenStateChanged(int screenState) {
        if(screenState == SCREEN_STATE_ON)
            startTick();    //既然没法保活，那就检测屏幕亮起

        if(screenState == SCREEN_STATE_OFF && mHandler != null) {
            mHandler.removeMessages(0);
            mHandler = null;
        }
        super.onScreenStateChanged(screenState);
    }

    public TextClock(Context context) {
        super(context);
        init();
    }

    public TextClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init(){
        //setTypeface(Typeface.DEFAULT_BOLD);
        //Log.i("debug-clock","init,delta=" + delta);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        startTick();
    }

    public void startTick(){
        mHandler = new Handler(msg -> {
            if(mHandler==null) {
                this.startTick();
                return false;
            }
            long now = System.currentTimeMillis();
            setText(dateFormat.format(now));
            invalidate();
            long next = now + (60000 - now % 60000) - delta;
            mHandler.sendEmptyMessageAtTime(0,next);
            //Log.i("debug-clock-tick","now:" + SystemClock.uptimeMillis() + " | next:" + next);
            //这样的方式非常巧妙，计算好下一时刻然后postAtTime
            //原先是一秒一次，我改成了一分钟一次
            //由于基准是systemclock（开机时间），如果开机时不是整分钟，可能会有误差几十秒。经过修改，增加了偏差值计算，避免了这个问题（但其实没啥必要的说
            return true;
        });
        mHandler.sendEmptyMessage(0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mHandler != null) {
            mHandler.removeMessages(0);
            mHandler = null;
        }
    }

}
