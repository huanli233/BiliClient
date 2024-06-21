package com.RobinNotBad.BiliClient.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

public class PreInflateHelper {
    public static final int DEFAULT_PRELOAD_COUNT = 5;

    private final ViewCache mViewCache = new ViewCache();

    private ILayoutInflater mLayoutInflater;

    public PreInflateHelper(Context context) {
        mLayoutInflater = DefaultLayoutInflater.get(context);
    }

    public void preloadOnce(@NonNull ViewGroup parent, int layoutId) {
        preloadOnce(parent, layoutId, DEFAULT_PRELOAD_COUNT);
    }

    public void preloadOnce(@NonNull ViewGroup parent, int layoutId, int maxCount) {
        preload(parent, layoutId, maxCount, 1);
    }

    public void preload(@NonNull ViewGroup parent, int layoutId) {
        preload(parent, layoutId, DEFAULT_PRELOAD_COUNT, 0);
    }

    public void preload(@NonNull ViewGroup parent, int layoutId, int maxCount) {
        preload(parent, layoutId, maxCount, 0);
    }

    public void preload(@NonNull ViewGroup parent, int layoutId, int maxCount, int forcePreCount) {
        int viewsAvailableCount = mViewCache.getViewPoolAvailableCount(layoutId);
        if (viewsAvailableCount >= maxCount) {
            return;
        }
        int needPreloadCount = maxCount - viewsAvailableCount;
        if (forcePreCount > 0) {
            needPreloadCount = Math.min(forcePreCount, needPreloadCount);
        }
        for (int i = 0; i < needPreloadCount; i++) {
            // 异步加载View
            preAsyncInflateView(parent, layoutId);
        }
    }

    private void preAsyncInflateView(@NonNull ViewGroup parent, int layoutId) {
        mLayoutInflater.asyncInflateView(parent, layoutId, (layoutId1, view) -> mViewCache.putView(layoutId1, view));
    }

    public View getView(@NonNull ViewGroup parent, int layoutId, boolean attachToRoot) {
        return getView(parent, layoutId, DEFAULT_PRELOAD_COUNT, attachToRoot);
    }
    public View getView(@NonNull ViewGroup parent, int layoutId, int maxCount, boolean attachToRoot) {
        View view = mViewCache.getView(layoutId);
        if (view != null) {
            preloadOnce(parent, layoutId, maxCount);
            return view;
        }
        return mLayoutInflater.inflateView(parent, layoutId, attachToRoot);
    }


    public View getView(@NonNull ViewGroup parent, int layoutId) {
        return getView(parent, layoutId, DEFAULT_PRELOAD_COUNT);
    }

    public View getView(@NonNull ViewGroup parent, int layoutId, int maxCount) {
        View view = mViewCache.getView(layoutId);
        if (view != null) {
            preloadOnce(parent, layoutId, maxCount);
            return view;
        }
        return mLayoutInflater.inflateView(parent, layoutId, false);
    }

    public PreInflateHelper setAsyncInflater(ILayoutInflater asyncInflater) {
        mLayoutInflater = asyncInflater;
        return this;
    }

    public interface ILayoutInflater {
        /**
         * 异步加载View
         *
         * @param parent   父布局
         * @param layoutId 布局资源id
         * @param callback 加载回调
         */
        void asyncInflateView(@NonNull ViewGroup parent, int layoutId, InflateCallback callback);

        /**
         * 同步加载View
         *
         * @param parent   父布局
         * @param layoutId 布局资源id
         * @param attachToRoot attachToRoot
         * @return 加载的View
         */
        View inflateView(@NonNull ViewGroup parent, int layoutId, boolean attachToRoot);
    }
    interface InflateCallback {
        void onInflateFinished(int layoutId, View view);
    }

    public static class DefaultLayoutInflater implements ILayoutInflater {

        private AsyncLayoutInflaterX mInflater;
        private final Context context;

        private DefaultLayoutInflater(Context context) {
            this.context = context;
        }

        public static DefaultLayoutInflater get(Context context) {
            return new DefaultLayoutInflater(context);
        }

        @Override
        public void asyncInflateView(@NonNull ViewGroup parent, int layoutId, InflateCallback callback) {
            if (mInflater == null) {
                Context context = this.context;
                mInflater = new AsyncLayoutInflaterX(context);
            }
            mInflater.inflate(layoutId, parent, (view, resId, parent1) -> {
                if (callback != null) {
                    callback.onInflateFinished(resId, view);
                }
            });
        }

        @Override
        public View inflateView(@NonNull ViewGroup parent, int layoutId, boolean attachToRoot) {
            return LayoutInflater.from(context).inflate(layoutId, parent, attachToRoot);
        }
    }
}
