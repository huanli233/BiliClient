package com.RobinNotBad.BiliClient.adapter.video;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.player.PlayerActivity;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.TerminalContext;

import java.util.List;

//视频卡片Adapter 适用于各种场景（迫真
//日期不记得了

//2023-10-01 把一些公用代码移动到VideoCardHolder里了

public class VideoCardAdapter extends RecyclerView.Adapter<VideoCardHolder> {

    final Context context;
    final List<VideoCard> videoCardList;
    OnItemLongClickListener longClickListener;
    private final boolean isLocal;

    public VideoCardAdapter(Context context, List<VideoCard> videoCardList) {
        this.context = context;
        this.videoCardList = videoCardList;
        this.isLocal = false; // Default to online
    }

    public VideoCardAdapter(Context context, List<VideoCard> videoCardList, boolean isLocal) {
        this.context = context;
        this.videoCardList = videoCardList;
        this.isLocal = isLocal;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public VideoCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list, parent, false);
        return new VideoCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoCardHolder holder, int position) {
        VideoCard videoCard = videoCardList.get(position);
        holder.showVideoCard(videoCard, context);    //此函数在VideoCardHolder里

        holder.itemView.setOnClickListener(view -> {
            if (isLocal) {
                Intent intent = new Intent(context, PlayerActivity.class);
                String videoPath = videoCard.localPath;
                String danmakuPath = videoPath.replace(".blv", ".xml");
                intent.putExtra("url", videoPath);
                intent.putExtra("danmaku", danmakuPath);
                intent.putExtra("title", videoCard.title);
                context.startActivity(intent);
            } else {
                switch (videoCard.type) {
                    case "video":
                        TerminalContext.getInstance().enterVideoDetailPage(context, videoCard.aid, videoCard.bvid, "video");
                        break;
                    case "media_bangumi":
                        TerminalContext.getInstance().enterVideoDetailPage(context, videoCard.aid, null, "media");
                        break;
                }
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
                return true;    //必须要true表示事件已处理 不再继续传递，不然上面的点按也会触发
            } else return false;
        });
    }

    @Override
    public int getItemCount() {
        return videoCardList.size();
    }

}
