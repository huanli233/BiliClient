package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicInfoActivity;
import com.RobinNotBad.BiliClient.activity.user.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.EmoteUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public class DynamicHolder extends RecyclerView.ViewHolder{
    public TextView username,content,pubdate;
    public ImageView avatar;
    public ConstraintLayout extraCard;
    public View itemView;
    public boolean isChild;

    public DynamicHolder(@NonNull View itemView, boolean isChild) {
        super(itemView);
        this.itemView = itemView;
        this.isChild = isChild;
        if(isChild) {
            username = itemView.findViewById(R.id.child_username);
            content = itemView.findViewById(R.id.child_content);
            avatar = itemView.findViewById(R.id.child_avatar);
            extraCard = itemView.findViewById(R.id.child_extraCard);
        }
        else {
            username = itemView.findViewById(R.id.username);
            pubdate = itemView.findViewById(R.id.pubdate);
            content = itemView.findViewById(R.id.content);
            avatar = itemView.findViewById(R.id.avatar);
            extraCard = itemView.findViewById(R.id.extraCard);
        }
    }


    @SuppressLint("SetTextI18n")
    public void showDynamic(Dynamic dynamic, Context context){    //公用的显示函数 这样修改和调用都方便
        username.setText(dynamic.userName);
        if(pubdate!=null) pubdate.setText(dynamic.pubDate);
        if(dynamic.content != null && !dynamic.content.isEmpty()) {
            content.setVisibility(View.VISIBLE);
            content.setText(dynamic.content);
            if (dynamic.emote != null) {
                CenterThreadPool.run(() -> {
                    try {
                        SpannableString spannableString = EmoteUtil.textReplaceEmote(dynamic.content, dynamic.emote, 1.0f, context);
                        CenterThreadPool.runOnMainThread(() -> content.setText(spannableString));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }else content.setVisibility(View.GONE);
        Glide.with(context).load(dynamic.userAvatar)
                .placeholder(R.mipmap.akari)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(avatar);

        avatar.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(context, UserInfoActivity.class);
            intent.putExtra("mid", dynamic.userId);
            context.startActivity(intent);
        });

        if(dynamic.pictureList != null){
            View imageCard = View.inflate(context,R.layout.cell_dynamic_image,extraCard);
            ImageView imageView = imageCard.findViewById(R.id.imageView);
            Glide.with(context).load(dynamic.pictureList.get(0)+"@25q.webp")
                    .override(Target.SIZE_ORIGINAL)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView);
            TextView textView = imageCard.findViewById(R.id.imageCount);
            textView.setText("共" + dynamic.pictureList.size() + "张图片");
            MaterialCardView cardView = imageCard.findViewById(R.id.imageCard);
            cardView.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, ImageViewerActivity.class);
                intent.putExtra("imageList", dynamic.pictureList);
                context.startActivity(intent);
            });
        }

        if(dynamic.childVideoCard != null){
            VideoCard childVideoCard = dynamic.childVideoCard;
            VideoCardHolder holder = new VideoCardHolder(View.inflate(context,R.layout.cell_dynamic_video,extraCard));
            holder.showVideoCard(childVideoCard,context);

            holder.itemView.findViewById(R.id.cardView).setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, VideoInfoActivity.class);
                intent.putExtra("bvid", "");
                intent.putExtra("aid", childVideoCard.aid);
                context.startActivity(intent);
            });
        }


        itemView.setOnClickListener(view -> {
            if(!isChild) {
                if(dynamic.type == 1 || dynamic.type == 2 || dynamic.type == 4) {
                    Intent intent = new Intent();
                    intent.setClass(context, DynamicInfoActivity.class);
                    intent.putExtra("id", dynamic.dynamicId);
                    intent.putExtra("rid", dynamic.rid);
                    intent.putExtra("type", dynamic.type);
                    context.startActivity(intent);
                }
            }
        });


    }
}
