package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.CopyTextActivity;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.api.PrivateMsgApi;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrivateMsgAdapter extends RecyclerView.Adapter<PrivateMsgAdapter.ViewHolder>{
    private final List<PrivateMessage> mPrivateMsgList;
    private long selfUid = -1;
    private final JSONArray emoteArray;
    private final Context context;

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
    public PrivateMsgAdapter(List<PrivateMessage> msgList,JSONArray emoteArray,Context context){
        this.mPrivateMsgList=msgList;
        this.context = context;
        this.emoteArray = emoteArray;
    }
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_private_msg,parent,false);
        return new ViewHolder(view);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        PrivateMessage msg = mPrivateMsgList.get(position);
        try {
            holder.nameTv.setText(msg.name);
            if(selfUid ==-1) {
            	selfUid=SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,-1);
            }
            if(msg.uid==selfUid) {
            	holder.root.setGravity(Gravity.END);
                holder.textContentCard.setCardBackgroundColor(context.getResources().getColor(R.color.pink));
                holder.textContentCard.setStrokeWidth(0);
            }else{
                holder.root.setGravity(Gravity.START);
                holder.textContentCard.setCardBackgroundColor(Color.parseColor("#78242424"));
                holder.textContentCard.setStrokeWidth(1);
            }
            
            switch (msg.type) {
                case PrivateMessage.TYPE_TEXT:
                    holder.tipTv.setVisibility(View.GONE);
                    holder.picMsg.setVisibility(View.GONE);
                    holder.nameTv.setVisibility(View.VISIBLE);
                    holder.videoCard.setVisibility(View.GONE);
                    holder.textContentCard.setVisibility(View.VISIBLE);
                    Log.e("",emoteArray.toString());
                    CenterThreadPool.run(()->{
                        try {
                            SpannableString contentWithEmote = PrivateMsgApi.textReplaceEmote(msg.content.getString("content"),emoteArray,1f,context);
                            ((Activity)context).runOnUiThread(()-> holder.textContentTv.setText(contentWithEmote));
                        } catch(Exception err) {
                            Log.e("",err.toString());
                            ((Activity)context).runOnUiThread(()->{
                                    try {
                                    	holder.textContentTv.setText(msg.content.getString("content"));
                                    } catch(JSONException e) {
                                    	Log.e("",e.toString());
                                    }
                            });
                        }
                    });
                    break;
                case PrivateMessage.TYPE_PIC:
                    holder.picMsg.setVisibility(View.VISIBLE);
                    holder.tipTv.setVisibility(View.GONE);
                    holder.nameTv.setVisibility(View.VISIBLE);
                    holder.textContentCard.setVisibility(View.GONE);
                    holder.videoCard.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(msg.content.getString("url") + "@25q.webp")
                            .override(Target.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(holder.picMsg);
                    holder.picMsg.setOnClickListener(view->{
                        ArrayList<String> imageList = new ArrayList<>();
                        try{
                            imageList.add(msg.content.getString("url"));
                        }catch(JSONException e){
                            Log.e("",e.toString());
                        }
                        Intent intent = new Intent(context,ImageViewerActivity.class);
                        intent.putStringArrayListExtra("imageList",imageList);
                        context.startActivity(intent);
                    });
                    break;
                case PrivateMessage.TYPE_RETRACT:
                    holder.tipTv.setVisibility(View.VISIBLE);
                    holder.nameTv.setVisibility(View.GONE);
                    holder.picMsg.setVisibility(View.GONE);
                    holder.videoCard.setVisibility(View.GONE);
                    holder.textContentCard.setVisibility(View.GONE);
                    holder.tipTv.setText(msg.name + "撤回了一条消息");
                    break;
                case PrivateMessage.TYPE_VIDEO:
                    holder.videoCard.setVisibility(View.VISIBLE);
                    holder.nameTv.setVisibility(View.VISIBLE);
                    holder.picMsg.setVisibility(View.GONE);
                    holder.textContentCard.setVisibility(View.GONE);
                    holder.tipTv.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(msg.content.getString("thumb"))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(holder.videoCover);
                    holder.upNameTv.setText(msg.content.getString("author"));
                    holder.videoTitleTv.setText(msg.content.getString("title"));
                    holder.videoCard.setOnClickListener(view-> CenterThreadPool.run(()->{
                        try {
                            long aid = msg.content.getLong("id");
                            String bvid = VideoInfoApi.getJsonByAid(aid).getString("bvid");
                            Intent intent = new Intent(context,VideoInfoActivity.class);
                            intent.putExtra("aid",aid);
                            intent.putExtra("bvid",bvid);
                            intent.putExtra("type","video");
                            context.startActivity(intent);
                    } catch(IOException err) {
                        Log.e("",err.toString());
                    } catch(JSONException err){
                        Log.e("",err.toString());
                    }
                    }));
                    break;
                default:
                    holder.textContentCard.setVisibility(View.VISIBLE);
                    holder.textContentTv.setText("暂时无法显示该消息");
                    holder.tipTv.setVisibility(View.GONE);
                    holder.picMsg.setVisibility(View.GONE);
                    holder.nameTv.setVisibility(View.VISIBLE);
                    holder.videoCard.setVisibility(View.GONE);
            }

            holder.textContentCard.setOnLongClickListener(view->{
                try {
                	Intent intent = new Intent(context,CopyTextActivity.class);
                    intent.putExtra("content",msg.content.getString("content"));
                    context.startActivity(intent);
                  
                } catch(Exception err) {
                	err.printStackTrace();
                }    
                 return false; 
            });
        } catch (JSONException err) {
            Log.e(PrivateMessage.class.getName(), err.toString());
        } 
    }
    public void addItem(ArrayList<PrivateMessage> list) {
    	mPrivateMsgList.addAll(0,list);
        this.notifyItemRangeInserted(0,list.size());
    }
    @Override 
    public int getItemCount(){
        return mPrivateMsgList.size();
    }
}
