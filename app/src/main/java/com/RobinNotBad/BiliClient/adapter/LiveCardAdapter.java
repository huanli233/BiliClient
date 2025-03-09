package com.RobinNotBad.BiliClient.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardHolder;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.LiveRoom;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.TerminalContext;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import java.util.List;

public class LiveCardAdapter extends RecyclerView.Adapter<VideoCardHolder> {

    final Context context;
    final List<LiveRoom> roomList;
    OnItemLongClickListener longClickListener;

    public LiveCardAdapter(Context context, List<LiveRoom> roomList) {
        this.context = context;
        this.roomList = roomList;
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
        LiveRoom room = roomList.get(position);

        VideoCard videoCard = new VideoCard();
        videoCard.title = ToolsUtil.removeHtml(room.title);
        if (!room.user_cover.startsWith("http")) videoCard.cover = "http:" + room.user_cover;
        else videoCard.cover = room.user_cover;
        if (TextUtils.isEmpty(videoCard.cover) || videoCard.cover.equals("http:"))
            videoCard.cover = room.cover;
        videoCard.upName = room.uname;
        videoCard.view = ToolsUtil.toWan(room.online) + "人观看";
        videoCard.type = "live";

        holder.showVideoCard(videoCard, context);    //此函数在VideoCardHolder里

        holder.itemView.setOnClickListener(view -> TerminalContext.getInstance().enterLiveDetailPage(context, room.roomid));

        holder.itemView.setOnLongClickListener(view -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
                return true;    //必须要true表示事件已处理 不再继续传递，不然上面的点按也会触发
            } else return false;
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

}
