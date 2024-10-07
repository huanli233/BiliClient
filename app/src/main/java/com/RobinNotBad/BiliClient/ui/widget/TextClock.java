package com.RobinNotBad.BiliClient.ui.widget;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextClock extends TextView {

    private final TextView textView;
    private String time;
    private TimeHandler mTimehandler = new TimeHandler();

    public TextClock(Context context) {
        this(context, null);
    }

    public TextClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.textView = this;
        init(context);
    }

    private void init(Context context) {
        try {
            // 初始化textview显示时间
            updateClock();
            // 更新进程开始
            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mTimehandler.startScheduleUpdate();
                                }
                            })
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 更新Handler通过handler的延时发送消息来更新时间

    private class TimeHandler extends Handler {
        private boolean mStopped;
        private void post() {
            // 每隔1秒发送一次消息
            sendMessageDelayed(obtainMessage(0), 60000);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!mStopped) {
                updateClock();
                // 实现实时更新
                post();
            }
        }

        // 开始更新
        public void startScheduleUpdate() {
            mStopped = false;
            post();
        }

        // 停止更新

        public void stopScheduleUpdate() {
            mStopped = true;
            removeMessages(0);
        }
    }

    // 返回当前的时间，并结束handler的信息发送

    public String getTime() {
        // 停止发送消息
        mTimehandler.stopScheduleUpdate();
        return time;
    }

    private void updateClock() {
        // 获取当前的时间
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");
        textView.setText(simpleDateFormat.format(date));
    }
}