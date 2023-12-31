package com.RobinNotBad.BiliClient.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class ArticleCardHolder extends RecyclerView.ViewHolder{
    TextView title,upName,playTimes;
    ImageView cover,playIcon,upIcon;

    public ArticleCardHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.listVideoTitle);
        upName = itemView.findViewById(R.id.listUpName);
        playTimes = itemView.findViewById(R.id.listPlayTimes);
        cover = itemView.findViewById(R.id.listCover);
        playIcon = itemView.findViewById(R.id.imageView3);
        upIcon = itemView.findViewById(R.id.avatarIcon);
    }

    public void showArticleCard(ArticleInfo articleCard, Context context){
        title.setText(LittleToolsUtil.htmlToString(articleCard.title));
        String upNameStr = articleCard.upName;
        if(upNameStr.equals("")){
            upName.setVisibility(View.GONE);
            upIcon.setVisibility(View.GONE);
        }
        else upName.setText(upNameStr);

        String playTimesStr = String.valueOf(articleCard.view);
        if(playTimesStr.equals("")){
            playIcon.setVisibility(View.GONE);
            playTimes.setVisibility(View.GONE);
        }
        else playTimes.setText(playTimesStr);

        Glide.with(context).load(articleCard.banner)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(LittleToolsUtil.dp2px(5,context))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(cover);
    }
}
