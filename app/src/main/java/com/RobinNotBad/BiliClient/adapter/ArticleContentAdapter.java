package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.ArticleLine;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

//文章内容Adapter by RobinNotBad

public class ArticleContentAdapter extends RecyclerView.Adapter<ArticleContentAdapter.ArticleLineHolder> {

    Context context;
    ArrayList<ArticleLine> article;
    ArticleInfo articleInfo;

    public ArticleContentAdapter(Context context,ArticleInfo articleInfo, ArrayList<ArticleLine> article) {
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
                    Glide.with(context).load(GlideUtil.url(url)).placeholder(R.mipmap.placeholder)
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
                    Glide.with(context).load(GlideUtil.url(articleInfo.banner)).placeholder(R.mipmap.placeholder)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(4,context))))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(cover);
                }
                Glide.with(context).load(GlideUtil.url(articleInfo.upInfo.avatar)).placeholder(R.mipmap.akari)
                        .apply(RequestOptions.circleCropTransform())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(upIcon);
                upCard.setOnClickListener(view1 ->{
                    Intent intent = new Intent();
                    intent.setClass(context, UserInfoActivity.class);
                    intent.putExtra("mid",articleInfo.upInfo.mid);
                    context.startActivity(intent);
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
