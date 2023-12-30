package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.model.MessageLikeInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;

public class MessageLikeHolder extends RecyclerView.ViewHolder{
    public LinearLayout avaterList;
    public TextView action,pubdate;
    public ConstraintLayout extraCard;
    public View itemView;
    public MessageLikeHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        avaterList = itemView.findViewById(R.id.avatar_list);
        action = itemView.findViewById(R.id.action);
        pubdate = itemView.findViewById(R.id.pubdate);
        extraCard = itemView.findViewById(R.id.extraCard);
    }
    @SuppressLint("SetTextI18n")
    public void showMessage(MessageLikeInfo message, Context context) {
        for(int i = 0;i<3;i++){
            if(i >= message.userList.size()) break;
            ImageView imageView = new ImageView(context);
            Glide.with(context)
                    .load(message.userList.get(i).avatar)
                    .placeholder(R.drawable.akari)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(LittleToolsUtil.dp2px(32,context),LittleToolsUtil.dp2px(32,context)));
            imageView.setLeft(LittleToolsUtil.dp2px(3,context));
            avaterList.addView(imageView);

            //这个View什么都没有，用来当间隔的
            View view = new View(context);
            view.setLayoutParams(new ViewGroup.LayoutParams(LittleToolsUtil.dp2px(2,context),LittleToolsUtil.dp2px(2,context)));
            avaterList.addView(view);
        }
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        pubdate.setText(sdf.format(message.timeStamp * 1000));

        if (message.videoCard != null){
            action.setText("等 " + message.userList.size() + "人点赞了你的视频");
            VideoCard childVideoCard = message.videoCard;
            VideoCardHolder holder = new VideoCardHolder(View.inflate(context,R.layout.cell_dynamic_video,extraCard));
            holder.showVideoCard(childVideoCard,context);
            holder.itemView.findViewById(R.id.cardView).setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, VideoInfoActivity.class);
                intent.putExtra("bvid", childVideoCard.bvid);
                intent.putExtra("aid", 0);
                context.startActivity(intent);
            });
        }
        if(message.replyInfo != null){
            action.setText("等 " + message.userList.size() + "人点赞了你的评论");
        }
    }
}
