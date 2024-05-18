package com.RobinNotBad.BiliClient.view;

import android.content.Context;
import android.util.AttributeSet;

public class FocusTextView extends androidx.appcompat.widget.AppCompatTextView {
    public FocusTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public FocusTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public FocusTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isFocused(){
        return true;
    }
}
