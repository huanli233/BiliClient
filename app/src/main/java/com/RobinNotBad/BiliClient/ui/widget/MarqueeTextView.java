package com.RobinNotBad.BiliClient.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

@SuppressLint("AppCompatCustomView")
public class MarqueeTextView extends TextView {
    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setMarquee();
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMarquee();
    }

    public MarqueeTextView(Context context) {
        super(context);
        setMarquee();
    }

    public void setMarquee() {
        if (!isInEditMode()) {
            if (SharedPreferencesUtil.getBoolean("marquee_enable", true)) {
                setSelected(true);
                setEllipsize(TextUtils.TruncateAt.MARQUEE);
                setSingleLine();
                setMarqueeRepeatLimit(-1);
                setFocusable(true);
                setFocusableInTouchMode(true);
            }
            else {
                setEllipsize(TextUtils.TruncateAt.END);
                setSingleLine();
            }
        }
    }
}
