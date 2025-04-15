package com.RobinNotBad.BiliClient.adapter;

import static com.RobinNotBad.BiliClient.util.ToolsUtil.toWan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
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

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.reply.ReplyInfoActivity;
import com.RobinNotBad.BiliClient.activity.reply.WriteReplyActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.ui.widget.CustomListView;
import com.RobinNotBad.BiliClient.ui.widget.RadiusBackgroundSpan;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.EmoteUtil;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//评论Adapter
//2023-07-22

public class ReplyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public boolean isDetail = false;
    public boolean isManager = false;
    final Context context;
    final ArrayList<Reply> replyList;
    final long oid;
    final long up_mid;
    final long root;
    final int type;
    public int sort;
    public final int replyType;
    OnItemClickListener listener;

    public ReplyAdapter(Context context, ArrayList<Reply> replyList, long oid, long root, int type, int sort, long up_mid) {
        this.context = context;
        this.replyList = replyList;
        this.oid = oid;
        this.root = root;
        this.type = type;
        this.sort = sort;
        this.replyType = type;
        this.up_mid = up_mid;
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
            String[] sorts = {"未知排序", "未知排序", "时间排序", "热度排序"};
            if (isDetail) {
                writeReply.sort.setVisibility(View.GONE);
                writeReply.count_label.setVisibility(View.GONE);
            } else {
                writeReply.sort.setText(sorts[sort]);
                writeReply.sort.setOnClickListener(view -> {
                    if (this.listener != null) listener.onItemClick(0);
                    writeReply.sort.setText(sorts[sort]);
                });

                CenterThreadPool.run(() -> {
                    try {
                        long count = ReplyApi.getReplyCount(oid, type);
                        CenterThreadPool.runOnUiThread(() -> writeReply.count_label.setText("共" + count + "条评论"));
                    } catch (Exception ignore) {
                    }
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
            Reply reply = replyList.get(realPosition);

            Glide.with(BiliTerminal.context).asDrawable().load(GlideUtil.url(reply.sender.avatar))
                    .transition(GlideUtil.getTransitionOptions())
                    .placeholder(R.mipmap.akari)
                    .apply(RequestOptions.circleCropTransform())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(replyHolder.replyAvatar);

            UserInfo sender = reply.sender;
            SpannableStringBuilder name_str = new SpannableStringBuilder(reply.sender.name);

            //大会员红字
            if (!TextUtils.isEmpty(sender.vip_nickname_color) && !SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.NO_VIP_COLOR, false))
                replyHolder.userName.setTextColor(Color.parseColor(sender.vip_nickname_color));

            //up主标识
            if (sender.mid == up_mid) {
                name_str = new SpannableStringBuilder(" UP " + reply.sender.name);
                name_str.setSpan(new RadiusBackgroundSpan(2, (int) context.getResources().getDimension(R.dimen.round_small), Color.WHITE, Color.rgb(207, 75, 95)), 0, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                name_str.setSpan(new RelativeSizeSpan(0.8f), 0, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            int last_length = name_str.length();
            name_str.append(" ").append(String.valueOf(sender.level));
            if(sender.is_senior_member == 1) name_str.append("+");
            name_str.setSpan(ToolsUtil.getLevelBadge(context, sender), last_length + 1, name_str.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            //等级
            if (!TextUtils.isEmpty(sender.medal_name)) {
                last_length = name_str.length();
                name_str.append("  ").append(sender.medal_name).append("Lv").append(String.valueOf(sender.medal_level)).append(" ");
                name_str.setSpan(new RadiusBackgroundSpan(2, (int) context.getResources().getDimension(R.dimen.round_small), Color.WHITE, Color.argb(140, 158, 186, 232)), last_length + 1, name_str.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                name_str.setSpan(new RelativeSizeSpan(0.8f), last_length + 1, name_str.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            replyHolder.userName.setText(name_str);

            if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.REPLY_MARQUEE_NAME, true)) {
                replyHolder.userName.setSingleLine(true);
                replyHolder.userName.setMaxLines(1);
            } else {
                replyHolder.userName.setSingleLine(false);
                replyHolder.userName.setMaxLines(3);
            }


            String text = reply.message;
            replyHolder.message.setText(text);  //防止加载速度慢时露出鸡脚
            ToolsUtil.setCopy(replyHolder.message);
            if (reply.emotes != null) {
                CenterThreadPool.run(() -> {
                    try {
                        SpannableString spannableString = EmoteUtil.textReplaceEmote(text, reply.emotes, 1.0f, context, replyHolder.message.getText());
                        CenterThreadPool.runOnUiThread(() -> {
                            replyHolder.message.setText(spannableString);
                            setTopSpan(reply, replyHolder);
                            ToolsUtil.setLink(replyHolder.message);
                            ToolsUtil.setAtLink(reply.atNameToMid, replyHolder.message);
                        });
                    } catch (Exception ignored) {}
                });
            }
            else setTopSpan(reply, replyHolder);

            ToolsUtil.setLink(replyHolder.message);
            ToolsUtil.setAtLink(reply.atNameToMid, replyHolder.message);

            replyHolder.likeCount.setText(toWan(reply.likeCount));

            if (reply.liked) {
                replyHolder.likeCount.setTextColor(Color.rgb(0xfe, 0x67, 0x9a));
                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.icon_reply_like1), null, null, null);
            } else {
                replyHolder.likeCount.setTextColor(Color.rgb(0xff, 0xff, 0xff));
                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.icon_reply_like0), null, null, null);
            }

            if (reply.childCount != 0 && !(realPosition == 0 && isDetail)) {
                replyHolder.childReplyCard.setVisibility(View.VISIBLE);

                if (reply.upReplied)
                    replyHolder.childCount.setText("UP主在内 共" + reply.childCount + "条回复");
                else
                    replyHolder.childCount.setText("共" + reply.childCount + "条回复");

                if (reply.childMsgList != null) {
                    final String up_tip = "  UP  ";
                    List<CharSequence> childStrList = new ArrayList<>();
                    CenterThreadPool.run(() -> {
                        try {
                            for (Reply child : reply.childMsgList) {
                                String senderName = child.sender.name;
                                SpannableString content = new SpannableString(senderName + (child.sender.mid == up_mid ? up_tip : "") + "：" + child.message);
                                if (child.sender.mid == up_mid) {
                                    float lineHeight = ToolsUtil.getTextHeightWithSize(context);
                                    content.setSpan(new RadiusBackgroundSpan(2, (int) context.getResources().getDimension(R.dimen.card_round), Color.WHITE, Color.rgb(207, 75, 95), (int) (lineHeight)), senderName.length(), senderName.length() + up_tip.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                                    content.setSpan(new RelativeSizeSpan(0.8f), senderName.length(), senderName.length() + up_tip.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                                }
                                if (child.emotes != null) {
                                    EmoteUtil.textReplaceEmote(content.toString(), child.emotes, 1.0f, context, content);
                                }
                                childStrList.add(content);
                            }
                            replyHolder.childReplies.setVerticalScrollBarEnabled(false);
                            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(context, R.layout.cell_reply_child, childStrList);
                            replyHolder.childReplies.setAdapter(adapter);
                        } catch (Exception ignored){}
                    });
                }
            } else replyHolder.childReplyCard.setVisibility(View.GONE);

            if (reply.upLiked)
                replyHolder.upLiked.setVisibility(View.VISIBLE);
            else replyHolder.upLiked.setVisibility(View.GONE);
            replyHolder.pubDate.setText(reply.pubTime);

            if (reply.pictureList != null && !reply.pictureList.isEmpty()) {  //图片显示相关
                replyHolder.imageCard.setVisibility(View.VISIBLE);
                replyHolder.imageCount.setVisibility(View.VISIBLE);
                Glide.with(BiliTerminal.context).asDrawable().load(GlideUtil.url(reply.pictureList.get(0)))
                        .transition(GlideUtil.getTransitionOptions())
                        .placeholder(R.mipmap.placeholder)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(replyHolder.imageCard);

                replyHolder.imageCount.setText("共" + reply.pictureList.size() + "张图片");
                replyHolder.imageCard.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, ImageViewerActivity.class);
                    intent.putExtra("imageList", reply.pictureList);
                    context.startActivity(intent);
                });
            } else {
                replyHolder.imageCount.setVisibility(View.GONE);
                replyHolder.imageCard.setVisibility(View.GONE);
            }

            replyHolder.childReplyCard.setOnClickListener(view -> startReplyInfoActivity(reply));
            replyHolder.childReplies.setOnItemClickListener((adapterView, view, i, l) -> startReplyInfoActivity(reply));
            if (!isDetail) {
                replyHolder.itemView.setOnClickListener((view) -> startReplyInfoActivity(reply));
                replyHolder.message.setOnClickListener((view) -> startReplyInfoActivity(reply));
            }

            replyHolder.replyAvatar.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, UserInfoActivity.class);
                intent.putExtra("mid", reply.sender.mid);
                context.startActivity(intent);
            });

            replyHolder.likeCount.setOnClickListener(view -> CenterThreadPool.run(() -> {
                if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                    ((Activity) context).runOnUiThread(() -> MsgUtil.showMsg("还没有登录喵~"));
                    return;
                }
                if (!reply.liked) {
                    try {
                        if (ReplyApi.likeReply(oid, reply.rpid, true) == 0) {
                            reply.liked = true;
                            ((Activity) context).runOnUiThread(() -> {
                                MsgUtil.showMsg("点赞成功");
                                replyHolder.likeCount.setText(toWan(++reply.likeCount));
                                replyHolder.likeCount.setTextColor(Color.rgb(0xfe, 0x67, 0x9a));
                                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.icon_reply_like1), null, null, null);
                            });
                        } else
                            ((Activity) context).runOnUiThread(() -> MsgUtil.showMsg("点赞失败"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (ReplyApi.likeReply(oid, reply.rpid, false) == 0) {
                            reply.liked = false;
                            ((Activity) context).runOnUiThread(() -> {
                                MsgUtil.showMsg("取消成功");
                                replyHolder.likeCount.setText(toWan(--reply.likeCount));
                                replyHolder.likeCount.setTextColor(Color.rgb(0xff, 0xff, 0xff));
                                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.icon_reply_like0), null, null, null);
                            });
                        } else
                            ((Activity) context).runOnUiThread(() -> MsgUtil.showMsg("取消失败"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }));

            //删除按钮
            if(isManager || reply.sender.mid == SharedPreferencesUtil.getLong("mid",0)) {
                View.OnClickListener onDeleteClick = view -> MsgUtil.showMsg("长按删除");
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
                                    int result = ReplyApi.deleteReply(oid, reply.rpid, replyType);
                                    if (result == 0) {
                                        replyList.remove(realPosition);
                                        ((Activity) context).runOnUiThread(() -> {
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, replyList.size() - position);
                                            longClickPosition = -1;
                                            MsgUtil.showMsg("删除成功~");
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
                                        ((Activity) context).runOnUiThread(() -> MsgUtil.showMsg(finalMsg));
                                    }
                                } catch (Exception e) {
                                    ((Activity) context).runOnUiThread(() -> MsgUtil.err(e));
                                }
                            });
                        } else {
                            longClickPosition = realPosition;
                            longClickTime = currentTime;
                            MsgUtil.showMsg("再次长按删除");
                        }
                        return true;
                    }
                };
                replyHolder.item_reply_delete.setOnLongClickListener(onDeleteLongClick);
                replyHolder.item_reply_delete.setVisibility(View.VISIBLE);
            }
            else replyHolder.item_reply_delete.setVisibility(View.GONE);

            //回复按钮
            replyHolder.replyBtn.setOnClickListener(view -> {
                boolean noParent = isDetail && realPosition == 0;
                Intent intent = new Intent();
                intent.setClass(context, WriteReplyActivity.class);
                intent.putExtra("oid", oid);
                intent.putExtra("rpid", noParent ? root : reply.rpid);
                intent.putExtra("parent", noParent ? root : reply.rpid);
                intent.putExtra("replyType", replyType);
                intent.putExtra("pos", realPosition);
                if (root != 0 && !noParent)
                    intent.putExtra("parentSender", reply.sender.name);
                else intent.putExtra("parentSender", "");
                context.startActivity(intent);
            });
        }
    }

    public void setTopSpan(Reply reply, ReplyHolder replyHolder) {
        if (reply.isTop && reply.message.startsWith(ReplyApi.TOP_TIP)) {
            SpannableString spannableString = new SpannableString(replyHolder.message.getText());
            spannableString.setSpan(new ForegroundColorSpan(Color.rgb(207, 75, 95)), 0, ReplyApi.TOP_TIP.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            replyHolder.message.setText(spannableString);
        }
    }

    public void startReplyInfoActivity(Reply reply) {
        long rpid = reply.rpid;
        long oid = reply.oid;
        Intent intent = new Intent();
        intent.setClass(context, ReplyInfoActivity.class);
        intent.putExtra("rpid", rpid);
        intent.putExtra("oid", oid);
        intent.putExtra("type", replyType);
        intent.putExtra("up_mid", up_mid);
        intent.putExtra("is_manager", isManager);
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
        final TextView count_label;

        public WriteReply(@NonNull View itemView) {
            super(itemView);

            write_reply = itemView.findViewById(R.id.write_reply);
            sort = itemView.findViewById(R.id.sort);
            count_label = itemView.findViewById(R.id.count_label);
        }
    }
}
