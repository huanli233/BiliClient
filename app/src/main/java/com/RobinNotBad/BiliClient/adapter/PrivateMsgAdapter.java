package com.RobinNotBad.BiliClient.adapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.BiliClient;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgAdapter;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Cache;
import org.json.JSONException;

public class PrivateMsgAdapter extends RecyclerView.Adapter<PrivateMsgAdapter.ViewHolder>{
    private ArrayList<PrivateMessage> mPrivateMsgList=new ArrayList<PrivateMessage>();
    private long selfUid = -1;
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView nameTv,textContentTv,tipTv,playTimesTv,upNameTv,videoTitleTv;
        MaterialCardView textContentCard,videoCard;
        ImageView picMsg,videoCover;
        LinearLayout root;
        
        public ViewHolder(View view){
            super(view);
            root = (LinearLayout)view.findViewById(R.id.msg_layout);
            nameTv = (TextView)view.findViewById(R.id.msg_name);
            textContentTv = (TextView)view.findViewById(R.id.msg_text_content);
            tipTv = (TextView)view.findViewById(R.id.msg_type_tip_text);
            playTimesTv = (TextView)view.findViewById(R.id.listPlayTimes);
            upNameTv = (TextView)view.findViewById(R.id.listUpName);
            videoTitleTv = (TextView)view.findViewById(R.id.listVideoTitle);
            textContentCard = (MaterialCardView)view.findViewById(R.id.msg_type_text_card);
            videoCard = (MaterialCardView)view.findViewById(R.id.cardView);
            picMsg = (ImageView)view.findViewById(R.id.msg_type_pic);
            videoCover = (ImageView)view.findViewById(R.id.listCover);
        }
    }
    public PrivateMsgAdapter(ArrayList<PrivateMessage> msgList){
        mPrivateMsgList=msgList;
    }
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_private_msg,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        PrivateMessage msg = mPrivateMsgList.get(position);
        try {
            holder.nameTv.setText(msg.name);
            if(selfUid ==-1) {
            	CenterThreadPool.run(()->{
                        try {
                        	selfUid = UserInfoApi.getCurrentUserInfo().mid;
                        } catch(IOException err) {
                        	Log.e(PrivateMessage.class.getName(), err.toString());
                        } catch(JSONException err){
                            Log.e(PrivateMessage.class.getName(), err.toString());
                        }
                    
                });
            }
            if(msg.uid==selfUid) {
            	holder.root.setGravity(Gravity.END);
                holder.textContentCard.setCardBackgroundColor(R.color.pink);
                holder.textContentCard.setStrokeWidth(0);
            }
            switch (msg.type) {
                case PrivateMessage.TYPE_TEXT:
                    holder.textContentCard.setVisibility(View.VISIBLE);
                    holder.textContentTv.setText(msg.content.getString("content"));
                    break;
                case PrivateMessage.TYPE_PIC:
                    holder.picMsg.setVisibility(View.VISIBLE);
                    Glide.with(BiliClient.context)
                            .load(msg.content.getString("url"))
                            .into(holder.picMsg);
                    break;
                case PrivateMessage.TYPE_TIP:
                    holder.tipTv.setVisibility(View.VISIBLE);
                    holder.tipTv.setText(msg.name + "撤回了一条消息");
                    break;
                case PrivateMessage.TYPE_VIDEO:
                    holder.videoCard.setVisibility(View.VISIBLE);
                    Glide.with(BiliClient.context)
                            .load(msg.content.getString("thumb"))
                            .into(holder.videoCover);
                    holder.upNameTv.setText(msg.content.getString("author"));
                    holder.videoTitleTv.setText(msg.content.getString("title"));
                    break;
                default:
                    holder.textContentCard.setVisibility(View.VISIBLE);
                    holder.textContentTv.setText("暂时无法显示该消息");
            }
        } catch (JSONException err) {
            Log.e(PrivateMessage.class.getName(), err.toString());
        } 
    }
    @Override 
    public int getItemCount(){
        return mPrivateMsgList.size();
    }
}
