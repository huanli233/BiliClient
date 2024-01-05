package com.RobinNotBad.BiliClient.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgAdapter;
import com.RobinNotBad.BiliClient.model.PrivateMsgContentPreview;
import com.RobinNotBad.BiliClient.model.UserInfo;
import java.util.ArrayList;

public class PrivateMsgUserAdapter extends RecyclerView.Adapter<PrivateMsgUserAdapter.PrivateMsgUserHolder> {

    Context context;
    ArrayList<PrivateMsgContentPreview> contentList;
    ArrayList<UserInfo> userList;

    public PrivateMsgUserAdapter(Context context, ArrayList<PrivateMsgContentPreview> contentList,ArrayList<UserInfo> userList) {
        this.context = context;
        this.contentList = contentList;
        this.userList = userList;
    }

    @Override
    public PrivateMsgUserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_user_list,parent,false);
        return new PrivateMsgUserHolder(view);
    }

    @Override
    public void onBindViewHolder(PrivateMsgUserHolder holder, int position) {

        
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }


    public static class PrivateMsgUserHolder extends RecyclerView.ViewHolder{
        

        public PrivateMsgUserHolder(View itemView) {
            super(itemView);
            
        }

    }
}