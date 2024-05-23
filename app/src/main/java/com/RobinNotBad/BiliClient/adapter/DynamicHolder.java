package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.article.ArticleInfoActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicInfoActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.send.SendDynamicActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.EmoteUtil;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class DynamicHolder extends RecyclerView.ViewHolder{
    public TextView username,content,pubdate;
    public ImageView avatar;
    public ConstraintLayout extraCard;
    public View itemView;
    public ImageView item_dynamic_share_img;
    public TextView item_dynamic_share;
    public boolean isChild;
    BaseActivity mActivity;
    public ActivityResultLauncher<Intent> relayDynamicLauncher;

    public DynamicHolder(@NonNull View itemView, BaseActivity mActivity, boolean isChild) {
        super(itemView);
        this.itemView = itemView;
        this.isChild = isChild;
        this.mActivity = mActivity;
        if (isChild) {
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
            item_dynamic_share_img = itemView.findViewById(R.id.item_dynamic_share_img);
            item_dynamic_share = itemView.findViewById(R.id.item_dynamic_share);
            relayDynamicLauncher = mActivity.relayDynamicLauncher;
        }
    }


    @SuppressLint("SetTextI18n")
    public void showDynamic(Dynamic dynamic, Context context, boolean clickable){    //公用的显示函数 这样修改和调用都方便
        username.setText(dynamic.userInfo.name);
        if(pubdate!=null) pubdate.setText(dynamic.pubTime);
        if(dynamic.content != null && !dynamic.content.isEmpty()) {
            content.setVisibility(View.VISIBLE);
            content.setText(dynamic.content);
            if (dynamic.emotes != null) {
                CenterThreadPool.run(() -> {
                    try {
                        SpannableString spannableString = EmoteUtil.textReplaceEmote(dynamic.content, dynamic.emotes, 1.0f, context);
                        CenterThreadPool.runOnUiThread(() -> content.setText(spannableString));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        } else content.setVisibility(View.GONE);
        Glide.with(context).load(GlideUtil.url(dynamic.userInfo.avatar))
                .placeholder(R.mipmap.akari)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(avatar);

        avatar.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(context, UserInfoActivity.class);
            intent.putExtra("mid", dynamic.userInfo.mid);
            context.startActivity(intent);
        });

        if(dynamic.major_type != null) switch (dynamic.major_type){
            case "MAJOR_TYPE_ARCHIVE":
            case "MAJOR_TYPE_UGC_SEASON":
                VideoCard childVideoCard = (VideoCard) dynamic.major_object;
                VideoCardHolder video_holder = new VideoCardHolder(View.inflate(context,R.layout.cell_dynamic_video,extraCard));
                video_holder.showVideoCard(childVideoCard,context);
                video_holder.itemView.findViewById(R.id.cardView).setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, VideoInfoActivity.class);
                    intent.putExtra("bvid", "");
                    intent.putExtra("aid", childVideoCard.aid);
                    context.startActivity(intent);
                });
                break;

            case "MAJOR_TYPE_ARTICLE":
                ArticleCard articleCard = (ArticleCard) dynamic.major_object;
                ArticleCardHolder article_holder = new ArticleCardHolder(View.inflate(context,R.layout.cell_dynamic_article,extraCard));
                article_holder.showArticleCard(articleCard,context);
                article_holder.itemView.findViewById(R.id.cardView).setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, ArticleInfoActivity.class);
                    intent.putExtra("cvid", articleCard.id);
                    context.startActivity(intent);
                });
                break;

            case "MAJOR_TYPE_DRAW":
                ArrayList<String> pictureList = (ArrayList<String>) dynamic.major_object;
                View imageCard = View.inflate(context,R.layout.cell_dynamic_image,extraCard);
                ImageView imageView = imageCard.findViewById(R.id.imageView);
                Glide.with(context).load(GlideUtil.url(pictureList.get(0)))
                        .placeholder(R.mipmap.placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView);
                TextView textView = imageCard.findViewById(R.id.imageCount);
                textView.setText("共" + pictureList.size() + "张图片");
                MaterialCardView cardView = imageCard.findViewById(R.id.imageCard);
                cardView.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, ImageViewerActivity.class);
                    intent.putExtra("imageList", pictureList);
                    context.startActivity(intent);
                });
                break;
        }

        if(clickable) {
            content.setMaxLines(5);
            if(dynamic.dynamicId != 0) {
                (isChild ? itemView.findViewById(R.id.cardView) : itemView).setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, DynamicInfoActivity.class);
                    intent.putExtra("id", dynamic.dynamicId);
                    context.startActivity(intent);
                });
            }
        }
        else {
            content.setMaxLines(999);
        }

        View.OnClickListener onRelayClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (relayDynamicLauncher == null) {
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(mActivity, SendDynamicActivity.class);
                intent.putExtra("dynamicId", dynamic.dynamicId);
                relayDynamicLauncher.launch(intent);
            }
        };
        if (item_dynamic_share != null && item_dynamic_share_img != null) {
            item_dynamic_share.setOnClickListener(onRelayClick);
            item_dynamic_share_img.setOnClickListener(onRelayClick);
        }
    }
}
