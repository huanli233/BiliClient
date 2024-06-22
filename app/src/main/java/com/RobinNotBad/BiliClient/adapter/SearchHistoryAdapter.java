package com.RobinNotBad.BiliClient.adapter;

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
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import java.util.ArrayList;


public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.BtnListHolder> {

    final Context context;
    final ArrayList<String> historyList;
    OnItemLongClickListener longClickListener;
    OnItemClickListener clickListener;

    public SearchHistoryAdapter(Context context, ArrayList<String> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener){
        this.longClickListener = listener;
    }

    public void setOnClickListener(OnItemClickListener listener){
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public BtnListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_choose,parent,false);
        return new BtnListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BtnListHolder holder, int position) {
        holder.show(historyList.get(position));

        holder.itemView.setOnClickListener(view -> {
            if(clickListener != null){
                clickListener.onItemClick(position);
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            if(longClickListener != null) {
                longClickListener.onItemLongClick(position);
                return true;    //必须要true哦，不然上面的点按也会触发
            }
            else return false;
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class BtnListHolder extends RecyclerView.ViewHolder{
        final TextView text_view;

        public BtnListHolder(@NonNull View itemView) {
            super(itemView);
            text_view = itemView.findViewById(R.id.text);
        }

        public void show(String text){
            text_view.setText(ToolsUtil.htmlToString(text));
        }
    }
}
