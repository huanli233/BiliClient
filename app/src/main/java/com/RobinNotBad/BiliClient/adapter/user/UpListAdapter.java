package com.RobinNotBad.BiliClient.adapter.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.UserInfo;

import java.util.ArrayList;

//视频详情页的联合投稿列表
//尝试用了extend，两边代码基本完全一样只是布局差别而已
//2024-06-13

public class UpListAdapter extends UserListAdapter {

    public UpListAdapter(Context context, ArrayList<UserInfo> userList) {
        super(context, userList);
    }

    @NonNull
    @Override
    public UserListAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_up_list,parent,false);
        return new Holder(view);
    }

}
