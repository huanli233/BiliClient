package com.RobinNotBad.BiliClient.adapter;

import static com.RobinNotBad.BiliClient.util.ToolsUtil.toWan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.reply.ReplyInfoActivity;
import com.RobinNotBad.BiliClient.activity.reply.WriteReplyActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.EmoteUtil;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.RobinNotBad.BiliClient.view.CustomListView;
import com.RobinNotBad.BiliClient.view.RadiusBackgroundSpan;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

//评论Adapter
//2023-07-22

public class ReplyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public boolean isDetail = false;
    final Context context;
    final ArrayList<Reply> replyList;
    final long oid;
    final long up_mid;
    final long root;
    final int type;
    final int sort;
    public final int replyType;
    OnItemClickListener listener;
    public Object source;

    public ReplyAdapter(Context context, ArrayList<Reply> replyList, long oid, long root, int type, int sort, Object source) {
        this.context = context;
        this.replyList = replyList;
        this.oid = oid;
        this.root = root;
        this.type = type;
        this.sort = sort;
        this.replyType = type;
        this.source = source;
        this.up_mid = -1;
    }

    public ReplyAdapter(Context context, ArrayList<Reply> replyList, long oid, long root, int type, int sort, Object source, long up_mid) {
        this.context = context;
        this.replyList = replyList;
        this.oid = oid;
        this.root = root;
        this.type = type;
        this.sort = sort;
        this.replyType = type;
        this.source = source;
        this.up_mid = up_mid;
    }

    public ReplyAdapter(Context context, ArrayList<Reply> replyList, long oid, long root, int type, int sort) {
        this(context, replyList, oid, root, type, sort, null);
    }

    public void setOnSortSwitchListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_reply_action, parent, false);
            return new WriteReply(view);
        } else {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_reply_list, parent, false);
            return new ReplyHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof WriteReply) {
            WriteReply writeReply = (WriteReply) holder;
            writeReply.write_reply.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, WriteReplyActivity.class);
                intent.putExtra("oid", oid);
                intent.putExtra("rpid", root);
                intent.putExtra("parent", root);
                intent.putExtra("parentSender", "");
                intent.putExtra("replyType", replyType);
                context.startActivity(intent);
            });
            String[] sorts = {"时间排序", "点赞排序", "回复排序"};
            if (isDetail) {
                writeReply.sort.setVisibility(View.GONE);
            } else {
                writeReply.sort.setText(sorts[sort]);
                writeReply.sort.setOnClickListener(view -> {
                    if (this.listener != null) listener.onItemClick(0);
                });
            }
        }
        if (holder instanceof ReplyHolder) {
            int realPosition;
            if (isDetail) {
                realPosition = position != 0 ? position - 1 : 0;
            } else {
                realPosition = position - 1;
            }
            ReplyHolder replyHolder = (ReplyHolder) holder;

            Glide.with(context).asDrawable().load(GlideUtil.url(replyList.get(realPosition).sender.avatar))
                    .transition(GlideUtil.getTransitionOptions())
                    .placeholder(R.mipmap.akari)
                    .apply(RequestOptions.circleCropTransform())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(replyHolder.replyAvatar);

            UserInfo sender = replyList.get(realPosition).sender;
            if (sender.mid == up_mid) {
                SpannableString name_str = new SpannableString(" UP " + replyList.get(realPosition).sender.name);
                name_str.setSpan(new RadiusBackgroundSpan(2, (int) context.getResources().getDimension(R.dimen.card_round), Color.WHITE, Color.rgb(207, 75, 95)), 0, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                name_str.setSpan(new RelativeSizeSpan(0.8f), 0,  4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                replyHolder.userName.setText(name_str);
            } else replyHolder.userName.setText(sender.name);


            String text = replyList.get(realPosition).message;
            replyHolder.message.setText(text);  //防止加载速度慢时露出鸡脚
            ToolsUtil.setCopy(replyHolder.message, context);
            if (replyList.get(realPosition).emotes != null) {
                CenterThreadPool.run(() -> {
                    try {
                        SpannableString spannableString = EmoteUtil.textReplaceEmote(text, replyList.get(realPosition).emotes, 1.0f, context, replyHolder.message.getText());
                        ((Activity) context).runOnUiThread(() -> {
                            replyHolder.message.setText(spannableString);
                            setTopSpan(realPosition, replyHolder);
                            ToolsUtil.setLink(replyHolder.message);
                            ToolsUtil.setAtLink(replyList.get(realPosition).atNameToMid, replyHolder.message);
                        });
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }

            setTopSpan(realPosition, replyHolder);
            ToolsUtil.setLink(replyHolder.message);
            ToolsUtil.setAtLink(replyList.get(realPosition).atNameToMid, replyHolder.message);

            replyHolder.likeCount.setText(String.valueOf(replyList.get(realPosition).likeCount));

            if (replyList.get(realPosition).liked) {
                replyHolder.likeCount.setTextColor(Color.rgb(0xfe, 0x67, 0x9a));
                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.icon_liked), null, null, null);
            } else {
                replyHolder.likeCount.setTextColor(Color.rgb(0xff, 0xff, 0xff));
                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.icon_like), null, null, null);
            }

            if (replyList.get(realPosition).childCount != 0 && !(realPosition == 0 && isDetail)) {
                replyHolder.childReplyCard.setVisibility(View.VISIBLE);

                if (replyList.get(realPosition).upReplied)
                    replyHolder.childCount.setText("UP主在内 共" + replyList.get(realPosition).childCount + "条回复");
                else
                    replyHolder.childCount.setText("共" + replyList.get(realPosition).childCount + "条回复");

                if (replyList.get(realPosition).childMsgList != null) {
                    final String up_tip = "  UP  ";
                    List<CharSequence> childMsgViewList = new ArrayList<>();
                    for (Reply reply : replyList.get(realPosition).childMsgList) {
                        String senderName = reply.sender.name;
                        SpannableString content = new SpannableString(senderName + (reply.sender.mid == up_mid ? up_tip : "") + "：" + reply.message);
                        if (reply.sender.mid == up_mid) {
                            Paint paint = new Paint();
                            paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics()));
                            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
                            float lineHeight = fontMetrics.descent - fontMetrics.ascent;
                            content.setSpan(new RadiusBackgroundSpan(2, (int) context.getResources().getDimension(R.dimen.card_round), Color.WHITE, Color.rgb(207, 75, 95), (int) (lineHeight)), senderName.length(), senderName.length() + up_tip.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            content.setSpan(new RelativeSizeSpan(0.8f), senderName.length(), senderName.length() + up_tip.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                        if (reply.emotes != null) {
                            CenterThreadPool.run(() -> {
                                try {
                                    EmoteUtil.textReplaceEmote(content.toString(), reply.emotes, 1.0f, context, content);
                                } catch (ExecutionException ignored) {} catch (InterruptedException ignored) {}
                            });
                        }
                        childMsgViewList.add(content);
                    }
                    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(context, R.layout.cell_reply_child, childMsgViewList);
                    replyHolder.childReplies.setAdapter(adapter);
                }
            } else replyHolder.childReplyCard.setVisibility(View.GONE);

            if (replyList.get(realPosition).upLiked)
                replyHolder.upLiked.setVisibility(View.VISIBLE);
            else replyHolder.upLiked.setVisibility(View.GONE);
            replyHolder.pubDate.setText(replyList.get(realPosition).pubTime);

            if (replyList.get(realPosition).pictureList != null && !replyList.get(realPosition).pictureList.isEmpty()) {  //图片显示相关
                replyHolder.imageCard.setVisibility(View.VISIBLE);
                replyHolder.imageCount.setVisibility(View.VISIBLE);
                Glide.with(context).asDrawable().load(GlideUtil.url(replyList.get(realPosition).pictureList.get(0)))
                        .transition(GlideUtil.getTransitionOptions())
                        .placeholder(R.mipmap.placeholder)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(replyHolder.imageCard);

                replyHolder.imageCount.setText("共" + replyList.get(realPosition).pictureList.size() + "张图片");
                replyHolder.imageCard.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, ImageViewerActivity.class);
                    intent.putExtra("imageList", replyList.get(realPosition).pictureList);
                    context.startActivity(intent);
                });
            } else {
                replyHolder.imageCount.setVisibility(View.GONE);
                replyHolder.imageCard.setVisibility(View.GONE);
            }

            replyHolder.childReplyCard.setOnClickListener(view -> startReplyInfoActivity(replyList.get(realPosition)));
            replyHolder.childReplies.setOnItemClickListener((adapterView, view, i, l) -> startReplyInfoActivity(replyList.get(realPosition)));
            if (!isDetail) {
                replyHolder.itemView.setOnClickListener((view) -> startReplyInfoActivity(replyList.get(realPosition)));
                replyHolder.message.setOnClickListener((view) -> startReplyInfoActivity(replyList.get(realPosition)));
            }

            replyHolder.replyAvatar.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, UserInfoActivity.class);
                intent.putExtra("mid", replyList.get(realPosition).sender.mid);
                context.startActivity(intent);
            });

            replyHolder.likeCount.setOnClickListener(view -> CenterThreadPool.run(() -> {
                if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                    ((Activity) context).runOnUiThread(() -> MsgUtil.showMsg("还没有登录喵~", context));
                    return;
                }
                if (!replyList.get(realPosition).liked) {
                    try {
                        if (ReplyApi.likeReply(oid, replyList.get(realPosition).rpid, true) == 0) {
                            replyList.get(realPosition).liked = true;
                            ((Activity) context).runOnUiThread(() -> {
                                MsgUtil.showMsg("点赞成功", context);
                                replyHolder.likeCount.setText(toWan(++replyList.get(realPosition).likeCount));
                                replyHolder.likeCount.setTextColor(Color.rgb(0xfe, 0x67, 0x9a));
                                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.icon_liked), null, null, null);
                            });
                        } else
                            ((Activity) context).runOnUiThread(() -> MsgUtil.showMsg("点赞失败", context));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (ReplyApi.likeReply(oid, replyList.get(realPosition).rpid, false) == 0) {
                            replyList.get(realPosition).liked = false;
                            ((Activity) context).runOnUiThread(() -> {
                                MsgUtil.showMsg("取消成功", context);
                                replyHolder.likeCount.setText(toWan(--replyList.get(realPosition).likeCount));
                                replyHolder.likeCount.setTextColor(Color.rgb(0xff, 0xff, 0xff));
                                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.icon_like), null, null, null);
                            });
                        } else
                            ((Activity) context).runOnUiThread(() -> MsgUtil.showMsg("取消失败", context));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }));

            View.OnClickListener onDeleteClick = view -> MsgUtil.showMsg("长按删除", context);
            replyHolder.item_reply_delete.setOnClickListener(onDeleteClick);
            View.OnLongClickListener onDeleteLongClick = new View.OnLongClickListener() {
                private int longClickPosition = -1;
                private long longClickTime = -1;

                @Override
                public boolean onLongClick(View view) {
                    long currentTime = System.currentTimeMillis();
                    if (longClickPosition == realPosition && currentTime - longClickTime < 6000) {
                        CenterThreadPool.run(() -> {
                            try {
                                int result = ReplyApi.deleteReply(oid, replyList.get(realPosition).rpid, replyType);
                                if (result == 0) {
                                    replyList.remove(realPosition);
                                    ((Activity) context).runOnUiThread(() -> {
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, replyList.size() - position);
                                        longClickPosition = -1;
                                        MsgUtil.showMsg("删除成功~", context);
                                        if (realPosition == 0 && isDetail) {
                                            ((Activity) context).finish();
                                        }
                                    });
                                } else {
                                    String msg = "操作失败：" + result;
                                    switch (result) {
                                        case -404:
                                            msg = "没有这条评论！";
                                            break;
                                        case -403:
                                            msg = "权限不足！";
                                            break;
                                    }
                                    String finalMsg = msg;
                                    ((Activity) context).runOnUiThread(() -> MsgUtil.showMsg(finalMsg, context));
                                }
                            } catch (Exception e) {
                                ((Activity) context).runOnUiThread(() -> MsgUtil.err(e, context));
                            }
                        });
                    } else {
                        longClickPosition = realPosition;
                        longClickTime = currentTime;
                        MsgUtil.showMsg("再次长按删除", context);
                    }
                    return true;
                }
            };
            replyHolder.item_reply_delete.setOnLongClickListener(onDeleteLongClick);
            if (!(replyList.get(realPosition).sender.mid == SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) || replyList.get(realPosition).sender.mid == 0 || replyList.get(realPosition).forceDelete)) {
                replyHolder.item_reply_delete.setVisibility(View.GONE);
                CenterThreadPool.run(() -> {
                    try {
                        boolean isManager = false;
                        if (source != null) {
                            if (source instanceof VideoInfo) {
                                List<UserInfo> staffs = ((VideoInfo) source).staff;
                                for (UserInfo userInfo : staffs) {
                                    if (userInfo.mid == SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0)) {
                                        isManager = true;
                                        break;
                                    }
                                }
                            } else if (source instanceof Dynamic) {
                                isManager = ((Dynamic) source).userInfo.mid == SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0);
                            } else if (source instanceof ArticleInfo) {
                                isManager = ((ArticleInfo) source).upInfo.mid == SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0);
                            }
                        }
                        if (isManager) {
                            ((Activity) context).runOnUiThread(() -> replyHolder.item_reply_delete.setVisibility(View.VISIBLE));
                        }
                    } catch (Exception e) {
                        if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) != 0)
                            ((Activity) context).runOnUiThread(() -> MsgUtil.err(e, context));
                    }
                });
            } else {
                replyHolder.item_reply_delete.setVisibility(View.VISIBLE);
            }

            replyHolder.replyBtn.setOnClickListener(view -> {
                boolean noParent = isDetail && realPosition == 0;
                Intent intent = new Intent();
                intent.setClass(context, WriteReplyActivity.class);
                intent.putExtra("oid", oid);
                intent.putExtra("rpid", noParent ? root : replyList.get(realPosition).rpid);
                intent.putExtra("parent", noParent ? root : replyList.get(realPosition).rpid);
                intent.putExtra("replyType", replyType);
                intent.putExtra("pos", realPosition);
                if (root != 0 && !noParent)
                    intent.putExtra("parentSender", replyList.get(realPosition).sender.name);
                else intent.putExtra("parentSender", "");
                context.startActivity(intent);
            });
        }
    }

    public void setTopSpan(int realPosition, ReplyHolder replyHolder) {
        if (replyList.get(realPosition).isTop && replyList.get(realPosition).message.startsWith(ReplyApi.TOP_TIP)) {
            SpannableString spannableString = new SpannableString(replyHolder.message.getText());
            spannableString.setSpan(new ForegroundColorSpan(Color.rgb(207, 75, 95)), 0, ReplyApi.TOP_TIP.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            replyHolder.message.setText(spannableString);
        }
    }

    public void startReplyInfoActivity(Reply reply) {
        long rpid = reply.rpid;
        long oid = reply.oid;
        int type = replyType;
        Intent intent = new Intent();
        intent.setClass(context, ReplyInfoActivity.class);
        intent.putExtra("rpid", rpid);
        intent.putExtra("oid", oid);
        intent.putExtra("type", type);
        intent.putExtra("origReply", reply);
        intent.putExtra("up_mid", up_mid);
        if (source != null && source instanceof Serializable)
            intent.putExtra("source", (Serializable) source);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return replyList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isDetail && position == 1) {
            return 0;
        } else if (!isDetail && position == 0) {
            return 0;
        }
        return 1;
    }

    public static class ReplyHolder extends RecyclerView.ViewHolder {
        final ImageView replyAvatar;
        final ImageView dislikeBtn;
        final CustomListView childReplies;
        final TextView message;
        final TextView userName;
        final TextView pubDate;
        final TextView childCount;
        final TextView likeCount;
        final TextView replyBtn;
        final TextView upLiked;
        final TextView imageCount;
        final TextView item_reply_delete;
        final LinearLayout childReplyCard;
        final ImageView imageCard;


        public ReplyHolder(@NonNull View itemView) {
            super(itemView);

            replyAvatar = itemView.findViewById(R.id.replyAvatar);
            dislikeBtn = itemView.findViewById(R.id.dislikeBtn);
            childReplies = itemView.findViewById(R.id.repliesList);
            message = itemView.findViewById(R.id.replyText);
            userName = itemView.findViewById(R.id.replyUsername);
            pubDate = itemView.findViewById(R.id.replyPubDate);
            childCount = itemView.findViewById(R.id.repliesControl);
            likeCount = itemView.findViewById(R.id.likes);
            replyBtn = itemView.findViewById(R.id.replyBtn);
            upLiked = itemView.findViewById(R.id.upLiked);
            childReplyCard = itemView.findViewById(R.id.repliesCard);
            imageCount = itemView.findViewById(R.id.imageCount);
            imageCard = itemView.findViewById(R.id.imageCard);
            item_reply_delete = itemView.findViewById(R.id.item_reply_delete);
        }
    }

    public static class WriteReply extends RecyclerView.ViewHolder {
        final MaterialButton write_reply;
        final MaterialButton sort;

        public WriteReply(@NonNull View itemView) {
            super(itemView);

            write_reply = itemView.findViewById(R.id.write_reply);
            sort = itemView.findViewById(R.id.sort);
        }
    }
}
