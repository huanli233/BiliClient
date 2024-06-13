package com.RobinNotBad.BiliClient.adapter.message;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.message.PrivateMsgActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.model.PrivateMsgSession;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class PrivateMsgSessionsAdapter extends RecyclerView.Adapter<PrivateMsgSessionsAdapter.PrivateMsgSessionsHolder> {

    Context context;
    ArrayList<PrivateMsgSession> sessionsList;
    HashMap<Long,UserInfo> userMap;

    public PrivateMsgSessionsAdapter(Context context, ArrayList<PrivateMsgSession> sessionsList,HashMap<Long,UserInfo> userMap) {
        this.context = context;
        this.sessionsList = sessionsList;
        this.userMap = userMap;
    }

    @NonNull
    @Override
    public PrivateMsgSessionsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_user_list,parent,false);
        return new PrivateMsgSessionsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrivateMsgSessionsHolder holder, int position) {
        PrivateMsgSession msgContent = sessionsList.get(position);
        try {
            switch (msgContent.contentType){
                case PrivateMessage.TYPE_TEXT:
                    holder.contentText.setText(msgContent.content.getString("content"));
                    break;
                case PrivateMessage.TYPE_PIC:
                    holder.contentText.setText("[图片消息]");
                    break;

                case PrivateMessage.TYPE_VIDEO:
                case PrivateMessage.TYPE_PIC_CARD:
                case PrivateMessage.TYPE_NOMAL_CARD:
                    holder.contentText.setText(msgContent.content.getString("title"));
                    break;

                case PrivateMessage.TYPE_TEXT_WITH_VIDEO:
                    holder.contentText.setText(msgContent.content.getString("reply_content"));
                    break;
                case PrivateMessage.TYPE_RETRACT:
                    holder.contentText.setText("[撤回消息]");
                    break;

                default:
                    holder.contentText.setText("");
            }
            holder.contentText.setEllipsize(TextUtils.TruncateAt.END);
            holder.nameText.setText(Objects.requireNonNull(userMap.get(msgContent.talkerUid)).name);
            Glide.with(context).asDrawable().load(GlideUtil.url(Objects.requireNonNull(userMap.get(msgContent.talkerUid)).avatar))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.mipmap.akari)
                            .apply(RequestOptions.circleCropTransform())
                            .into(holder.avatarView);
            holder.itemView.setOnClickListener(view->{
                Intent intent = new Intent(context,PrivateMsgActivity.class);
                intent.putExtra("uid",msgContent.talkerUid);
                context.startActivity(intent);
            });
            holder.itemView.setOnLongClickListener(view->{
                Intent intent = new Intent(context,UserInfoActivity.class);
                intent.putExtra("mid",msgContent.talkerUid);
                context.startActivity(intent);
                return true;
            });
        } catch (JSONException err) {
            Log.e("PrivateMsgUserAdapter",err.toString());
        }
    }

    @Override
    public int getItemCount() {
        return sessionsList.size();
    }


    public static class PrivateMsgSessionsHolder extends RecyclerView.ViewHolder{
        ImageView avatarView;
        TextView nameText;
        TextView contentText;
        public PrivateMsgSessionsHolder(View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.userAvatar);
            nameText = itemView.findViewById(R.id.userName);
            contentText = itemView.findViewById(R.id.userDesc);
        }

    }
}