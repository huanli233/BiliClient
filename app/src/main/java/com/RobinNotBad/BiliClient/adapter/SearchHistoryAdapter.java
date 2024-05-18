package com.RobinNotBad.BiliClient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.search.SearchActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;


public class SearchHistoryAdapter extends RecyclerView.Adapter<BtnListHolder> {

    Context context;
    ArrayList<String> historyList;
    OnItemLongClickListener longClickListener;
    SearchActivity activity;

    public SearchHistoryAdapter(Context context, ArrayList<String> historyList, SearchActivity activity) {
        this.context = context;
        this.historyList = historyList;
        this.activity = activity;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener){
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public BtnListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_btn_list,parent,false);
        return new BtnListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BtnListHolder holder, int position) {
        holder.show(historyList.get(position));

        holder.itemView.setOnClickListener(view -> {
            if(activity != null){
                activity.searchKeyword(historyList.get(position));
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

}
