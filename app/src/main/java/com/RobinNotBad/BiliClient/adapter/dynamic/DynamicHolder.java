package com.RobinNotBad.BiliClient.adapter.dynamic;

import static com.RobinNotBad.BiliClient.util.ToolsUtil.toWan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.article.ArticleInfoActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicInfoActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.send.SendDynamicActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardHolder;
import com.RobinNotBad.BiliClient.adapter.article.ArticleCardHolder;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.EmoteUtil;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.PreInflateHelper;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DynamicHolder extends RecyclerView.ViewHolder{
    public static final int GO_TO_INFO_REQUEST = 71;
    public TextView username,content,pubdate;
    public ImageView avatar;
    public ConstraintLayout extraCard;
    public View itemView;
    public TextView item_dynamic_share, item_dynamic_delete;
    public TextView likeCount;
    public boolean isChild;
    private final PreInflateHelper preInflateHelper;
    BaseActivity mActivity;
    public ActivityResultLauncher<Intent> relayDynamicLauncher;

    public DynamicHolder(@NonNull View itemView, BaseActivity mActivity, PreInflateHelper preInflateHelper, boolean isChild) {
        super(itemView);
        this.itemView = itemView;
        this.isChild = isChild;
        this.preInflateHelper = preInflateHelper;
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
            item_dynamic_share = itemView.findViewById(R.id.item_dynamic_share);
            likeCount = itemView.findViewById(R.id.likes);
            item_dynamic_delete = itemView.findViewById(R.id.item_dynamic_delete);
            relayDynamicLauncher = mActivity.relayDynamicLauncher;
        }
    }

    public static void removeDynamicFromList(List<Dynamic> dynamicList, int finalPosition, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        dynamicList.remove(finalPosition);
        adapter.notifyItemRemoved(finalPosition + 1);
        adapter.notifyItemRangeChanged(finalPosition + 1, dynamicList.size() - finalPosition);
    }

    public static View.OnLongClickListener getDeleteListener(Activity dynamicActivity, List<Dynamic> dynamicList, int finalPosition, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        return new View.OnLongClickListener() {
            private int longClickPosition = -1;
            private long longClickTime = -1;
            @Override
            public boolean onLongClick(View view) {
                if (dynamicList.get(finalPosition).canDelete) {
                    long currentTime = System.currentTimeMillis();
                    if (longClickPosition == finalPosition && currentTime - longClickTime < 10000) {
                        CenterThreadPool.run(() -> {
                            try {
                                int result = DynamicApi.deleteDynamic(dynamicList.get(finalPosition).dynamicId);
                                if (result == 0) {
                                    dynamicList.remove(finalPosition);
                                    dynamicActivity.runOnUiThread(() -> {
                                        adapter.notifyItemRemoved(finalPosition + 1);
                                        adapter.notifyItemRangeChanged(finalPosition + 1, dynamicList.size() - finalPosition);
                                        longClickPosition = -1;
                                        MsgUtil.toast("删除成功~", dynamicActivity);
                                    });
                                } else {
                                    String msg = "操作失败：" + result;
                                    switch (result) {
                                        case 500404:
                                            msg = "已经删除过了哦~";
                                            break;
                                        case 500406:
                                            msg = "不是自己的动态！";
                                            break;
                                    }
                                    String finalMsg = msg;
                                    dynamicActivity.runOnUiThread(() -> MsgUtil.toast(finalMsg, dynamicActivity));
                                }
                            } catch (IOException e) {
                                dynamicActivity.runOnUiThread(() -> MsgUtil.err(e, dynamicActivity));
                            }
                        });
                    } else {
                        longClickPosition = finalPosition;
                        longClickTime = currentTime;
                        MsgUtil.toast("再次长按删除", dynamicActivity);
                    }
                }
                return true;
            }
        };
    }

    public static View.OnLongClickListener getDeleteListener(Activity dynamicActivity, Dynamic dynamic) {
        return new View.OnLongClickListener() {
            private long longClickTime = -1;
            @Override
            public boolean onLongClick(View view) {
                if (dynamic.canDelete) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - longClickTime < 10000) {
                        CenterThreadPool.run(() -> {
                            try {
                                int result = DynamicApi.deleteDynamic(dynamic.dynamicId);
                                if (result == 0) {
                                    dynamicActivity.runOnUiThread(() -> {
                                        dynamicActivity.setResult(Activity.RESULT_OK, dynamicActivity.getIntent().getExtras() != null ? new Intent().putExtras(dynamicActivity.getIntent().getExtras()) : new Intent());
                                        dynamicActivity.finish();
                                        MsgUtil.toast("删除成功~", dynamicActivity);
                                    });
                                } else {
                                    String msg = "操作失败：" + result;
                                    switch (result) {
                                        case 500404:
                                            msg = "已经删除过了哦~";
                                            break;
                                        case 500406:
                                            msg = "不是自己的动态！";
                                            break;
                                    }
                                    String finalMsg = msg;
                                    dynamicActivity.runOnUiThread(() -> MsgUtil.toast(finalMsg, dynamicActivity));
                                }
                            } catch (IOException e) {
                                dynamicActivity.runOnUiThread(() -> MsgUtil.err(e, dynamicActivity));
                            }
                        });
                    } else {
                        longClickTime = currentTime;
                        MsgUtil.toast("再次长按删除", dynamicActivity);
                    }
                }
                return true;
            }
        };
    }

    @SuppressLint("SetTextI18n")
    public void showDynamic(Dynamic dynamic, Context context, boolean clickable){    //公用的显示函数 这样修改和调用都方便
        ToolsUtil.setCopy(content, context);
        username.setText(dynamic.userInfo.name);
        if(pubdate!=null) pubdate.setText(dynamic.pubTime);
        if(dynamic.content != null && !dynamic.content.isEmpty()) {
            content.setVisibility(View.VISIBLE);
            content.setText(dynamic.content);
            if (dynamic.emotes != null) {
                CenterThreadPool.run(() -> {
                    try {
                        SpannableString spannableString = EmoteUtil.textReplaceEmote(dynamic.content, dynamic.emotes, 1.0f, context, content.getText());
                        CenterThreadPool.runOnUiThread(() -> {
                            content.setText(spannableString);
                            ToolsUtil.setLink(content);
                            ToolsUtil.setAtLink(dynamic.ats, content);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
            ToolsUtil.setLink(content);
            ToolsUtil.setAtLink(dynamic.ats, content);
        } else content.setVisibility(View.GONE);
        Glide.with(context).asDrawable().load(GlideUtil.url(dynamic.userInfo.avatar))
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

        boolean isPgc = false;
        if(dynamic.major_type != null) switch (dynamic.major_type){
            case "MAJOR_TYPE_PGC":
                isPgc = true;
            case "MAJOR_TYPE_ARCHIVE":
            case "MAJOR_TYPE_UGC_SEASON":
                VideoCard childVideoCard = (VideoCard) dynamic.major_object;
                VideoCardHolder video_holder = new VideoCardHolder(preInflateHelper.getView(extraCard, R.layout.cell_dynamic_video, true));
                video_holder.showVideoCard(childVideoCard,context);
                boolean finalIsPgc = isPgc;
                video_holder.itemView.findViewById(R.id.cardView).setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, VideoInfoActivity.class);
                    if (finalIsPgc) intent.putExtra("type", "media");
                    intent.putExtra("bvid", "");
                    intent.putExtra("aid", childVideoCard.aid);
                    context.startActivity(intent);
                });
                break;

            case "MAJOR_TYPE_ARTICLE":
                ArticleCard articleCard = (ArticleCard) dynamic.major_object;
                ArticleCardHolder article_holder = new ArticleCardHolder(preInflateHelper.getView(extraCard, R.layout.cell_dynamic_article, true));
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
                Glide.with(context).asDrawable().load(GlideUtil.url(pictureList.get(0)))
                        .placeholder(R.mipmap.placeholder)
                        .format(DecodeFormat.PREFER_RGB_565)
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

        if (clickable) {
            content.setMaxLines(5);
            if(dynamic.dynamicId != 0) {
                (isChild ? itemView.findViewById(R.id.cardView) : itemView).setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, DynamicInfoActivity.class);
                    intent.putExtra("id", dynamic.dynamicId);
                    intent.putExtra("position", getAdapterPosition());
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(intent, GO_TO_INFO_REQUEST);
                    } else {
                        context.startActivity(intent);
                    }
                });
                content.setOnClickListener(view -> (isChild ? itemView.findViewById(R.id.cardView) : itemView).callOnClick());
            }
        } else {
            content.setMaxLines(999);
        }
        content.setEllipsize(TextUtils.TruncateAt.END);

        View.OnClickListener onRelayClick = view -> {
            if (relayDynamicLauncher == null) {
                return;
            }
            Intent intent = new Intent();
            intent.setClass(mActivity, SendDynamicActivity.class);
            intent.putExtra("dynamicId", dynamic.dynamicId);
            intent.putExtra("forward", dynamic);
            relayDynamicLauncher.launch(intent);
        };
        if (item_dynamic_share != null) item_dynamic_share.setOnClickListener(onRelayClick);

        View.OnClickListener onDeleteClick = view -> MsgUtil.toast("长按删除", context);
        if (item_dynamic_delete != null) {
            item_dynamic_delete.setOnClickListener(onDeleteClick);
            item_dynamic_delete.setVisibility(View.GONE);
        }

        if (likeCount != null) {
            if (dynamic.stats != null) {
                if (dynamic.stats.liked){           //这里，还有下面，一定要加else！否则会导致错乱
                    likeCount.setTextColor(Color.rgb(0xfe,0x67,0x9a));
                    likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_liked),null,null,null);
                } else {
                    likeCount.setTextColor(Color.rgb(0xff,0xff,0xff));
                    likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_like),null,null,null);
                }
                likeCount.setText(toWan(dynamic.stats.like));
            } else {
                likeCount.setVisibility(View.GONE);
            }
            likeCount.setOnClickListener(view -> CenterThreadPool.run(() -> {
                if (!dynamic.stats.liked) {
                    try {
                        if (DynamicApi.likeDynamic(dynamic.dynamicId, true) == 0) {
                            dynamic.stats.liked = true;
                            ((Activity) context).runOnUiThread(() -> {
                                MsgUtil.toast("点赞成功",context);
                                likeCount.setText(toWan(++dynamic.stats.like));
                                likeCount.setTextColor(Color.rgb(0xfe,0x67,0x9a));
                                likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_liked),null,null,null);
                            });
                        } else
                            ((Activity) context).runOnUiThread(() -> MsgUtil.toast("点赞失败",context));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (DynamicApi.likeDynamic(dynamic.dynamicId, false) == 0) {
                            dynamic.stats.liked = false;
                            ((Activity) context).runOnUiThread(() -> {
                                MsgUtil.toast("取消成功",context);
                                likeCount.setText(toWan(--dynamic.stats.like));
                                likeCount.setTextColor(Color.rgb(0xff,0xff,0xff));
                                likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_like),null,null,null);
                            });
                        } else
                            ((Activity) context).runOnUiThread(() -> MsgUtil.toast("取消失败",context));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
    }
}
