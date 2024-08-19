package com.RobinNotBad.BiliClient.ui.widget.recycler;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public class BaseHolder extends RecyclerView.ViewHolder {
    private final SparseArray<View> viewArray;

    public BaseHolder(ViewGroup viewGroup, int i) {
        super(LayoutInflater.from(viewGroup.getContext()).inflate(i, viewGroup, false));
        this.viewArray = new SparseArray<>();
    }

    public BaseHolder(View view) {
        super(view);
        this.viewArray = new SparseArray<>();
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T getView(int i) {
        T t = (T) this.viewArray.get(i);
        if (t == null) {
            T t2 = this.itemView.findViewById(i);
            this.viewArray.put(i, t2);
            return t2;
        }
        return t;
    }

    protected Context getContext() {
        return this.itemView.getContext();
    }
}