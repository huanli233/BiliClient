package com.RobinNotBad.BiliClient.adapter.video;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.Opus;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class OpusAdapter extends RecyclerView.Adapter<OpusAdapter.OpusHolder> {

    Context context;
    ArrayList<Opus> opusList;

    public OpusAdapter(Context context, ArrayList<Opus> opusList) {
        this.context = context;
        this.opusList = opusList;
    }

    @NonNull
    @Override
    public OpusHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_opus,parent,false);
        return new OpusHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OpusHolder holder, int position) {
        Opus opus = opusList.get(position);
        holder.favTimeText.setText(opus.timeText);
        holder.titleText.setText(opus.title);
        Glide.with(context).load(opus.cover)
                .placeholder(R.mipmap.placeholder)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5,context))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.coverView);
        
        
    }

    @Override
    public int getItemCount() {
        return opusList.size();
    }


    public static class OpusHolder extends RecyclerView.ViewHolder{
        ImageView coverView;
        TextView favTimeText;
        TextView titleText;
        public OpusHolder(View itemView) {
            super(itemView);
            coverView = itemView.findViewById(R.id.listCover);
            favTimeText = itemView.findViewById(R.id.favTime);
            titleText = itemView.findViewById(R.id.listOpusTitle);
        }

    }
}