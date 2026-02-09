package com.RobinNotBad.BiliClient.adapter.article;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.api.ArticleApi;
import com.RobinNotBad.BiliClient.model.Opus;
import com.RobinNotBad.BiliClient.model.OpusParagraph;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.StringUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

//文章内容Adapter by RobinNotBad

public class OpusContentAdapter extends RecyclerView.Adapter<OpusContentAdapter.ArticleLineHolder> {

    final Activity context;
    final Opus article;
    final OpusParagraph[] paragraphs;

    private int coinAdd = 0;

    public OpusContentAdapter(Activity context, Opus article) {
        this.context = context;
        this.article = article;
        this.paragraphs = article.paragraphs;
    }

    @NonNull
    @Override
    public ArticleLineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) { // -1=头，0=文本，1=图片
            case -1:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_head, parent, false);
                break;
            case -2:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_end, parent, false);
                break;
            case OpusParagraph.TYPE_PIC:
            case OpusParagraph.TYPE_DIVIDER:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_image, parent, false);
                break;
            case OpusParagraph.TYPE_ARTICLE:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_list, parent, false);
                break;
            case OpusParagraph.TYPE_VIDEO:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_dynamic_video, parent, false);
                break;
            case OpusParagraph.TYPE_DYNAMIC:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_dynamic_child, parent, false);
                break;
            case OpusParagraph.TYPE_TEXT:
            case OpusParagraph.TYPE_TEXT_BLOCKQUOTE:
            case OpusParagraph.TYPE_TEXT_OPUS:
            case OpusParagraph.TYPE_LIST:
            default:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_textview, parent, false);
                break;
        }
        return new ArticleLineHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ArticleLineHolder holder, int position) {
        if (position < 0)
            return;
        int viewType = getItemViewType(position);
        int realPosition = position - 1; // 头部占了position 0
        // 只对内容段落检查越界，不影响头部(-1)和尾部(-2)
        if (viewType >= 0 && paragraphs != null && realPosition >= 0 && realPosition >= paragraphs.length)
            return;

        switch (viewType) {
            case OpusParagraph.TYPE_PIC:
            case OpusParagraph.TYPE_DIVIDER:
                if (realPosition < 0 || realPosition >= paragraphs.length)
                    break;
                ImageFilterView imageView = holder.itemView.findViewById(R.id.imageView);
                TextView imageCount = holder.itemView.findViewById(R.id.imageCount);

                if (paragraphs[realPosition].content instanceof String[]) {
                    String[] urls = (String[]) paragraphs[realPosition].content;
                    int length = urls != null ? urls.length : 0;
                    if (length > 0 && urls[0] != null) {
                        String imageUrl = GlideUtil.url(urls[0]);
                        if (!imageUrl.equals(holder.lastImageUrl)) {
                            holder.lastImageUrl = imageUrl;
                            Glide.with(BiliTerminal.context).asDrawable().load(imageUrl)
                                    .placeholder(R.mipmap.placeholder)
                                    .transition(GlideUtil.getTransitionOptions())
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .into(imageView);
                        }

                        imageView.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(context, ImageViewerActivity.class);
                            intent.putExtra("imageList", new ArrayList<>(Arrays.asList(urls)));
                            context.startActivity(intent);
                        });

                        if (length > 1)
                            imageCount.setText(String.format(Locale.CHINA, "共%d张图片", length));
                    }
                }
                break;

            case -1:
                TextView title = holder.itemView.findViewById(R.id.text_title);
                ImageView topImage = holder.itemView.findViewById(R.id.topImage);
                TextView topCount = holder.itemView.findViewById(R.id.topCount);
                ImageView upIcon = holder.itemView.findViewById(R.id.upInfo_Icon); // 头
                TextView upName = holder.itemView.findViewById(R.id.upInfo_Name);
                MaterialCardView upCard = holder.itemView.findViewById(R.id.upInfo);

                if (!TextUtils.isEmpty(article.title)) {
                    title.setVisibility(View.VISIBLE);
                    title.setText(article.title);
                    StringUtil.setCopy(title);
                } else
                    title.setVisibility(View.GONE);

                if (!TextUtils.isEmpty(article.cover)) {
                    String coverUrl = GlideUtil.url(article.cover);
                    if (!coverUrl.equals(holder.lastTopImageUrl)) {
                        holder.lastTopImageUrl = coverUrl;
                        Glide.with(BiliTerminal.context).asDrawable().load(coverUrl)
                                .placeholder(R.mipmap.placeholder)
                                .transition(GlideUtil.getTransitionOptions())
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(4))))
                                .format(DecodeFormat.PREFER_RGB_565)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(topImage);
                    }
                    topCount.setVisibility(View.GONE);
                } else if (article.topImages != null && article.topImages.size() > 0) {
                    String firstImageUrl = GlideUtil.url(article.topImages.get(0));
                    if (!firstImageUrl.equals(holder.lastTopImageUrl)) {
                        holder.lastTopImageUrl = firstImageUrl;
                        Glide.with(BiliTerminal.context).asDrawable().load(firstImageUrl)
                                .placeholder(R.mipmap.placeholder)
                                .transition(GlideUtil.getTransitionOptions())
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(4))))
                                .format(DecodeFormat.PREFER_RGB_565)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(topImage);
                    }
                    topImage.setOnClickListener(view -> {
                        Intent intent = new Intent();
                        intent.setClass(context, ImageViewerActivity.class);
                        intent.putExtra("imageList", article.topImages);
                        context.startActivity(intent);
                    });
                    if (article.topImages.size() > 1) {
                        topCount.setText(String.format(Locale.CHINA, "共%d张图片", article.topImages.size()));
                        topCount.setVisibility(View.VISIBLE);
                    } else
                        topCount.setVisibility(View.GONE);
                } else
                    holder.itemView.findViewById(R.id.topImageLayout).setVisibility(View.GONE);

                upName.setText(article.upInfo.name);
                String avatarUrl = GlideUtil.url(article.upInfo.avatar);
                if (!avatarUrl.equals(holder.lastAvatarUrl)) {
                    holder.lastAvatarUrl = avatarUrl;
                    Glide.with(BiliTerminal.context).asDrawable().load(avatarUrl)
                            .placeholder(R.mipmap.akari)
                            .transition(GlideUtil.getTransitionOptions())
                            .apply(RequestOptions.circleCropTransform())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(upIcon);
                }
                upCard.setOnClickListener(view1 -> {
                    Intent intent = new Intent();
                    intent.setClass(context, UserInfoActivity.class);
                    intent.putExtra("mid", article.upInfo.mid);
                    context.startActivity(intent);
                });

                break;

            case -2:
                TextView viewCount = holder.itemView.findViewById(R.id.viewCount);
                TextView timeText = holder.itemView.findViewById(R.id.timeText);
                TextView cvidText = holder.itemView.findViewById(R.id.cvidText);
                cvidText.setText("cv" + article.id/* + " | " + article.wordCount + "字" */);
                StringUtil.setCopy(cvidText, "cv" + article.id);
                viewCount.setText(StringUtil.toWan(article.stats.view) + "阅读");
                timeText.setText(article.pubTime);

                ImageButton like = holder.itemView.findViewById(R.id.btn_like);
                ImageButton coin = holder.itemView.findViewById(R.id.btn_coin);
                TextView likeLabel = holder.itemView.findViewById(R.id.like_label);
                TextView coinLabel = holder.itemView.findViewById(R.id.coin_label);
                TextView favLabel = holder.itemView.findViewById(R.id.fav_label);
                ImageButton fav = holder.itemView.findViewById(R.id.btn_fav);

                likeLabel.setText(StringUtil.toWan(article.stats.like));
                coinLabel.setText(StringUtil.toWan(article.stats.coin));
                favLabel.setText(StringUtil.toWan(article.stats.favorite));

                like.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    try {
                        if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                            context.runOnUiThread(() -> MsgUtil.showMsg("还没有登录喵~"));
                            return;
                        }
                        int result = ArticleApi.like(article.id, !article.stats.liked);
                        if (result == 0) {
                            article.stats.liked = !article.stats.liked;
                            context.runOnUiThread(() -> {
                                MsgUtil.showMsg((article.stats.liked ? "点赞成功" : "取消成功"));

                                if (article.stats.liked)
                                    likeLabel.setText(StringUtil.toWan(++article.stats.like));
                                else
                                    likeLabel.setText(StringUtil.toWan(--article.stats.like));
                                like.setImageResource(
                                        article.stats.liked ? R.drawable.icon_like_1 : R.drawable.icon_like_0);
                            });
                        } else {
                            context.runOnUiThread(() -> MsgUtil.showMsg("操作失败：" + result));
                        }
                    } catch (Exception e) {
                        context.runOnUiThread(() -> MsgUtil.err(e));
                    }
                }));

                coin.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    if (article.stats.coined < article.stats.coin_limit) {
                        try {
                            if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                                context.runOnUiThread(() -> MsgUtil.showMsg("还没有登录喵~"));
                                return;
                            }
                            int result = ArticleApi.addCoin(article.id, article.upInfo.mid, 1);
                            if (result == 0) {
                                if (++coinAdd <= 2)
                                    article.stats.coined++;
                                context.runOnUiThread(() -> {
                                    MsgUtil.showMsg("投币成功！");
                                    coinLabel.setText(StringUtil.toWan(++article.stats.coin));
                                    coin.setImageResource(R.drawable.icon_coin_1);
                                });
                            } else {
                                String msg = "投币失败：" + result;
                                if (result == 34002) {
                                    msg = "不能给自己投币哦！";
                                }
                                String finalMsg = msg;
                                context.runOnUiThread(() -> MsgUtil.showMsg(finalMsg));
                            }
                        } catch (Exception e) {
                            context.runOnUiThread(() -> MsgUtil.err(e));
                        }
                    } else {
                        context.runOnUiThread(() -> MsgUtil.showMsg("投币数量到达上限"));
                    }
                }));

                fav.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    try {
                        if (article.stats.favoured) {
                            if (ArticleApi.delFavorite(article.id) == 0) {
                                context.runOnUiThread(() -> fav.setImageResource(R.drawable.icon_fav_0));
                                article.stats.favorite--;
                            }
                        } else {
                            if (ArticleApi.favorite(article.id) == 0) {
                                context.runOnUiThread(() -> fav.setImageResource(R.drawable.icon_fav_1));
                                article.stats.favorite++;
                            }
                        }
                        article.stats.favoured = !article.stats.favoured;
                        context.runOnUiThread(() -> {
                            favLabel.setText(StringUtil.toWan(article.stats.favorite));
                            MsgUtil.showMsg("操作成功~");
                        });
                    } catch (Exception e) {
                        context.runOnUiThread(() -> MsgUtil.err(e));
                    }
                }));

                if (article.type == Opus.TYPE_DYNAMIC) {
                    holder.itemView.findViewById(R.id.viewIcon).setVisibility(View.GONE);
                    viewCount.setVisibility(View.GONE);
                    holder.itemView.findViewById(R.id.cvidIcon).setVisibility(View.GONE);
                    cvidText.setVisibility(View.GONE);
                }
                break;

            case OpusParagraph.TYPE_VIDEO:

                break;

            case OpusParagraph.TYPE_ARTICLE:

                break;

            case OpusParagraph.TYPE_TEXT:
            case OpusParagraph.TYPE_TEXT_BLOCKQUOTE:
            case OpusParagraph.TYPE_TEXT_OPUS:
            case OpusParagraph.TYPE_LIST:
            default:
                if (realPosition >= 0 && realPosition < paragraphs.length && paragraphs[realPosition].content != null) {
                    TextView textView = holder.itemView.findViewById(R.id.textView);
                    if (paragraphs[realPosition].content instanceof CharSequence) {
                        textView.setText((CharSequence) paragraphs[realPosition].content);
                        StringUtil.setCopy(textView);
                        StringUtil.setLink(textView);
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (paragraphs == null)
            return 2;
        return paragraphs.length + 2;
    }

    @Override
    public void onViewRecycled(@NonNull ArticleLineHolder holder) {
        holder.lastTopImageUrl = null;
        holder.lastAvatarUrl = null;
        holder.lastImageUrl = null;
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemViewType(int position) {
        if (paragraphs == null)
            return -1;
        if (position == 0)
            return -1;
        else if (position == paragraphs.length + 1)
            return -2;
        else {
            int realPosition = position - 1;
            if (realPosition >= 0 && realPosition < paragraphs.length) {
                return paragraphs[realPosition].type;
            }
            return -1;
        }
    }

    public static class ArticleLineHolder extends RecyclerView.ViewHolder {
        String lastTopImageUrl;
        String lastAvatarUrl;
        String lastImageUrl;

        public ArticleLineHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
