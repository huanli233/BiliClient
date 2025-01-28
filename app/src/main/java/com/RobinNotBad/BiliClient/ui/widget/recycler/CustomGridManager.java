package com.RobinNotBad.BiliClient.ui.widget.recycler;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.util.MsgUtil;

public class CustomGridManager extends GridLayoutManager {
    public CustomGridManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomGridManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public CustomGridManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Throwable e){
            MsgUtil.err("列表报错：",e);
        }
    }
}
