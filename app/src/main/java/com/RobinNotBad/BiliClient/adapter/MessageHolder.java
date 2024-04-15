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
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.model.MessageCard;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;

public class MessageHolder extends RecyclerView.ViewHolder{
    public LinearLayout avaterList;
    public TextView action,pubdate;
    public ConstraintLayout extraCard;
    public View itemView;
    public MessageHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        avaterList = itemView.findViewById(R.id.avatar_list);
        action = itemView.findViewById(R.id.action);
        pubdate = itemView.findViewById(R.id.pubdate);
        extraCard = itemView.findViewById(R.id.extraCard);
    }
    @SuppressLint("SetTextI18n")
    public void showMessage(MessageCard message, Context context) {
        avaterList.removeAllViews();
        if(message.user.size() < 1) avaterList.setVisibility(View.GONE);
        else avaterList.setVisibility(View.VISIBLE);
        for(int i = 0;i<message.user.size();i++){
            ImageView imageView = new ImageView(context);
            Glide.with(context)
                    .load(GlideUtil.url(message.user.get(i).avatar))
                    .placeholder(R.mipmap.akari)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ToolsUtil.dp2px(32,context), ToolsUtil.dp2px(32,context)));
            imageView.setLeft(ToolsUtil.dp2px(3,context));
            int finalI = i;
            imageView.setOnClickListener(view1 -> {
                Intent intent = new Intent();
                intent.setClass(context, UserInfoActivity.class);
                intent.putExtra("mid", message.user.get(finalI).mid);
                context.startActivity(intent);
            });
            avaterList.addView(imageView);

            //这个View什么都没有，用来当间隔的
            View view = new View(context);
            view.setLayoutParams(new ViewGroup.LayoutParams(ToolsUtil.dp2px(3,context), ToolsUtil.dp2px(32,context)));
            avaterList.addView(view);
        }

        if(message.timeStamp != 0){
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            pubdate.setText(sdf.format(message.timeStamp * 1000));
        }else pubdate.setText(message.timeDesc);

        action.setText(message.content);
        
        if (message.videoCard != null){
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
            Reply childReply = message.replyInfo;
            ReplyCardHolder holder = new ReplyCardHolder(View.inflate(context,R.layout.cell_message_reply,extraCard));
            holder.showReplyCard(childReply);
            holder.itemView.findViewById(R.id.cardView).setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, VideoInfoActivity.class);
                intent.putExtra("bvid", childReply.ofBvid);
                intent.putExtra("aid", 0);
                context.startActivity(intent);
            });
        }
        if(message.dynamicInfo != null){
            Reply childReply = message.dynamicInfo;
            ReplyCardHolder holder = new ReplyCardHolder(View.inflate(context,R.layout.cell_message_reply,extraCard));
            holder.showReplyCard(childReply);
        }
    }
}
