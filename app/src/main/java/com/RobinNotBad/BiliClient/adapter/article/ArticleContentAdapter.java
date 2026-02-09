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
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.api.ArticleApi;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.ArticleLine;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;

//文章内容Adapter by RobinNotBad

public class ArticleContentAdapter extends RecyclerView.Adapter<ArticleContentAdapter.ArticleLineHolder> {

    final Activity context;
    final ArrayList<ArticleLine> article;
    final ArticleInfo articleInfo;

    private int coinAdd = 0;

    public ArticleContentAdapter(Activity context, ArticleInfo articleInfo, ArrayList<ArticleLine> article) {
        this.context = context;
        this.article = article;
        this.articleInfo = articleInfo;
    }

    @NonNull
    @Override
    public ArticleLineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) { // -1=头，0=文本，1=图片
            case 1:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_image, parent, false);
                break;
            case -1:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_head, parent, false);
                break;
            case -2:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_end, parent, false);
                break;
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
        if (viewType >= 0 && article != null && realPosition >= 0 && realPosition >= article.size())
            return;

        switch (viewType) {
            case 1:
                if (realPosition < 0 || realPosition >= article.size())
                    break;
                ImageFilterView imageView = (ImageFilterView) holder.itemView;
                ArticleLine line = article.get(realPosition);
                if (line == null || line.content == null)
                    break;

                String url = GlideUtil.url(line.content);
                if (!url.equals(holder.lastImageUrl)) {
                    holder.lastImageUrl = url;
                    Glide.with(BiliTerminal.context).asDrawable().load(url).placeholder(R.mipmap.placeholder)
                            .transition(GlideUtil.getTransitionOptions())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(imageView);
                }

                imageView.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, ImageViewerActivity.class);
                    ArrayList<String> imageList = new ArrayList<>();
                    imageList.add(url);
                    intent.putExtra("imageList", imageList);
                    context.startActivity(intent);
                });
                break;

            case -1:
                TextView title = holder.itemView.findViewById(R.id.text_title);
                ImageView cover = holder.itemView.findViewById(R.id.img_cover);
                ImageView upIcon = holder.itemView.findViewById(R.id.upInfo_Icon); // 头
                TextView upName = holder.itemView.findViewById(R.id.upInfo_Name);
                MaterialCardView upCard = holder.itemView.findViewById(R.id.upInfo);

                StringUtil.setCopy(title);

                upName.setText(articleInfo.upInfo.name);
                if (articleInfo.banner.isEmpty())
                    cover.setVisibility(View.GONE);
                else {
                    String bannerUrl = GlideUtil.url(articleInfo.banner);
                    if (!bannerUrl.equals(holder.lastTopImageUrl)) {
                        holder.lastTopImageUrl = bannerUrl;
                        Glide.with(BiliTerminal.context).asDrawable().load(bannerUrl)
                                .placeholder(R.mipmap.placeholder)
                                .transition(GlideUtil.getTransitionOptions())
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(4))))
                                .format(DecodeFormat.PREFER_RGB_565)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(cover);
                    }
                }

                String avatarUrl = GlideUtil.url(articleInfo.upInfo.avatar);
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
                    intent.putExtra("mid", articleInfo.upInfo.mid);
                    context.startActivity(intent);
                });
                ImageButton like = holder.itemView.findViewById(R.id.btn_like);
                ImageButton coin = holder.itemView.findViewById(R.id.btn_coin);
                TextView likeLabel = holder.itemView.findViewById(R.id.like_label);
                TextView coinLabel = holder.itemView.findViewById(R.id.coin_label);
                TextView favLabel = holder.itemView.findViewById(R.id.fav_label);
                ImageButton fav = holder.itemView.findViewById(R.id.btn_fav);

                likeLabel.setText(StringUtil.toWan(articleInfo.stats.like));
                coinLabel.setText(StringUtil.toWan(articleInfo.stats.coin));
                favLabel.setText(StringUtil.toWan(articleInfo.stats.favorite));

                like.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    try {
                        if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                            context.runOnUiThread(() -> MsgUtil.showMsg("还没有登录喵~"));
                            return;
                        }
                        int result = ArticleApi.like(articleInfo.id, !articleInfo.stats.liked);
                        if (result == 0) {
                            articleInfo.stats.liked = !articleInfo.stats.liked;
                            context.runOnUiThread(() -> {
                                MsgUtil.showMsg((articleInfo.stats.liked ? "点赞成功" : "取消成功"));

                                if (articleInfo.stats.liked)
                                    likeLabel.setText(StringUtil.toWan(++articleInfo.stats.like));
                                else
                                    likeLabel.setText(StringUtil.toWan(--articleInfo.stats.like));
                                like.setImageResource(
                                        articleInfo.stats.liked ? R.drawable.icon_like_1 : R.drawable.icon_like_0);
                            });
                        } else {
                            context.runOnUiThread(() -> MsgUtil.showMsg("操作失败：" + result));
                        }
                    } catch (Exception e) {
                        context.runOnUiThread(() -> MsgUtil.err(e));
                    }
                }));

                coin.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    if (articleInfo.stats.coined < articleInfo.stats.coin_limit) {
                        try {
                            if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                                context.runOnUiThread(() -> MsgUtil.showMsg("还没有登录喵~"));
                                return;
                            }
                            int result = ArticleApi.addCoin(articleInfo.id, articleInfo.upInfo.mid, 1);
                            if (result == 0) {
                                if (++coinAdd <= 2)
                                    articleInfo.stats.coined++;
                                context.runOnUiThread(() -> {
                                    MsgUtil.showMsg("投币成功！");
                                    coinLabel.setText(StringUtil.toWan(++articleInfo.stats.coin));
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
                        if (articleInfo.stats.favoured) {
                            if (ArticleApi.delFavorite(articleInfo.id) == 0) {
                                context.runOnUiThread(() -> fav.setImageResource(R.drawable.icon_fav_0));
                                articleInfo.stats.favorite--;
                            }
                        } else {
                            if (ArticleApi.favorite(articleInfo.id) == 0) {
                                context.runOnUiThread(() -> fav.setImageResource(R.drawable.icon_fav_1));
                                articleInfo.stats.favorite++;
                            }
                        }
                        articleInfo.stats.favoured = !articleInfo.stats.favoured;
                        context.runOnUiThread(() -> {
                            favLabel.setText(StringUtil.toWan(articleInfo.stats.favorite));
                            MsgUtil.showMsg("操作成功~");
                        });
                    } catch (Exception e) {
                        context.runOnUiThread(() -> MsgUtil.err(e));
                    }
                }));

                CenterThreadPool.run(() -> {
                    try {
                        ArticleInfo viewInfo = ArticleApi.getArticleViewInfo(articleInfo.id);
                        if (viewInfo != null) {
                            articleInfo.stats = viewInfo.stats;
                            articleInfo.stats.coin_limit = 1;
                            context.runOnUiThread(() -> {
                                if (articleInfo.stats.coined != 0)
                                    coin.setImageResource(R.drawable.icon_coin_1);
                                if (articleInfo.stats.liked)
                                    like.setImageResource(R.drawable.icon_like_1);
                                if (articleInfo.stats.favoured)
                                    fav.setImageResource(R.drawable.icon_fav_1);
                            });
                        }
                    } catch (Exception e) {
                        context.runOnUiThread(() -> MsgUtil.err(e));
                    }
                });

                title.setText(articleInfo.title);
                break;

            case -2:
                TextView views = holder.itemView.findViewById(R.id.viewCount);
                TextView timeText = holder.itemView.findViewById(R.id.timeText);
                TextView cvidText = holder.itemView.findViewById(R.id.cvidText);
                ImageView viewIcon = holder.itemView.findViewById(R.id.viewIcon);
                ImageView timeIcon = holder.itemView.findViewById(R.id.timeIcon);
                ImageView cvidIcon = holder.itemView.findViewById(R.id.cvidIcon);

                if (articleInfo.id > 0) {
                    cvidText.setText("cv" + articleInfo.id + " | " + articleInfo.wordCount + "字");
                    StringUtil.setCopy(cvidText, "cv" + articleInfo.id);
                    cvidText.setVisibility(View.VISIBLE);
                    cvidIcon.setVisibility(View.VISIBLE);
                } else {
                    cvidText.setVisibility(View.GONE);
                    cvidIcon.setVisibility(View.GONE);
                }

                if (articleInfo.stats != null && articleInfo.stats.view > 0) {
                    views.setText(StringUtil.toWan(articleInfo.stats.view) + "阅读");
                    views.setVisibility(View.VISIBLE);
                    viewIcon.setVisibility(View.VISIBLE);
                } else {
                    views.setVisibility(View.GONE);
                    viewIcon.setVisibility(View.GONE);
                }

                if (articleInfo.ctime > 0) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    timeText.setText(sdf.format(articleInfo.ctime * 1000));
                    timeText.setVisibility(View.VISIBLE);
                    timeIcon.setVisibility(View.VISIBLE);
                } else {
                    timeText.setVisibility(View.GONE);
                    timeIcon.setVisibility(View.GONE);
                }
                break;
            default:
                if (realPosition >= 0 && realPosition < article.size()) {
                    ArticleLine textLine = article.get(realPosition);
                    if (textLine != null && textLine.content != null) {
                        TextView textView = holder.itemView.findViewById(R.id.textView);
                        textView.setText(textLine.content);
                        switch (textLine.extra) {
                            case "strong":
                                textView.setAlpha(0.92f);
                                break;
                            case "br":
                                textView.setHeight(ToolsUtil.dp2px(6f));
                                break;
                            default:
                                textView.setAlpha(0.85f);
                                break;
                        }
                        StringUtil.setCopy(textView);
                        StringUtil.setLink(textView);
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return article != null ? article.size() + 2 : 2;
    }

    @Override
    public void onViewRecycled(@NonNull ArticleLineHolder holder) {
        holder.lastImageUrl = null;
        holder.lastTopImageUrl = null;
        holder.lastAvatarUrl = null;
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemViewType(int position) {
        if (article == null)
            return -1;
        if (position == 0)
            return -1;
        else if (position == article.size() + 1)
            return -2;
        else {
            int realPosition = position - 1;
            if (realPosition >= 0 && realPosition < article.size()) {
                return article.get(realPosition).type;
            }
            return 0;
        }
    }

    public static class ArticleLineHolder extends RecyclerView.ViewHolder {
        String lastImageUrl;
        String lastTopImageUrl;
        String lastAvatarUrl;

        public ArticleLineHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
