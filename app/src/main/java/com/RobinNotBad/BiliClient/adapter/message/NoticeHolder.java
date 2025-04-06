package com.RobinNotBad.BiliClient.adapter.message;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardHolder;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.model.MessageCard;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.TerminalContext;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;

public class NoticeHolder extends RecyclerView.ViewHolder {
    public final LinearLayout avaterList;
    public final TextView action;
    public final TextView pubdate;
    public final ConstraintLayout extraCard;
    public final View itemView;

    public NoticeHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        avaterList = itemView.findViewById(R.id.avatar_list);
        action = itemView.findViewById(R.id.action);
        pubdate = itemView.findViewById(R.id.pubdate);
        extraCard = itemView.findViewById(R.id.extraCard);
    }

    @SuppressLint("SetTextI18n")
    public void showMessage(MessageCard message, Context context) {
        avaterList.removeAllViews();
        if (message.user.isEmpty()) avaterList.setVisibility(View.GONE);
        else avaterList.setVisibility(View.VISIBLE);
        for (int i = 0; i < message.user.size(); i++) {
            ImageView imageView = new ImageView(context);
            Glide.with(BiliTerminal.context)
                    .asDrawable()
                    .load(GlideUtil.url(message.user.get(i).avatar))
                    .transition(GlideUtil.getTransitionOptions())
                    .placeholder(R.mipmap.akari)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ToolsUtil.dp2px(32), ToolsUtil.dp2px(32)));
            imageView.setLeft(ToolsUtil.dp2px(3));
            int finalI = i;
            imageView.setOnClickListener(view1 -> {
                Intent intent = new Intent();
                intent.setClass(context, UserInfoActivity.class);
                intent.putExtra("mid", message.user.get(finalI).mid);
                context.startActivity(intent);
            });
            avaterList.addView(imageView);

            //这个View什么都没有，用来当间隔的
            View view = new View(context);
            view.setLayoutParams(new ViewGroup.LayoutParams(ToolsUtil.dp2px(3), ToolsUtil.dp2px(32)));
            avaterList.addView(view);
        }

        if (message.timeStamp != 0) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            pubdate.setText(sdf.format(message.timeStamp * 1000));
        } else pubdate.setText(message.timeDesc);

        action.setText(message.content);
        ToolsUtil.setCopy(action);

        if (message.videoCard != null) {
            VideoCard childVideoCard = message.videoCard;
            VideoCardHolder holder = new VideoCardHolder(View.inflate(context, R.layout.cell_dynamic_video, extraCard));
            holder.showVideoCard(childVideoCard, context);
            holder.itemView.findViewById(R.id.videoCardView).setOnClickListener(view ->
                TerminalContext.getInstance().enterVideoDetailPage(context, 0, childVideoCard.bvid)
            );
        }
        if (message.replyInfo != null || message.dynamicInfo != null) {
            Reply childReply = message.replyInfo != null ? message.replyInfo : message.dynamicInfo;
            ReplyCardHolder holder = new ReplyCardHolder(View.inflate(context, R.layout.cell_message_reply, extraCard));
            holder.showReplyCard(childReply);
            holder.itemView.findViewById(R.id.cardView).setOnClickListener(view -> {
                try {
                    if(message.itemType.equals("reply") || message.getType == MessageCard.GET_TYPE_REPLY) {
                        long seekReply = message.rootId == 0 ? message.sourceId : message.rootId;
                        switch (message.businessId) {
                            case ReplyApi.REPLY_TYPE_VIDEO_CHILD:
                                MsgUtil.showMsg("视频的子评论暂时无法定位，也许以后会做吧……");
                                //Todo 定位子评论 MessageApi:70 可拆分native_uri获得id
                            case ReplyApi.REPLY_TYPE_VIDEO:
                                TerminalContext.getInstance().enterVideoDetailPage(context, 0, childReply.ofBvid, null, seekReply);
                                break;
                            case ReplyApi.REPLY_TYPE_DYNAMIC_CHILD:
                                MsgUtil.showMsg("动态的子评论暂时无法跳转，也许以后会做吧……");
                                break;
                            case ReplyApi.REPLY_TYPE_DYNAMIC:
                                TerminalContext.getInstance().enterDynamicDetailPage(context, message.subjectId, 0, seekReply);
                                break;
                            case ReplyApi.REPLY_TYPE_ARTICLE:
                                TerminalContext.getInstance().enterArticleDetailPage(context, message.subjectId, seekReply);
                                break;
                            default:
                                MsgUtil.showMsg("不支持这个类型喵：" + message.businessId);
                        }
                    }
                    else switch (message.getType) {
                        case MessageCard.GET_TYPE_LIKE:
                        case MessageCard.GET_TYPE_AT:
                            switch (message.itemType) {
                                case "video":
                                    TerminalContext.getInstance().enterVideoDetailPage(context, 0, childReply.ofBvid);
                                    break;
                                case "dynamic":
                                    TerminalContext.getInstance().enterDynamicDetailPage(context, message.subjectId);
                                    break;
                                case "article":
                                    TerminalContext.getInstance().enterArticleDetailPage(context, message.subjectId);
                                    break;
                                default:
                                    MsgUtil.showMsg("不支持这个类型喵：" + message.itemType);
                            }
                            break;
                    }

                } catch (Exception e) {
                    MsgUtil.err("跳转出错？", e);
                }
            });
        }
    }
}
