package com.RobinNotBad.BiliClient.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.VideoCard;

import java.util.ArrayList;

//视频卡片Adapter 适用于各种场景（迫真
//日期不记得了

//2023-10-01 把一些公用代码移动到VideoCardHolder里了

public class VideoCardAdapter extends RecyclerView.Adapter<VideoCardHolder> {

    Context context;
    ArrayList<VideoCard> videoCardList;
    OnItemLongClickListener longClickListener;

    public VideoCardAdapter(Context context, ArrayList<VideoCard> videoCardList) {
        this.context = context;
        this.videoCardList = videoCardList;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener){
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public VideoCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list,parent,false);
        return new VideoCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoCardHolder holder, int position) {
        VideoCard videoCard = videoCardList.get(position);
        holder.showVideoCard(videoCard,context);    //此函数在VideoCardHolder里

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent();
            switch (videoCard.type) {
                case "video":
                    intent.setClass(context, VideoInfoActivity.class);
                    intent.putExtra("bvid", videoCard.bvid);
                    intent.putExtra("aid", videoCard.aid);
                    context.startActivity(intent);
                    break;
                case "media_bangumi":
                    intent.putExtra("aid", videoCard.aid);
                    context.startActivity(intent);
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
        return videoCardList.size();
    }

}
