package com.RobinNotBad.BiliClient.ui.widget.recycler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbstractAdapter<VH extends BaseHolder> extends RecyclerView.Adapter<BaseHolder> {
    public static final int VIEW_TYPE_FOOTER = 1025;
    public static final int VIEW_TYPE_HEADER = 1024;
    protected Context mContext;
    protected View footerView;
    protected View headerView;

    public abstract void doBindViewHolder(VH viewHolder, int position);

    public abstract VH doCreateViewHolder(ViewGroup parent, int viewType);

    public void bindHeaderView(BaseHolder viewHolder) {
    }

    public void bindFooterView(BaseHolder viewHolder) {
    }

    public AbstractAdapter(Context context) {
        this.mContext = context;
    }

    @NonNull
    @Override
    public final BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return new BaseHolder(this.headerView);
        } else if (viewType == VIEW_TYPE_FOOTER) {
            return new BaseHolder(this.footerView);
        }
        return doCreateViewHolder(parent, viewType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void onBindViewHolder(@NonNull BaseHolder holder, int position) {
        int viewType = holder.getItemViewType();
        if (viewType == VIEW_TYPE_HEADER) {
            bindHeaderView(holder);
            return;
        } else if (viewType == VIEW_TYPE_FOOTER) {
            bindFooterView(holder);
            return;
        }
        int realPosition = position;
        if (headerView != null) {
            realPosition--;
        }
        if (footerView != null) {
            realPosition--;
        }
        doBindViewHolder((VH) holder, realPosition);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setHeaderView(View headerView) {
        if (this.headerView != headerView) {
            this.headerView = headerView;
            notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFooterView(View footerView) {
        if (this.footerView != footerView) {
            this.footerView = footerView;
            notifyDataSetChanged();
        }
    }

    public int getExtraViewCount() {
        int i = this.headerView != null ? 1 : 0;
        return this.footerView != null ? i + 1 : i;
    }

    public int getHeaderViewCount() {
        return this.headerView == null ? 0 : 1;
    }

    public int getFooterViewCount() {
        return this.footerView == null ? 0 : 1;
    }

}
