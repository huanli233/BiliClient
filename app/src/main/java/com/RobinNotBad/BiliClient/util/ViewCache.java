package com.RobinNotBad.BiliClient.util;

import android.util.SparseArray;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedList;

public class ViewCache {

    private final SparseArray<LinkedList<SoftReference<View>>> mViewPools = new SparseArray<>();

    @NonNull
    public LinkedList<SoftReference<View>> getViewPool(int layoutId) {
        LinkedList<SoftReference<View>> views = mViewPools.get(layoutId);
        if (views == null) {
            views = new LinkedList<>();
            mViewPools.put(layoutId, views);
        }
        return views;
    }

    public int getViewPoolAvailableCount(int layoutId) {
        LinkedList<SoftReference<View>> views = getViewPool(layoutId);
        Iterator<SoftReference<View>> it = views.iterator();
        int count = 0;
        while (it.hasNext()) {
            if (it.next().get() != null) {
                count++;
            } else {
                it.remove();
            }
        }
        return count;
    }

    public void putView(int layoutId, View view) {
        if (view == null) {
            return;
        }
        getViewPool(layoutId).offer(new SoftReference<>(view));
    }

    @Nullable
    public View getView(int layoutId) {
        return getViewFromPool(getViewPool(layoutId));
    }

    private View getViewFromPool(@NonNull LinkedList<SoftReference<View>> views) {
        if (views.isEmpty()) {
            return null;
        }
        View target = views.pop().get();
        if (target == null) {
            return getViewFromPool(views);
        }
        return target;
    }
}
