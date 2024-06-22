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

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.video.local.LocalPageChooseActivity;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.LocalVideo;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

//本地视频Adapter
//2023-10-04

public class LocalVideoAdapter extends RecyclerView.Adapter<LocalVideoAdapter.LocalVideoHolder> {

    final Context context;
    final ArrayList<LocalVideo> localVideoList;
    OnItemLongClickListener longClickListener;

    public LocalVideoAdapter(Context context, ArrayList<LocalVideo> localVideoList) {
        this.context = context;
        this.localVideoList = localVideoList;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener){
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public LocalVideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_local_video,parent,false);
        return new LocalVideoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalVideoHolder holder, int position) {
        holder.showLocalVideo(localVideoList.get(position),context);    //此函数在VideoCardHolder里

        holder.itemView.setOnClickListener(view -> {
            LocalVideo localVideo = localVideoList.get(position);
            if(localVideo.videoFileList.size() == 1){
                try {
                    PlayerApi.jumpToPlayer(context, localVideo.videoFileList.get(0), localVideo.danmakuFileList.get(0), localVideo.title,true, 0, "", 0, 0,0,false);
                }catch (ActivityNotFoundException e){
                    MsgUtil.toast("跳转失败",context);
                    e.printStackTrace();
                }
            }
            else{
                Intent intent = new Intent();
                intent.setClass(context, LocalPageChooseActivity.class);
                intent.putExtra("pageList",localVideo.pageList);
                intent.putExtra("videoFileList",localVideo.videoFileList);
                intent.putExtra("danmakuFileList",localVideo.danmakuFileList);
                intent.putExtra("title",localVideo.title);
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
        return localVideoList.size();
    }



    public static class LocalVideoHolder extends RecyclerView.ViewHolder{
        final TextView title;
        final ImageView cover;

        public LocalVideoHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.listVideoTitle);
            cover = itemView.findViewById(R.id.listCover);
        }

        public void showLocalVideo(LocalVideo videoCard, Context context){
            title.setText(ToolsUtil.htmlToString(videoCard.title));

            Glide.with(context).asDrawable().load(videoCard.cover)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5,context))))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(cover);
        }
    }
}
