package com.RobinNotBad.BiliClient.adapter.video;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.video.local.DownloadListActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalPageChooseActivity;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.LocalVideo;
import com.RobinNotBad.BiliClient.model.PlayerData;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

//本地视频Adapter
//2023-10-04

public class LocalVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final Context context;
    final ArrayList<LocalVideo> localVideoList;
    OnItemLongClickListener longClickListener;

    public LocalVideoAdapter(Context context, ArrayList<LocalVideo> localVideoList) {
        this.context = context;
        this.localVideoList = localVideoList;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0) {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_local, parent, false);
            return new LocalVideoHolder(view);
        }
        else {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_goto, parent, false);
            return new GotoHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(position!=0) {
            int realPosition = position - 1;
            ((LocalVideoHolder)holder).showLocalVideo(localVideoList.get(realPosition), context);    //此函数在VideoCardHolder里

            holder.itemView.setOnClickListener(view -> {
                LocalVideo localVideo = localVideoList.get(realPosition);
                if (localVideo.videoFileList.size() == 1) {
                    PlayerData playerData = new PlayerData(PlayerData.TYPE_LOCAL);
                    playerData.videoUrl = localVideo.videoFileList.get(0);
                    playerData.danmakuUrl = localVideo.danmakuFileList.get(0);
                    playerData.title = localVideo.title;

                    try {
                        Intent player = PlayerApi.jumpToPlayer(playerData);
                        context.startActivity(player);
                    } catch (ActivityNotFoundException e) {
                        MsgUtil.showMsg("跳转失败");
                        e.printStackTrace();
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClass(context, LocalPageChooseActivity.class);
                    intent.putExtra("pageList", localVideo.pageList);
                    intent.putExtra("videoFileList", localVideo.videoFileList);
                    intent.putExtra("danmakuFileList", localVideo.danmakuFileList);
                    intent.putExtra("title", localVideo.title);
                    context.startActivity(intent);
                }
            });

            holder.itemView.setOnLongClickListener(view -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(realPosition);
                    return true;    //必须要true哦，不然上面的点按也会触发
                } else return false;
            });
        }
        else ((GotoHolder)holder).show(context);
    }

    @Override
    public int getItemCount() {
        return localVideoList.size()+1;
    }

    public static class GotoHolder extends RecyclerView.ViewHolder{

        TextView textView;

        public GotoHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }

        public void show(Context context){
            textView.setText("下载列表");
            itemView.setOnClickListener(v ->
                    context.startActivity(new Intent(context, DownloadListActivity.class)));
        }
    }

    public static class LocalVideoHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView extra;
        final ImageView cover;

        public LocalVideoHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
            extra = itemView.findViewById(R.id.text_extra);
            cover = itemView.findViewById(R.id.img_cover);
        }

        public void showLocalVideo(LocalVideo videoCard, Context context) {
            title.setText(videoCard.title);
            extra.setVisibility(View.GONE); //TODO:大小显示

            try {
                Glide.with(BiliTerminal.context).asDrawable().load(videoCard.cover)
                        .transition(GlideUtil.getTransitionOptions())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5))))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(cover);
            } catch (Exception ignored){}
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 1 : 0;
    }
}
