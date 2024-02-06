package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.DynamicOld;

import java.util.ArrayList;

//动态Adapter 显示部分在单独的DynamicHolder里
//2023-09-28

public class DynamicAdapter extends RecyclerView.Adapter<DynamicHolder> {

    Context context;
    ArrayList<DynamicOld> dynamicList;

    public DynamicAdapter(Context context, ArrayList<DynamicOld> dynamicList) {
        this.context = context;
        this.dynamicList = dynamicList;
    }

    @NonNull
    @Override
    public DynamicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_dynamic,parent,false);
        return new DynamicHolder(view,false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DynamicHolder holder, int position) {
        holder.showDynamic(dynamicList.get(position),context);      //该函数在DynamicHolder里

        if(dynamicList.get(position).childDynamic != null){
            Log.e("debug","有子动态！");
            View childCard = View.inflate(context,R.layout.cell_dynamic_child,holder.extraCard);
            DynamicHolder childHolder = new DynamicHolder(childCard,true);
            childHolder.showDynamic(dynamicList.get(position).childDynamic,context);
        }
    }


    @Override
    public void onViewRecycled(@NonNull DynamicHolder holder) {
        holder.extraCard.removeAllViews();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return dynamicList.size();
    }

}
