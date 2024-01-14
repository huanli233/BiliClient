package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.user.UserInfoActivity;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.ArticleLine;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
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
    LruCache<Integer, Bitmap> pictureCache;
    boolean keywords_expand = false;

    public ArticleContentAdapter(Context context,ArticleInfo articleInfo, ArrayList<ArticleLine> article) {
        this.context = context;
        this.article = article;
        this.articleInfo = articleInfo;
        if(SharedPreferencesUtil.getBoolean("dev_article_pic_load",false))
            pictureCache = new LruCache<>(12);
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
                        textView.setHeight(LittleToolsUtil.dp2px(6f,context));
                        break;
                }
                break;
            case 1:
                ImageView imageView = holder.itemView.findViewById(R.id.imageView);  //图片
                //在图片没有完全加载成功之前， 先用占位图垫着，后面等加载成功了再替换
                imageView.setImageResource(R.drawable.placeholder);

                if(SharedPreferencesUtil.getBoolean("dev_article_pic_load",true)) {
                    Bitmap cachedImage = pictureCache.get(realPosition);
                    if(cachedImage != null)
                        imageView.setImageBitmap(cachedImage);
                    else CenterThreadPool.run(()->{
                        try {
                            Bitmap bitmap = Glide.with(context).asBitmap().load(article.get(realPosition).content).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).submit().get();
                            pictureCache.put(realPosition,bitmap);
                            ((Activity)context).runOnUiThread(()->imageView.setImageBitmap(bitmap));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }else{
                    Glide.with(context).load(article.get(realPosition).content)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(imageView);
                }

                holder.itemView.findViewById(R.id.imageCard).setOnClickListener(view -> {
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
                TextView keywords = holder.itemView.findViewById(R.id.keywords);
                TextView upName = holder.itemView.findViewById(R.id.upInfo_Name);
                TextView views = holder.itemView.findViewById(R.id.viewsCount);
                TextView timeText = holder.itemView.findViewById(R.id.timeText);
                TextView cvidText = holder.itemView.findViewById(R.id.cvidText);
                MaterialCardView upCard = holder.itemView.findViewById(R.id.upInfo);

                keywords.setMaxLines((keywords_expand ? 10 : 1));
                keywords.setOnClickListener(view1 -> {
                    if(keywords_expand) keywords.setMaxLines(1);
                    else keywords.setMaxLines(512);
                    keywords_expand = !keywords_expand;
                });

                cvidText.setText("cv" + articleInfo.id + " | " + articleInfo.wordCount + "字");
                upName.setText(articleInfo.upName);
                keywords.setText("关键词：" + articleInfo.keywords);
                views.setText(articleInfo.view);
                if(articleInfo.banner.isEmpty()) cover.setVisibility(View.GONE);
                else{
                    Glide.with(context).load(articleInfo.banner).placeholder(R.drawable.placeholder)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(LittleToolsUtil.dp2px(4,context))))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(cover);
                }
                Glide.with(context).load(articleInfo.upAvatar).placeholder(R.drawable.akari)
                        .apply(RequestOptions.circleCropTransform())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(upIcon);
                upCard.setOnClickListener(view1 ->{
                    Intent intent = new Intent();
                    intent.setClass(context, UserInfoActivity.class);
                    intent.putExtra("mid",articleInfo.upMid);
                    context.startActivity(intent);
                });
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                timeText.setText(sdf.format(articleInfo.ctime * 1000));

                title.setText(articleInfo.title);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return article.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position==0 ? -1 : article.get(position-1).type);
    }

    public static class ArticleLineHolder extends RecyclerView.ViewHolder {
        public ArticleLineHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
