package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.send.SendDynamicActivity;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;

//动态Adapter 显示部分在单独的DynamicHolder里
//2023-09-28

public class DynamicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<Dynamic> dynamicList;
    DynamicActivity dynamicActivity;
    ActivityResultLauncher<Intent> writeDynamicLauncher;

    public DynamicAdapter(Context context, ArrayList<Dynamic> dynamicList) {
        this.context = context;
        this.dynamicList = dynamicList;
        dynamicActivity = (DynamicActivity) context;
        this.writeDynamicLauncher = dynamicActivity.writeDynamicLauncher;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 ? 0 : 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_dynamic_send, parent,false);
            return new WriteDynamic(view);
        } else {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_dynamic, parent,false);
            return new DynamicHolder(view, dynamicActivity, false);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof WriteDynamic) {
            WriteDynamic writeDynamic = (WriteDynamic) holder;
            writeDynamic.write_dynamic.setOnClickListener((view) -> {
                Intent intent = new Intent();
                intent.setClass(context, SendDynamicActivity.class);
                writeDynamicLauncher.launch(intent);
            });
        } else if (holder instanceof DynamicHolder) {
            position--;
            DynamicHolder dynamicHolder = (DynamicHolder) holder;
            dynamicHolder.showDynamic(dynamicList.get(position), context, true);      //该函数在DynamicHolder里

            if (dynamicList.get(position).dynamic_forward != null){
                Log.e("debug","有子动态！");
                View childCard = View.inflate(context,R.layout.cell_dynamic_child,dynamicHolder.extraCard);
                DynamicHolder childHolder = new DynamicHolder(childCard, dynamicActivity, true);
                childHolder.showDynamic(dynamicList.get(position).dynamic_forward, context, true);
            }

            int finalPosition = position;
            View.OnLongClickListener onDeleteLongClick = DynamicHolder.getDeleteListener(dynamicActivity, dynamicList, finalPosition, this);
            dynamicHolder.item_dynamic_delete_img.setOnLongClickListener(onDeleteLongClick);
            dynamicHolder.item_dynamic_delete.setOnLongClickListener(onDeleteLongClick);
            if (dynamicList.get(position).canDelete) {
                dynamicHolder.item_dynamic_delete.setVisibility(View.VISIBLE);
                dynamicHolder.item_dynamic_delete_img.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof DynamicHolder) {
            ((DynamicHolder) holder).extraCard.removeAllViews();
        }
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return dynamicList.size();
    }



    public static class WriteDynamic extends RecyclerView.ViewHolder {
        MaterialButton write_dynamic;

        public WriteDynamic(@NonNull View itemView) {
            super(itemView);
            write_dynamic = itemView.findViewById(R.id.write_dynamic);
        }
    }

}
