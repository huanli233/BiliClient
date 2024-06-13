package com.RobinNotBad.BiliClient.adapter.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.user.info.UserCollectionActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.adapter.dynamic.DynamicHolder;
import com.RobinNotBad.BiliClient.model.VideoCard;

import java.util.List;

//用户视频列表专用Adapter

public class UserVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    long mid;
    List<VideoCard> videoCardList;

    public UserVideoAdapter(Context context, long mid, List<VideoCard> videoCardList) {
        this.context = context;
        this.mid = mid;
        this.videoCardList = videoCardList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0) {
            View view = LayoutInflater.from(context).inflate(R.layout.cell_goto, parent, false);
            return new RecyclerView.ViewHolder(view){};
        }
        else {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list,parent,false);
            return new VideoCardHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(position==0){
            TextView textView = holder.itemView.findViewById(R.id.text);
            textView.setText("合集列表");
            holder.itemView.setOnClickListener(view->{
                Intent intent = new Intent(context, UserCollectionActivity.class);
                intent.putExtra("mid",mid);
                context.startActivity(intent);
            });
        }
        else {
            int realPosition = position - 1;
            VideoCardHolder videoCardHolder = (VideoCardHolder) holder;
            VideoCard videoCard = videoCardList.get(realPosition);
            videoCardHolder.showVideoCard(videoCard,context);    //此函数在VideoCardHolder里

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, VideoInfoActivity.class);
                intent.putExtra("bvid", videoCard.bvid);
                intent.putExtra("aid", videoCard.aid);
                context.startActivity(intent);
            });
        }
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if(holder instanceof DynamicHolder) ((DynamicHolder)holder).extraCard.removeAllViews();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return videoCardList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position==0 ? 0 : 1);
    }
}
