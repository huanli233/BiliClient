package com.RobinNotBad.BiliClient.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class BatteryView extends View {
    private int mPower = 100;

    public BatteryView(Context context) {
        super(context);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int battery_width = (int) (getWidth() * 0.9f);
        int battery_height = getHeight();
        int battery_head_width = (int) (getWidth() * 0.08f);
        int battery_head_height = (int) (getHeight() * 0.4f);
        int battery_inside_margin = (int) (getWidth() * 0.08f);

        Paint paint = new Paint();
        if (mPower <= 20) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.WHITE);
        }
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(getWidth() * 0.05f);
        Rect rect = new Rect(0, 0,
                battery_width, battery_height);
        canvas.drawRect(rect, paint);

        float power_percent = mPower / 100.0f;

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0f);
        //画电量
        if (power_percent != 0) {
            int p_right = (int) ((battery_width - battery_inside_margin) * power_percent);
            int p_bottom = battery_inside_margin + battery_height - battery_inside_margin * 2;
            Rect rect2 = new Rect(battery_inside_margin, battery_inside_margin, p_right, p_bottom);
            canvas.drawRect(rect2, paint);
        }
        int h_top = battery_height / 2 - battery_head_height / 2;
        int h_right = battery_width + battery_head_width;
        int h_bottom = h_top + battery_head_height;
        Rect rect4 = new Rect(battery_width, h_top, h_right, h_bottom);
        canvas.drawRect(rect4, paint);
    }

    public void setPower(int power) {
        mPower = power;
        if (mPower < 0) {
            mPower = 0;
        }
        invalidate();
    }
}
