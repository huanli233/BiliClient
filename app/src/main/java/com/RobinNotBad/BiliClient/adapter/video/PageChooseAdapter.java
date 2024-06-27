package com.RobinNotBad.BiliClient.adapter.video;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;

import java.util.ArrayList;

//分页视频选集
//2023-08-29

public class PageChooseAdapter extends RecyclerView.Adapter<PageChooseAdapter.Holder> {

    final Context context;
    final ArrayList<String> nameList;

    OnItemClickListener onItemClickListener;
    OnItemLongClickListener onItemLongClickListener;

    public PageChooseAdapter(Context context, ArrayList<String> nameList) {
        this.context = context;
        this.nameList = nameList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_choose, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        holder.folder_name.setText(nameList.get(position));


        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) onItemClickListener.onItemClick(position);
        });

        holder.itemView.setOnLongClickListener(view -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(position);
                return true;
            } else return false;
        });

    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        final TextView folder_name;

        public Holder(@NonNull View itemView) {
            super(itemView);
            folder_name = itemView.findViewById(R.id.text);
        }
    }
}
