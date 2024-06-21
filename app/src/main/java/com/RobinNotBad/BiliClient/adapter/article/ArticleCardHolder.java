package com.RobinNotBad.BiliClient.adapter.article;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class ArticleCardHolder extends RecyclerView.ViewHolder{
    TextView title,upName,readTimes;
    ImageView cover,readIcon,upIcon;

    public ArticleCardHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.listArticleTitle);
        upName = itemView.findViewById(R.id.listUpName);
        readTimes = itemView.findViewById(R.id.listReadTimes);
        cover = itemView.findViewById(R.id.listCover);
        readIcon = itemView.findViewById(R.id.imageView3);
        upIcon = itemView.findViewById(R.id.avatarIcon);
    }

    public void showArticleCard(ArticleCard articleCard, Context context){
        title.setText(ToolsUtil.htmlToString(articleCard.title));
        String upNameStr = articleCard.upName;
        if(upNameStr.isEmpty()){
            upName.setVisibility(View.GONE);
            upIcon.setVisibility(View.GONE);
        }
        else upName.setText(upNameStr);

        if(articleCard.view.isEmpty()){
            readIcon.setVisibility(View.GONE);
            readTimes.setVisibility(View.GONE);
        }
        else readTimes.setText(articleCard.view);

        Glide.with(context).asDrawable().load(!TextUtils.isEmpty(articleCard.cover) ? GlideUtil.url(articleCard.cover) : R.mipmap.article_placeholder)
                .placeholder(R.mipmap.placeholder)
                .format(DecodeFormat.PREFER_RGB_565)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5,context))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(cover);
    }
}
