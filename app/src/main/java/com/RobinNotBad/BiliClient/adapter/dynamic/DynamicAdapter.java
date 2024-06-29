package com.RobinNotBad.BiliClient.adapter.dynamic;

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
import com.RobinNotBad.BiliClient.activity.ListChooseActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.send.SendDynamicActivity;
import com.RobinNotBad.BiliClient.activity.live.FollowLiveActivity;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//动态Adapter 显示部分在单独的DynamicHolder里
//2023-09-28

public class DynamicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final Context context;
    final List<Dynamic> dynamicList;
    final RecyclerView recyclerView;
    final DynamicActivity dynamicActivity;
    final ActivityResultLauncher<Intent> writeDynamicLauncher;

    public DynamicAdapter(Context context, List<Dynamic> dynamicList, RecyclerView recyclerView) {
        this.context = context;
        this.dynamicList = dynamicList;
        this.recyclerView = recyclerView;
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
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_dynamic_action, parent, false);
            return new WriteDynamic(view);
        } else {
            return new DynamicHolder(LayoutInflater.from(this.context).inflate(R.layout.cell_dynamic, parent, false), dynamicActivity, false);
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
            writeDynamic.type.setOnClickListener((view) -> dynamicActivity.selectTypeLauncher.launch(new Intent().setClass(context, ListChooseActivity.class).putExtra("title", "选择类型").putExtra("items", new ArrayList<>(Arrays.asList("全部", "视频投稿", "追番", "专栏")))));
            writeDynamic.live.setOnClickListener(view -> {
                Intent intent = new Intent(context, FollowLiveActivity.class);
                context.startActivity(intent);
            });
        } else if (holder instanceof DynamicHolder) {
            long time = System.currentTimeMillis();
            position--;
            DynamicHolder dynamicHolder = (DynamicHolder) holder;
            dynamicHolder.showDynamic(dynamicList.get(position), context, true);      //该函数在DynamicHolder里

            if (dynamicList.get(position).dynamic_forward != null) {
                View childCard = dynamicHolder.cell_dynamic_child;
                DynamicHolder childHolder = new DynamicHolder(childCard, dynamicActivity, true);
                childHolder.showDynamic(dynamicList.get(position).dynamic_forward, context, true);
                childCard.setVisibility(View.VISIBLE);
            } else {
                dynamicHolder.cell_dynamic_child.setVisibility(View.GONE);
            }

            int finalPosition = position;
            View.OnLongClickListener onDeleteLongClick = DynamicHolder.getDeleteListener(dynamicActivity, dynamicList, finalPosition, this);
            dynamicHolder.item_dynamic_delete.setOnLongClickListener(onDeleteLongClick);
            if (dynamicList.get(position).canDelete)
                dynamicHolder.item_dynamic_delete.setVisibility(View.VISIBLE);
            Log.d("BiliClient", "DynamicAdapter onBindViewHolder finish: " + (System.currentTimeMillis() - time));
        }
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return dynamicList.size() + 1;
    }


    public static class WriteDynamic extends RecyclerView.ViewHolder {
        final MaterialButton write_dynamic, type, live;

        public WriteDynamic(@NonNull View itemView) {
            super(itemView);
            write_dynamic = itemView.findViewById(R.id.write_dynamic);
            type = itemView.findViewById(R.id.type);
            live = itemView.findViewById(R.id.live);
        }
    }
}
