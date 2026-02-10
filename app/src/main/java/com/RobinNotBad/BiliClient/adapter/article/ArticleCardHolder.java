package com.RobinNotBad.BiliClient.adapter.article;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.StringUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class ArticleCardHolder extends RecyclerView.ViewHolder {
    TextView title, upName, readTimes;
    ImageView cover, readIcon, upIcon;

    public ArticleCardHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.listArticleTitle);
        upName = itemView.findViewById(R.id.text_upname);
        readTimes = itemView.findViewById(R.id.listReadTimes);
        cover = itemView.findViewById(R.id.img_cover);
        readIcon = itemView.findViewById(R.id.imageView3);
        upIcon = itemView.findViewById(R.id.avatarIcon);
    }

    public void showArticleCard(ArticleCard articleCard, Context context) {
        title.setText(StringUtil.htmlToString(articleCard.title));
        String upNameStr = articleCard.upName;
        if (upNameStr.isEmpty()) {
            upName.setVisibility(View.GONE);
            upIcon.setVisibility(View.GONE);
        } else {
            upName.setVisibility(View.VISIBLE);
            upIcon.setVisibility(View.VISIBLE);
            upName.setText(upNameStr);
        }

        if (articleCard.view.isEmpty()) {
            readIcon.setVisibility(View.GONE);
            readTimes.setVisibility(View.GONE);
        } else {
            readIcon.setVisibility(View.VISIBLE);
            readTimes.setVisibility(View.VISIBLE);
            readTimes.setText(articleCard.view);
        }

        Glide.with(BiliTerminal.context).asDrawable().load(!TextUtils.isEmpty(articleCard.cover) ? GlideUtil.url(articleCard.cover) : R.mipmap.article_placeholder)
                .placeholder(R.mipmap.placeholder)
                .transition(GlideUtil.getTransitionOptions())
                .format(DecodeFormat.PREFER_RGB_565)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(cover);
    }

    public void showArticleCard(VideoCard videoCard, Context context) {
        // 恢复 [专栏] 前缀，保持与历史记录原本样式一致
        android.text.SpannableString sstr_article = new android.text.SpannableString("[专栏]" + StringUtil.htmlToString(videoCard.title));
        sstr_article.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.rgb(100, 181, 246)), 0, 4,
                android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        title.setText(sstr_article);

        String upNameStr = videoCard.upName;
        if (upNameStr == null || upNameStr.isEmpty()) {
            upName.setVisibility(View.GONE);
            upIcon.setVisibility(View.GONE);
        } else {
            upName.setVisibility(View.VISIBLE);
            upIcon.setVisibility(View.VISIBLE);
            upName.setText(upNameStr);
        }

        String viewStr = videoCard.view;
        if (viewStr == null || viewStr.isEmpty()) {
            readIcon.setVisibility(View.GONE);
            readTimes.setVisibility(View.GONE);
        } else {
            readIcon.setVisibility(View.VISIBLE);
            readTimes.setVisibility(View.VISIBLE);
            readTimes.setText(viewStr);
        }

        Glide.with(BiliTerminal.context).asDrawable().load(!TextUtils.isEmpty(videoCard.cover) ? GlideUtil.url(videoCard.cover) : R.mipmap.article_placeholder)
                .placeholder(R.mipmap.placeholder)
                .transition(GlideUtil.getTransitionOptions())
                .format(DecodeFormat.PREFER_RGB_565)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(cover);
    }
}
