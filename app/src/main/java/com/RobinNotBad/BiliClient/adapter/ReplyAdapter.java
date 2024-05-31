package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
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
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.ReplyInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.WriteReplyActivity;
import com.RobinNotBad.BiliClient.api.ArticleApi;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.EmoteUtil;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.RobinNotBad.BiliClient.view.CustomListView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

//评论Adapter
//2023-07-22

public class ReplyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public boolean isDetail = false;
    Context context;
    ArrayList<Reply> replyList;
    long oid, root;
    int type, sort;
    public int replyType;
    OnItemClickListener listener;

    public ReplyAdapter(Context context, ArrayList<Reply> replyList, long oid, long root, int type, int sort, int replyType) {
        this.context = context;
        this.replyList = replyList;
        this.oid = oid;
        this.root = root;
        this.type = type;
        this.sort = sort;
        this.replyType = type;
    }

    public ReplyAdapter(Context context, ArrayList<Reply> replyList, long oid, long root, int type, int sort) {
        this(context, replyList, oid, root, type, sort, ReplyApi.REPLY_TYPE_VIDEO);
    }

    public void setOnSortSwitchListener(OnItemClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_reply_action,parent,false);
            return new WriteReply(view);
        } else {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_reply_list,parent,false);
            return new ReplyHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof WriteReply){
            WriteReply writeReply = (WriteReply) holder;
            writeReply.write_reply.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, WriteReplyActivity.class);
                intent.putExtra("oid",oid);
                intent.putExtra("rpid",root);
                intent.putExtra("parent",root);
                intent.putExtra("parentSender","");
                intent.putExtra("replyType", replyType);
                context.startActivity(intent);
            });
            String[] sorts = {"时间排序","点赞排序","回复排序"};
            writeReply.sort.setText(sorts[sort]);
            writeReply.sort.setOnClickListener(view -> {
                if(this.listener!=null) listener.onItemClick(0);
            });
        }
        if(holder instanceof ReplyHolder) {
            int tmpPosition;
            if (isDetail) {
                tmpPosition = position != 0 ? position - 1 : 0;
            } else {
                tmpPosition = position - 1;
            }
            int realPosition = tmpPosition;
            ReplyHolder replyHolder = (ReplyHolder) holder;

            Glide.with(context).load(GlideUtil.url(replyList.get(realPosition).sender.avatar))
                    .placeholder(R.mipmap.akari)
                    .apply(RequestOptions.circleCropTransform())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(replyHolder.replyAvatar);
            replyHolder.userName.setText(replyList.get(realPosition).sender.name);

            String text = replyList.get(realPosition).message;
            replyHolder.message.setText(text);  //防止加载速度慢时露出鸡脚
            ToolsUtil.setCopy(replyHolder.message,context);
            if(replyList.get(realPosition).emotes != null) {
                CenterThreadPool.run(() -> {
                    try {
                        SpannableString spannableString = EmoteUtil.textReplaceEmote(text, replyList.get(realPosition).emotes, 1.0f, context, replyHolder.message.getText());
                        ((Activity) context).runOnUiThread(() -> {
                            replyHolder.message.setText(spannableString);
                            ToolsUtil.setLink(replyHolder.message);
                            ToolsUtil.setAtLink(replyList.get(realPosition).atNameToMid, replyHolder.message);
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

            ToolsUtil.setLink(replyHolder.message);
            ToolsUtil.setAtLink(replyList.get(realPosition).atNameToMid, replyHolder.message);

            replyHolder.likeCount.setText(String.valueOf(replyList.get(realPosition).likeCount));

            if (replyList.get(realPosition).liked) {
                replyHolder.likeCount.setTextColor(Color.rgb(0xfe,0x67,0x9a));
                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_liked),null,null,null);
            } else {
                replyHolder.likeCount.setTextColor(Color.rgb(0xff,0xff,0xff));
                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_like),null,null,null);
            }

            if (replyList.get(realPosition).childCount != 0 && !(realPosition == 0 && isDetail)) {
                replyHolder.childReplyCard.setVisibility(View.VISIBLE);

                if (replyList.get(realPosition).upReplied) replyHolder.childCount.setText("UP主在内 共" + replyList.get(realPosition).childCount + "条回复");
                else replyHolder.childCount.setText("共" + replyList.get(realPosition).childCount + "条回复");

                if(replyList.get(realPosition).childMsgList != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.cell_reply_child, replyList.get(realPosition).childMsgList);
                    replyHolder.childReplies.setAdapter(adapter);
                }
            } else replyHolder.childReplyCard.setVisibility(View.GONE);

            if (replyList.get(realPosition).upLiked) replyHolder.upLiked.setVisibility(View.VISIBLE);
            else replyHolder.upLiked.setVisibility(View.GONE);
            replyHolder.pubDate.setText(replyList.get(realPosition).pubTime);

            if (replyList.get(realPosition).pictureList != null && !replyList.get(realPosition).pictureList.isEmpty()) {  //图片显示相关
                replyHolder.imageCard.setVisibility(View.VISIBLE);
                replyHolder.imageCount.setVisibility(View.VISIBLE);
                Glide.with(context).load(GlideUtil.url(replyList.get(realPosition).pictureList.get(0)))
                        .placeholder(R.mipmap.placeholder)
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

            replyHolder.childReplyCard.setOnClickListener(view -> {
                startReplyInfoActivity(replyList.get(realPosition));
            });
            replyHolder.childReplies.setOnItemClickListener((adapterView, view, i, l) -> {
                startReplyInfoActivity(replyList.get(realPosition));
            });
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
                if (!replyList.get(realPosition).liked) {
                    try {
                        if (ReplyApi.likeReply(oid, replyList.get(realPosition).rpid, true) == 0) {
                            replyList.get(realPosition).liked = true;
                            ((Activity) context).runOnUiThread(() -> {
                                MsgUtil.toast("点赞成功",context);
                                replyHolder.likeCount.setText(String.valueOf(replyList.get(realPosition).likeCount + 1));
                                replyHolder.likeCount.setTextColor(Color.rgb(0xfe,0x67,0x9a));
                                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_liked),null,null,null);
                            });
                        } else
                            ((Activity) context).runOnUiThread(() -> MsgUtil.toast("点赞失败",context));
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
                                MsgUtil.toast("取消成功",context);
                                replyHolder.likeCount.setText(String.valueOf(replyList.get(realPosition).likeCount));
                                replyHolder.likeCount.setTextColor(Color.rgb(0xff,0xff,0xff));
                                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_like),null,null,null);
                            });
                        } else
                            ((Activity) context).runOnUiThread(() -> MsgUtil.toast("取消失败",context));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }));

            View.OnClickListener onDeleteClick = view -> {
                MsgUtil.toast("长按删除", context);
            };
            replyHolder.item_reply_delete_img.setOnClickListener(onDeleteClick);
            replyHolder.item_reply_delete.setOnClickListener(onDeleteClick);
            View.OnLongClickListener onDeleteLongClick = new View.OnLongClickListener() {
                private int longClickPosition = -1;
                private long longClickTime = -1;
                @Override
                public boolean onLongClick(View view) {
                    long currentTime = System.currentTimeMillis();
                    if (longClickPosition == realPosition && currentTime - longClickTime < 10000) {
                        CenterThreadPool.run(() -> {
                            try {
                                int result = ReplyApi.deleteReply(oid, replyList.get(realPosition).rpid, replyType);
                                if (result == 0) {
                                    replyList.remove(realPosition);
                                    ((Activity) context).runOnUiThread(() -> {
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, replyList.size() - realPosition);
                                        longClickPosition = -1;
                                        MsgUtil.toast("删除成功~", context);
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
                                    ((Activity) context).runOnUiThread(() -> MsgUtil.toast(finalMsg, context));
                                }
                            } catch (Exception e) {
                                ((Activity) context).runOnUiThread(() -> MsgUtil.err(e, context));
                            }
                        });
                    } else {
                        longClickPosition = realPosition;
                        longClickTime = currentTime;
                        MsgUtil.toast("再次长按删除", context);
                    }
                    return true;
                }
            };
            replyHolder.item_reply_delete_img.setOnLongClickListener(onDeleteLongClick);
            replyHolder.item_reply_delete.setOnLongClickListener(onDeleteLongClick);
            if (!(replyList.get(realPosition).sender.mid == SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,0)) || replyList.get(realPosition).sender.mid == 0 || replyList.get(realPosition).forceDelete) {
                replyHolder.item_reply_delete_img.setVisibility(View.GONE);
                replyHolder.item_reply_delete.setVisibility(View.GONE);
                CenterThreadPool.run(() -> {
                    try {
                        Reply reply = replyList.get(realPosition);
                        boolean isManager = false;
                        switch (replyType) {
                            case ReplyApi.REPLY_TYPE_VIDEO:
                                ArrayList<UserInfo> staffs = VideoInfoApi.getInfoByJson(Objects.requireNonNull(VideoInfoApi.getJsonByAid(reply.oid))).staff;
                                for (UserInfo userInfo : staffs) {
                                    if (userInfo.mid == SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,0)) {
                                        isManager = true;
                                        break;
                                    }
                                }
                                break;
                            case ReplyApi.REPLY_TYPE_DYNAMIC:
                                isManager = DynamicApi.getDynamic(reply.oid).userInfo.mid == SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0);
                                break;
                            case ReplyApi.REPLY_TYPE_ARTICLE:
                                isManager = Objects.requireNonNull(ArticleApi.getArticle(reply.oid)).upInfo.mid == SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0);
                                break;
                        }
                        if (isManager) {
                            ((Activity) context).runOnUiThread(() -> {
                                replyHolder.item_reply_delete_img.setVisibility(View.VISIBLE);
                                replyHolder.item_reply_delete.setVisibility(View.VISIBLE);
                            });
                        }
                    } catch (Exception e) {
                        ((Activity) context).runOnUiThread(() -> MsgUtil.err(e, context));
                    }
                });
            }

            replyHolder.replyBtn.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, WriteReplyActivity.class);
                intent.putExtra("oid",oid);
                intent.putExtra("rpid",replyList.get(realPosition).rpid);
                intent.putExtra("parent",replyList.get(realPosition).rpid);
                intent.putExtra("replyType", replyType);
                if(root!=0) intent.putExtra("parentSender",replyList.get(realPosition).sender.name);
                else intent.putExtra("parentSender","");
                context.startActivity(intent);
            });
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
        intent.putExtra("type",type);
        intent.putExtra("origReply", reply);
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

    public static class ReplyHolder extends RecyclerView.ViewHolder{
        ImageView replyAvatar, dislikeBtn, item_reply_delete_img;
        CustomListView childReplies;
        TextView message,userName,pubDate,childCount,likeCount,replyBtn,upLiked,imageCount, item_reply_delete;
        LinearLayout childReplyCard;
        ImageView imageCard;


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
            item_reply_delete_img = itemView.findViewById(R.id.item_reply_delete_img);
            item_reply_delete = itemView.findViewById(R.id.item_reply_delete);
        }
    }

    public static class WriteReply extends RecyclerView.ViewHolder{
        MaterialButton write_reply, sort;

        public WriteReply(@NonNull View itemView) {
            super(itemView);

            write_reply = itemView.findViewById(R.id.write_reply);
            sort = itemView.findViewById(R.id.sort);
        }
    }
}
