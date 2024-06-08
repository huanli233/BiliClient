package com.RobinNotBad.BiliClient.util.view;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;

public class ImageAutoLoadScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState) {
            case SCROLL_STATE_IDLE: // The RecyclerView is not currently scrolling.
                try {
                    if (recyclerView.getContext() != null) Glide.with(recyclerView.getContext()).resumeRequests();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case SCROLL_STATE_DRAGGING: // The RecyclerView is currently being dragged by outside input such as user touch input.
            case SCROLL_STATE_SETTLING: // The RecyclerView is currently animating to a final position while not under outside control.
                try {
                    if (recyclerView.getContext() != null) Glide.with(recyclerView.getContext()).pauseRequests();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public static void install(@NonNull RecyclerView recyclerView) {
        if(SharedPreferencesUtil.getBoolean("image_no_load_onscroll",true))
            recyclerView.addOnScrollListener(new ImageAutoLoadScrollListener());
    }
}