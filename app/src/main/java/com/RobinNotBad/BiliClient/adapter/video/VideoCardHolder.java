package com.RobinNotBad.BiliClient.adapter.video;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class VideoCardHolder extends RecyclerView.ViewHolder{
    TextView title,upName,playTimes;
    ImageView cover,playIcon,upIcon;

    public VideoCardHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.listVideoTitle);
        upName = itemView.findViewById(R.id.listUpName);
        playTimes = itemView.findViewById(R.id.listPlayTimes);
        cover = itemView.findViewById(R.id.listCover);
        playIcon = itemView.findViewById(R.id.imageView3);
        upIcon = itemView.findViewById(R.id.avatarIcon);
    }

    public void showVideoCard(VideoCard videoCard, Context context){
        title.setText(ToolsUtil.htmlToString(videoCard.title));
        String upNameStr = videoCard.upName;
        if(upNameStr.isEmpty()){
            upName.setVisibility(View.GONE);
            upIcon.setVisibility(View.GONE);
        }
        else upName.setText(upNameStr);

        String playTimesStr = videoCard.view;
        if(playTimesStr.isEmpty()){
            playIcon.setVisibility(View.GONE);
            playTimes.setVisibility(View.GONE);
        }
        else playTimes.setText(playTimesStr);

        Glide.with(context).load(GlideUtil.url(videoCard.cover))
                .placeholder(R.mipmap.placeholder)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5,context))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(cover);
    }
}
