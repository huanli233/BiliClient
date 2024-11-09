package com.RobinNotBad.BiliClient.adapter.article;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.api.ArticleApi;
import com.RobinNotBad.BiliClient.model.Opus;
import com.RobinNotBad.BiliClient.util.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class OpusAdapter extends RecyclerView.Adapter<OpusAdapter.OpusHolder> {

    Context context;
    ArrayList<Opus> opusList;
    Opus parsedOpus;
    long cvid = -1;

    public OpusAdapter(Context context, ArrayList<Opus> opusList) {
        this.context = context;
        this.opusList = opusList;
    }

    @NonNull
    @Override
    public OpusHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_opus, parent, false);
        return new OpusHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OpusHolder holder, int position) {
        Opus opus = opusList.get(position);
        holder.favTimeText.setText(opus.timeText);
        holder.titleText.setText(opus.title);
        Glide.with(context).load(GlideUtil.url(opus.cover))
                .transition(GlideUtil.getTransitionOptions())
                .placeholder(R.mipmap.placeholder)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.coverView);
        if (opus.content.equals("内容失效")) {
            holder.itemView.setOnClickListener(v -> MsgUtil.showMsg("内容失效，无法打开"));
        } else {
            holder.itemView.setOnClickListener(v -> CenterThreadPool.run(() -> {
                try {
                    parsedOpus = ArticleApi.opusId2cvid(opus.opusId);
                    cvid = parsedOpus.parsedId;
                } catch (Exception e) {
                    MsgUtil.err(e);
                    cvid = -1;
                }
                if (cvid == -1)
                    ((Activity) context).runOnUiThread(() -> MsgUtil.showMsg("打开失败"));
                else {
                    if (parsedOpus.type == Opus.TYPE_ARTICLE) {
                        TerminalContext.getInstance().enterArticleDetailPage(context, cvid);
                    }
                    if (parsedOpus.type == Opus.TYPE_DYNAMIC) {
                        TerminalContext.getInstance().enterDynamicDetailPage(context, opus.opusId);
                    }
                }
            }));
        }


    }

    @Override
    public int getItemCount() {
        return opusList.size();
    }


    public static class OpusHolder extends RecyclerView.ViewHolder {
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