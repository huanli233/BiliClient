package com.RobinNotBad.BiliClient.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgAdapter;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;

public class PrivateMsgAdapter extends RecyclerView.Adapter<PrivateMsgAdapter.ViewHolder>{
    private ArrayList<PrivateMessage> mPrivateMsgList;
    
    TextView nameTv,textContentTv;
    MaterialCardView textCard,videoCard;
    
    
    
    static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View view){
            super(view);
        }
    }
    public PrivateMsgAdapter(ArrayList<PrivateMessage> msgList){
        mPrivateMsgList=msgList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_private_msg,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        PrivateMessage msg = mPrivateMsgList.get(position);
    }
    @Override 
    public int getItemCount(){
        return mPrivateMsgList.size();
    }
}
