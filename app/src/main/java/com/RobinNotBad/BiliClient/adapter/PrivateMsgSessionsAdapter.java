package com.RobinNotBad.BiliClient.adapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.message.PrivateMsgActivity;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgAdapter;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.model.PrivateMsgSession;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;

public class PrivateMsgSessionsAdapter extends RecyclerView.Adapter<PrivateMsgSessionsAdapter.PrivateMsgSessionsHolder> {

    Context context;
    ArrayList<PrivateMsgSession> sessionsList;
    HashMap<Long,UserInfo> userMap;

    public PrivateMsgSessionsAdapter(Context context, ArrayList<PrivateMsgSession> sessionsList,HashMap<Long,UserInfo> userMap) {
        this.context = context;
        this.sessionsList = sessionsList;
        this.userMap = userMap;
    }

    @Override
    public PrivateMsgSessionsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_user_list,parent,false);
        return new PrivateMsgSessionsHolder(view);
    }

    @Override
    public void onBindViewHolder(PrivateMsgSessionsHolder holder, int position) {
        PrivateMsgSession msgContent = sessionsList.get(position);
        try {
            if (msgContent.contentType == PrivateMessage.TYPE_TEXT) {
                holder.contentText.setText(msgContent.content.getString("content"));
            }if(msgContent.contentType == PrivateMessage.TYPE_PIC) {
            	holder.contentText.setText("[图片消息]");
            }if(msgContent.contentType == PrivateMessage.TYPE_VIDEO||msgContent.contentType==PrivateMessage.TYPE_PIC_CARD||msgContent.contentType  == PrivateMessage.TYPE_NOMAL_CARD) {
            	holder.contentText.setText(msgContent.content.getString("title"));
            }if(msgContent.contentType == PrivateMessage.TYPE_TEXT_WITH_VIDEO) {
            	holder.contentText.setText(msgContent.content.getString("reply_content"));
            }if(msgContent.contentType == PrivateMessage.TYPE_RETRACT) {
            	holder.contentText.setText("[撤回消息]");
            }
            holder.nameText.setText(userMap.get(msgContent.talkerUid).name);
            Glide.with(context).load(userMap.get(msgContent.talkerUid).avatar)
                            .placeholder(R.drawable.akari)
                            .apply(RequestOptions.circleCropTransform())
                            .into(holder.avatarView);
            holder.itemView.setOnClickListener(view->{
                Intent intent = new Intent(context,PrivateMsgActivity.class);
                intent.putExtra("uid",msgContent.talkerUid);
                context.startActivity(intent);
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