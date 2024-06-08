package com.RobinNotBad.BiliClient.adapter.article;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.api.ArticleApi;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.ArticleLine;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

//文章内容Adapter by RobinNotBad

public class ArticleContentAdapter extends RecyclerView.Adapter<ArticleContentAdapter.ArticleLineHolder> {

    Activity context;
    ArrayList<ArticleLine> article;
    ArticleInfo articleInfo;

    public ArticleContentAdapter(Activity context, ArticleInfo articleInfo, ArrayList<ArticleLine> article) {
        this.context = context;
        this.article = article;
        this.articleInfo = articleInfo;
    }

    @NonNull
    @Override
    public ArticleLineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){    //-1=头，0=文本，1=图片
            default:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_textview,parent,false);
                break;
            case 1:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_image,parent,false);
                break;
            case -1:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_head,parent,false);
                break;
            case -2:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_end,parent,false);
                break;
        }
        return new ArticleLineHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ArticleLineHolder holder, int position) {
        int realPosition = position - 1;
        switch (getItemViewType(position)){
            default:
                TextView textView = holder.itemView.findViewById(R.id.textView);  //文本
                textView.setText(article.get(realPosition).content);
                switch (article.get(realPosition).extra){
                    default:
                        textView.setAlpha(0.85f);
                        break;
                    case "strong":
                        textView.setAlpha(0.92f);
                        break;
                    case "br":
                        textView.setHeight(ToolsUtil.dp2px(6f,context));
                        break;
                }
                ToolsUtil.setCopy(context, textView);
                ToolsUtil.setLink(textView);
                break;

            case 1:
                ImageFilterView imageView = (ImageFilterView) holder.itemView;  //图片


                String url = article.get(realPosition).content;
                    Glide.with(context).asDrawable().load(GlideUtil.url(url)).placeholder(R.mipmap.placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(imageView);

                imageView.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, ImageViewerActivity.class);
                    ArrayList<String> imageList = new ArrayList<>();
                    imageList.add(article.get(realPosition).content);
                    intent.putExtra("imageList", imageList);
                    context.startActivity(intent);
                });
                break;

            case -1:
                TextView title = holder.itemView.findViewById(R.id.title);
                ImageView cover = holder.itemView.findViewById(R.id.cover);
                ImageView upIcon = holder.itemView.findViewById(R.id.upInfo_Icon);  //头
                TextView upName = holder.itemView.findViewById(R.id.upInfo_Name);
                MaterialCardView upCard = holder.itemView.findViewById(R.id.upInfo);

                ToolsUtil.setCopy(context, title);

                upName.setText(articleInfo.upInfo.name);
                if(articleInfo.banner.isEmpty()) cover.setVisibility(View.GONE);
                else{
                    Glide.with(context).asDrawable().load(GlideUtil.url(articleInfo.banner)).placeholder(R.mipmap.placeholder)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(4,context))))
                            .format(DecodeFormat.PREFER_RGB_565)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(cover);
                }
                Glide.with(context).asDrawable().load(GlideUtil.url(articleInfo.upInfo.avatar)).placeholder(R.mipmap.akari)
                        .apply(RequestOptions.circleCropTransform())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(upIcon);
                upCard.setOnClickListener(view1 ->{
                    Intent intent = new Intent();
                    intent.setClass(context, UserInfoActivity.class);
                    intent.putExtra("mid",articleInfo.upInfo.mid);
                    context.startActivity(intent);
                });
                ImageButton like = holder.itemView.findViewById(R.id.btn_like);
                ImageButton coin = holder.itemView.findViewById(R.id.btn_coin);
                TextView likeLabel = holder.itemView.findViewById(R.id.like_label);
                TextView coinLabel = holder.itemView.findViewById(R.id.coin_label);
                TextView favLabel = holder.itemView.findViewById(R.id.fav_label);
                ImageButton fav = holder.itemView.findViewById(R.id.btn_fav);

                likeLabel.setText(ToolsUtil.toWan(articleInfo.stats.like));
                coinLabel.setText(ToolsUtil.toWan(articleInfo.stats.coin));
                favLabel.setText(ToolsUtil.toWan(articleInfo.stats.favorite));

                like.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    try {
                        int result = ArticleApi.like(articleInfo.id, !articleInfo.stats.liked);
                        if (result == 0) {
                            articleInfo.stats.liked = !articleInfo.stats.liked;
                            context.runOnUiThread(() -> {
                                MsgUtil.toast((articleInfo.stats.liked ? "点赞成功" : "取消成功"), context);

                                if (articleInfo.stats.liked) likeLabel.setText(ToolsUtil.toWan(++articleInfo.stats.like));
                                else likeLabel.setText(ToolsUtil.toWan(--articleInfo.stats.like));
                                like.setBackground(ContextCompat.getDrawable(context, (articleInfo.stats.liked ? R.drawable.icon_like_1 : R.drawable.icon_like_0)));
                            });
                        } else {
                            context.runOnUiThread(() -> MsgUtil.toast("操作失败：" + result, context));
                        }
                    } catch (Exception e) {
                        context.runOnUiThread(() -> MsgUtil.err(e, context));
                    }
                }));

                coin.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    if (articleInfo.stats.coined < articleInfo.stats.allow_coin) {
                        try {
                            int result = ArticleApi.addCoin(articleInfo.id, articleInfo.upInfo.mid, 1);
                            if (result == 0) {
                                articleInfo.stats.coined++;
                                context.runOnUiThread(() -> {
                                    MsgUtil.toast("投币成功！", context);
                                    coinLabel.setText(ToolsUtil.toWan(++articleInfo.stats.coin));
                                    coin.setBackground(ContextCompat.getDrawable(context, R.drawable.icon_coin_1));
                                });
                            } else {
                                String msg = "投币失败：" + result;
                                switch (result) {
                                    case 34002:
                                        msg = "不能给自己投币哦！";
                                }
                                String finalMsg = msg;
                                context.runOnUiThread(() -> MsgUtil.toast(finalMsg, context));
                            }
                        } catch (Exception e) {
                            context.runOnUiThread(() -> MsgUtil.err(e, context));
                        }
                    } else {
                        context.runOnUiThread(() -> MsgUtil.toast("投币数量到达上限", context));
                    }
                }));

                fav.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    try {
                        if (articleInfo.stats.favoured) {
                            ArticleApi.delFavorite(articleInfo.id);
                            context.runOnUiThread(() -> fav.setBackground(ContextCompat.getDrawable(context, R.drawable.icon_favourite_0)));
                            articleInfo.stats.favorite--;
                        } else {
                            ArticleApi.favorite(articleInfo.id);
                            context.runOnUiThread(() -> fav.setBackground(ContextCompat.getDrawable(context, R.drawable.icon_favourite_1)));
                            articleInfo.stats.favorite++;
                        }
                        articleInfo.stats.favoured = !articleInfo.stats.favoured;
                        context.runOnUiThread(() -> {
                            favLabel.setText(ToolsUtil.toWan(articleInfo.stats.favorite));
                            MsgUtil.toast("操作成功~", context);
                        });
                    } catch (IOException e) {
                        context.runOnUiThread(() -> MsgUtil.err(e, context));
                    }
                }));

                CenterThreadPool.run(() -> {
                    try {
                        ArticleInfo viewInfo = ArticleApi.getArticleViewInfo(articleInfo.id);
                        if (viewInfo != null) {
                            articleInfo.stats = viewInfo.stats;
                            articleInfo.stats.allow_coin = 1;
                            context.runOnUiThread(()->{
                                if(articleInfo.stats.coined!=0) coin.setBackground(ContextCompat.getDrawable(context, R.drawable.icon_coin_1));
                                if(articleInfo.stats.liked) like.setBackground(ContextCompat.getDrawable(context, R.drawable.icon_like_1));
                                if(articleInfo.stats.favoured) fav.setBackground(ContextCompat.getDrawable(context, R.drawable.icon_favourite_1));
                            });
                        }
                    } catch (Exception e) {
                        context.runOnUiThread(() -> MsgUtil.err(e, context));
                    }
                });

                title.setText(articleInfo.title);
                break;

            case -2:
                TextView views = holder.itemView.findViewById(R.id.viewsCount);
                TextView timeText = holder.itemView.findViewById(R.id.timeText);
                TextView cvidText = holder.itemView.findViewById(R.id.cvidText);
                cvidText.setText("cv" + articleInfo.id + " | " + articleInfo.wordCount + "字");
                ToolsUtil.setCopy(cvidText, context, "cv" + articleInfo.id);
                views.setText(ToolsUtil.toWan(articleInfo.stats.view) + "阅读");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                timeText.setText(sdf.format(articleInfo.ctime * 1000));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return article.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0) return -1;
        else if(position == article.size() + 1) return -2;
        else return article.get(position-1).type;
    }

    public static class ArticleLineHolder extends RecyclerView.ViewHolder {
        public ArticleLineHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
