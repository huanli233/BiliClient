package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.Announcement;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

//公告
//2024-02-23

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.Holder> {

    final Context context;
    final ArrayList<Announcement> list;

    public AnnouncementAdapter(Context context, ArrayList<Announcement> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_announcement_list, parent, false);
        return new Holder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Announcement announcement = list.get(position);

        holder.name.setText(announcement.title);
        int extra_start = announcement.content.indexOf("<extra_insert>");
        holder.content.setText(extra_start == -1 ? announcement.content : announcement.content.substring(0, extra_start) + "[附加内容]");
        holder.info.setText("ID:" + announcement.id + " | " + announcement.ctime);

        holder.cardView.setOnClickListener(view -> MsgUtil.showText(announcement.title, announcement.content));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView content;
        final TextView info;
        final MaterialCardView cardView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            name = itemView.findViewById(R.id.name);
            content = itemView.findViewById(R.id.content);
            info = itemView.findViewById(R.id.info);
        }
    }
}
