package com.RobinNotBad.BiliClient.adapter.dynamic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.message.PrivateMsgActivity;
import com.RobinNotBad.BiliClient.activity.user.FollowUsersActivity;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.ui.widget.RadiusBackgroundSpan;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.TerminalContext;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

//用户信息页专用Adapter 独立出来也是为了做首项不同

public class UserDynamicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final Context context;
    final ArrayList<Dynamic> dynamicList;
    final UserInfo userInfo;
    boolean desc_expand, notice_expand;
    boolean follow_onprocess;

    public UserDynamicAdapter(Context context, ArrayList<Dynamic> dynamicList, UserInfo userInfo) {
        this.context = context;
        this.dynamicList = dynamicList;
        this.userInfo = userInfo;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(context).inflate(R.layout.cell_user_info, parent, false);
            return new UserInfoHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.cell_dynamic, parent, false);
            return new DynamicHolder(view, (BaseActivity) context, false);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DynamicHolder) {
            int realPosition = position - 1;
            DynamicHolder dynamicHolder = (DynamicHolder) holder;

            dynamicHolder.showDynamic(dynamicList.get(realPosition), context, true);

            if (dynamicList.get(realPosition).dynamic_forward != null) {
                View childCard = dynamicHolder.cell_dynamic_child;
                DynamicHolder childHolder = new DynamicHolder(childCard, (BaseActivity) context, true);
                childHolder.showDynamic(dynamicList.get(realPosition).dynamic_forward, context, true);
                dynamicHolder.cell_dynamic_child.setVisibility(View.VISIBLE);
            } else {
                dynamicHolder.cell_dynamic_child.setVisibility(View.GONE);
            }

            View.OnLongClickListener onDeleteLongClick = DynamicHolder.getDeleteListener((Activity) context, dynamicList, realPosition, this);
            dynamicHolder.item_dynamic_delete.setOnLongClickListener(onDeleteLongClick);
            if (dynamicList.get(realPosition).canDelete)
                dynamicHolder.item_dynamic_delete.setVisibility(View.VISIBLE);
        }
        if (holder instanceof UserInfoHolder) {
            UserInfoHolder userInfoHolder = (UserInfoHolder) holder;

            SpannableStringBuilder lvStr = new SpannableStringBuilder("Lv" + userInfo.level);
            lvStr.setSpan(ToolsUtil.getLevelBadge(context, userInfo), 0, lvStr.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            if (userInfo.vip_role > 0) {
                LinkedHashMap<Integer, String> vipTypeMap = new LinkedHashMap<>() {{
                    put(1, "月度大会员");
                    put(3, "年度大会员");
                    put(7, "十年大会员");
                    put(15, "百年大会员");
                }};
                lvStr.append("  ").append(vipTypeMap.get(userInfo.vip_role)).append(" ");
                lvStr.setSpan(new RadiusBackgroundSpan(1, (int) context.getResources().getDimension(R.dimen.card_round), Color.WHITE, Color.rgb(207, 75, 95)), ("Lv" + userInfo.level).length() + 1, lvStr.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            userInfoHolder.userLevel.setText(lvStr);
            if (!userInfo.vip_nickname_color.isEmpty())
                userInfoHolder.userName.setTextColor(Color.parseColor(userInfo.vip_nickname_color));
            userInfoHolder.userName.setText(userInfo.name);
            userInfoHolder.userDesc.setText(userInfo.sign);
            if (!userInfo.notice.isEmpty()) userInfoHolder.userNotice.setText(userInfo.notice);
            else userInfoHolder.userNotice.setVisibility(View.GONE);
            userInfoHolder.uidTv.setText(String.valueOf(userInfo.mid));
            ToolsUtil.setCopy(userInfoHolder.uidTv);
            ToolsUtil.setLink(userInfoHolder.userDesc, userInfoHolder.userNotice);
            userInfoHolder.userFans.setText(ToolsUtil.toWan(userInfo.fans) + "粉丝");
            userInfoHolder.userFans.setOnClickListener((view) -> view.getContext().startActivity(new Intent(view.getContext(), FollowUsersActivity.class).putExtra("mode", 1).putExtra("mid", userInfo.mid)));
            userInfoHolder.userFollowings.setText(ToolsUtil.toWan(userInfo.following) + "关注");
            userInfoHolder.userFollowings.setOnClickListener((view) -> view.getContext().startActivity(new Intent(view.getContext(), FollowUsersActivity.class).putExtra("mode", 0).putExtra("mid", userInfo.mid)));

            if (userInfo.official != 0) {
                userInfoHolder.officialIcon.setVisibility(View.VISIBLE);
                userInfoHolder.userOfficial.setVisibility(View.VISIBLE);
                String[] official_signs = {"哔哩哔哩不知名UP主", "哔哩哔哩知名UP主", "哔哩哔哩大V达人", "哔哩哔哩企业认证",
                        "哔哩哔哩组织认证", "哔哩哔哩媒体认证", "哔哩哔哩政府认证", "哔哩哔哩高能主播", "社会不知名人士", "社会知名人士"};
                userInfoHolder.userOfficial.setText(official_signs[userInfo.official] + (userInfo.officialDesc.isEmpty() ? "" : ("\n" + userInfo.officialDesc)));
            } else {
                userInfoHolder.officialIcon.setVisibility(View.GONE);
                userInfoHolder.userOfficial.setVisibility(View.GONE);
            }

            Glide.with(this.context).asDrawable().load(GlideUtil.url(userInfo.avatar))
                    .transition(GlideUtil.getTransitionOptions())
                    .placeholder(R.mipmap.akari)
                    .apply(RequestOptions.circleCropTransform())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(userInfoHolder.userAvatar);

            userInfoHolder.userAvatar.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, ImageViewerActivity.class);
                ArrayList<String> imageList = new ArrayList<>();
                imageList.add(userInfo.avatar);
                intent.putExtra("imageList", imageList);
                context.startActivity(intent);
            });

            if (!userInfo.sys_notice.isEmpty()) {
                userInfoHolder.exclusiveTip.setVisibility(View.VISIBLE);
                SpannableString spannableString = new SpannableString("!:" + userInfo.sys_notice);
                Drawable drawable = ToolsUtil.getDrawable(context, R.drawable.icon_warning);
                drawable.setBounds(0, 0, 30, 30);
                spannableString.setSpan(new ImageSpan(drawable), 0, 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                userInfoHolder.exclusiveTipLabel.setText(spannableString);
            } else userInfoHolder.exclusiveTip.setVisibility(View.GONE);

            if (userInfo.live_room != null) {
                userInfoHolder.liveRoom.setVisibility(View.VISIBLE);
                userInfoHolder.liveRoomLabel.setText(userInfo.live_room.title);
                userInfoHolder.liveRoom.setOnClickListener(view -> TerminalContext.getInstance().enterLiveDetailPage(context, userInfo.live_room.roomid));
            } else userInfoHolder.liveRoom.setVisibility(View.GONE);

            if ((userInfo.mid == SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0)) || (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) || (userInfo.mid == 0))
                userInfoHolder.followBtn.setVisibility(View.GONE);
            else userInfoHolder.followBtn.setChecked(userInfo.followed);
            userInfoHolder.followBtn.setOnClickListener(btn -> {
                if (!follow_onprocess) {
                    follow_onprocess = true;
                    userInfoHolder.setFollowed(!(userInfo.followed));
                    CenterThreadPool.run(() -> {
                        try {
                            int result = UserInfoApi.followUser(userInfo.mid, !(userInfo.followed));
                            String msg;
                            if (result == 0) {
                                userInfo.followed = !(userInfo.followed);
                                msg = "操作成功喵~";
                            } else {
                                CenterThreadPool.runOnUiThread(() -> userInfoHolder.setFollowed(userInfo.followed));
                                if(result == 25056) msg = "被B站风控系统拦截了\n（无法解决）";
                                else msg = "操作失败（原因未知）：" + result;
                            }
                            MsgUtil.showMsg(msg);
                        } catch (Exception e) {
                            MsgUtil.err(e);
                        }
                        follow_onprocess = false;
                    });
                }
            });

            userInfoHolder.setFollowed(userInfo.followed);

            userInfoHolder.msgBtn.setOnClickListener(view -> {
                Intent intent = new Intent(context, PrivateMsgActivity.class);
                intent.putExtra("uid", userInfo.mid);
                context.startActivity(intent);
            });

            userInfoHolder.userDesc.setOnClickListener(view1 -> {
                if (desc_expand) userInfoHolder.userDesc.setMaxLines(2);
                else userInfoHolder.userDesc.setMaxLines(32);
                desc_expand = !desc_expand;
            });

            userInfoHolder.userNotice.setOnClickListener(view1 -> {
                if (notice_expand) userInfoHolder.userNotice.setMaxLines(2);
                else userInfoHolder.userNotice.setMaxLines(32);
                notice_expand = !notice_expand;
            });

        }
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return dynamicList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 ? 0 : 1);
    }

    public static class UserInfoHolder extends RecyclerView.ViewHolder {
        final TextView userName;
        final TextView userFollowings;
        final TextView userLevel;
        final TextView userFans;
        final TextView userDesc;
        final TextView userNotice;
        final TextView userOfficial;
        final TextView exclusiveTipLabel;
        final TextView liveRoomLabel;
        final MaterialCardView exclusiveTip;
        final MaterialCardView liveRoom;
        final ImageView userAvatar;
        final ImageView officialIcon;
        final TextView uidTv;
        final MaterialButton followBtn;
        final MaterialButton msgBtn;

        public UserInfoHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userDesc = itemView.findViewById(R.id.userDesc);
            userNotice = itemView.findViewById(R.id.userNotice);
            userLevel = itemView.findViewById(R.id.userLevel);
            userFans = itemView.findViewById(R.id.userFollowers);
            userFollowings = itemView.findViewById(R.id.userFollowings);
            userOfficial = itemView.findViewById(R.id.userOfficial);
            exclusiveTip = itemView.findViewById(R.id.exclusiveTip);
            exclusiveTipLabel = itemView.findViewById(R.id.exclusiveTipLabel);
            liveRoom = itemView.findViewById(R.id.liveRoom);
            liveRoomLabel = itemView.findViewById(R.id.liveRoomLabel);
            officialIcon = itemView.findViewById(R.id.officialIcon);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            followBtn = itemView.findViewById(R.id.followBtn);
            msgBtn = itemView.findViewById(R.id.msgBtn);
            uidTv = itemView.findViewById(R.id.uidText);
            ToolsUtil.setCopy(userDesc, userNotice);
        }

        public void setFollowed(boolean followed) {
            msgBtn.setVisibility((followed ? View.VISIBLE : View.GONE));
            followBtn.setBackgroundTintList(ColorStateList.valueOf((followed ? Color.argb(0xDD, 0x26, 0x26, 0x26) : Color.argb(0xFE, 0xF0, 0x5D, 0x8E))));
            followBtn.setText((followed ? "已关注" : "关注"));
        }
    }
}
