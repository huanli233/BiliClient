package com.RobinNotBad.BiliClient.adapter.message;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.MessageCard;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeHolder> {
    Context context;
    List<MessageCard> messageList;

    public NoticeAdapter(Context context, List<MessageCard> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public NoticeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_message, parent, false);
        return new NoticeHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull NoticeHolder holder, int position) {
        holder.showMessage(messageList.get(position), context);
    }

    @Override
    public void onViewRecycled(@NonNull NoticeHolder holder) {
        holder.extraCard.removeAllViews();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
