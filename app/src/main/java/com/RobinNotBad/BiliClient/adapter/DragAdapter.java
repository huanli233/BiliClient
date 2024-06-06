package com.RobinNotBad.BiliClient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;

import java.util.List;

public class DragAdapter extends RecyclerView.Adapter<DragAdapter.ViewHolder> {

    private final Context mContext;
    private final List<String> mList;
    private static final int fixedPosition = -1; // 固定菜单

    public DragAdapter(Context context, List<String> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_drag_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItemTextView.setText(mList.get(position));

        holder.mItemTextView.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onItemClick(holder.getAdapterPosition());
            }
        });

        holder.mItemTextView.setOnLongClickListener(view -> {
            if (mListener != null) {
                mListener.onItemLongClick(holder);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public int getFixedPosition() {
        return fixedPosition;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mItemTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mItemTextView = itemView.findViewById(R.id.item);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(ViewHolder holder);
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
