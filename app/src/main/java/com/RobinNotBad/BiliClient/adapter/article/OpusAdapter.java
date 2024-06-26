package com.RobinNotBad.BiliClient.adapter.article;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.article.ArticleInfoActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicInfoActivity;
import com.RobinNotBad.BiliClient.api.ArticleApi;
import com.RobinNotBad.BiliClient.model.Opus;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;

import java.io.IOException;
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
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_opus,parent,false);
        return new OpusHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OpusHolder holder, int position) {
        Opus opus = opusList.get(position);
        holder.favTimeText.setText(opus.timeText);
        holder.titleText.setText(opus.title);
        Glide.with(context).load(opus.cover)
                .transition(GlideUtil.getTransitionOptions())
                .placeholder(R.mipmap.placeholder)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5,context))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.coverView);
        if(opus.content.equals("内容失效")) {
        	holder.itemView.setOnClickListener(v->{
                MsgUtil.toast("内容失效，无法打开",context);
            });
        }else{
            holder.itemView.setOnClickListener(v->{
                CenterThreadPool.run(()->{
                    try {
                    	parsedOpus = ArticleApi.opusId2cvid(opus.opusId);
                        cvid = parsedOpus.parsedId;
                    } catch(JSONException err) {
                    	err.printStackTrace();
                    } catch(IOException err){
                        err.printStackTrace();
                    }
                    if(cvid == -1) ((Activity) context).runOnUiThread(() -> MsgUtil.toast("打开失败",context));
                    else {
                        CenterThreadPool.runOnUiThread(()->{
                            if(parsedOpus.type == Opus.TYPE_ARTICLE) {
                                Intent intent = new Intent(context,ArticleInfoActivity.class);
                                intent.putExtra("cvid",cvid);
                                context.startActivity(intent);
                            }if(parsedOpus.type == Opus.TYPE_DYNAMIC) {
                                Intent intent = new Intent(context,DynamicInfoActivity.class);
                                intent.putExtra("id",opus.opusId);
                                context.startActivity(intent);
                            }
                        });
                    }
                });
            });
        }
        
        
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
    public void insertItem(ArrayList<Opus> list){
        int oldSize = opusList.size();
        opusList.addAll(list);
        this.notifyItemRangeInserted(oldSize,list.size()-1);
    }
}