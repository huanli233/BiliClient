package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
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
import java.util.List;

public class QualityChooseAdapter extends RecyclerView.Adapter<QualityChooseAdapter.Holder> {

    Context context;

    List<String> nameList = new ArrayList<>();

    OnItemClickListener onItemClickListener;
    OnItemLongClickListener onItemLongClickListener;

    public QualityChooseAdapter(Context context){
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener = listener;
    }

    // 勾使。
    @SuppressLint("NotifyDataSetChanged")
    public void setNameList(List<String> newList) {
        this.nameList = newList;
        notifyDataSetChanged();
    }

    public String getName(int index) {
        return nameList == null ? null : nameList.get(index);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_choose, parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        holder.folder_name.setText(nameList.get(position));

        holder.itemView.setOnClickListener(view -> {
            if(onItemClickListener != null) onItemClickListener.onItemClick(position);
        });

        holder.itemView.setOnLongClickListener(view -> {
            if(onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(position);
                return true;
            }
            else return false;
        });

    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public static class Holder extends RecyclerView.ViewHolder{
        TextView folder_name;

        public Holder(@NonNull View itemView) {
            super(itemView);
            folder_name = itemView.findViewById(R.id.text);
        }
    }
}
