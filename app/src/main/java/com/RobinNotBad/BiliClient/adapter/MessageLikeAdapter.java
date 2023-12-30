package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.MessageLikeInfo;

import java.util.ArrayList;

public class MessageLikeAdapter extends RecyclerView.Adapter<MessageLikeHolder> {
    Context context;
    ArrayList<MessageLikeInfo> messageList;

    public MessageLikeAdapter(Context context, ArrayList<MessageLikeInfo> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageLikeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_message,parent,false);
        return new MessageLikeHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MessageLikeHolder holder, int position) {
        holder.showMessage(messageList.get(position),context);
    }

    @Override
    public void onViewRecycled(@NonNull MessageLikeHolder holder) {
        holder.extraCard.removeAllViews();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
