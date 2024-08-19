package com.RobinNotBad.BiliClient.ui.widget.recycler;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<M, VH extends BaseHolder> extends AbstractAdapter<VH> {
    private List<M> dataList;

    public int getViewType(int position) {
        return 0;
    }

    public BaseAdapter(Context context) {
        super(context);
        this.dataList = new ArrayList<>();
    }

    public BaseAdapter(Context context, List<M> dataList) {
        super(context);
        this.dataList = new ArrayList<>();
        this.dataList.addAll(dataList);
    }

    @SuppressLint("NotifyDataSetChanged")
    public boolean fillList(List<M> list) {
        this.dataList.clear();
        boolean result = this.dataList.addAll(list);
        notifyDataSetChanged();
        return result;
    }

    public boolean appendItem(M item) {
        int size = this.dataList.size();
        boolean result = this.dataList.add(item);
        notifyItemInserted(size + getHeaderViewCount());
        return result;
    }

    public boolean appendList(List<M> list) {
        int size = this.dataList.size();
        boolean result = this.dataList.addAll(list);
        notifyItemRangeInserted(size, list.size());
        return result;
    }

    public void preposeItem(M item) {
        this.dataList.add(0, item);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void preposeList(List<M> list) {
        this.dataList.addAll(0, list);
        notifyItemRangeInserted(0, list.size());
    }

    public void updateItem(int position, M item) {
        this.dataList.set(position, item);
        notifyItemChanged(getHeaderViewCount() + position);
    }

    public void updateItem(M originalItem, M newItem) {
        int index = this.dataList.indexOf(originalItem);
        if (index >= 0) {
            this.dataList.set(index, newItem);
            notifyItemChanged(index);
        }
    }

    public void removeItem(int position) {
        if (this.headerView == null) {
            this.dataList.remove(position);
            notifyItemRemoved(position);
        } else {
            this.dataList.remove(position - 1);
            notifyItemRemoved(position - 1);
        }
    }

    public void removeItem(M item) {
        int index = this.dataList.indexOf(item);
        if (index >= 0) {
            this.dataList.remove(index);
            notifyItemRemoved(index);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        this.dataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public final int getItemViewType(int position) {
        if (this.headerView != null && position == 0) {
            return VIEW_TYPE_HEADER;
        } else if (this.footerView != null && position == this.dataList.size() + getHeaderViewCount()) {
            return VIEW_TYPE_FOOTER;
        }
        return getViewType(position);
    }

    @Override
    public int getItemCount() {
        return this.dataList.size() + getExtraViewCount();
    }

    @Nullable
    public M getItem(int i) {
        List<M> list;
        if ((this.headerView == null || i != 0) && i < this.dataList.size() + getHeaderViewCount()) {
            if (this.headerView == null) {
                list = this.dataList;
            } else {
                list = this.dataList;
                i--;
            }
            return list.get(i);
        }
        return null;
    }

    public M getItem(VH vh) {
        return getItem(vh.getAdapterPosition());
    }

    public List<M> getAllData() {
        return this.dataList;
    }
}
