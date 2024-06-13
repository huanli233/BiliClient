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
import com.RobinNotBad.BiliClient.model.SettingSection;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

//设置项
//2024-06-06

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.Holder> {

    Context context;
    ArrayList<SettingSection> list;

    public SettingsAdapter(Context context, ArrayList<SettingSection> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_announcement_list,parent,false);
        return new Holder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SettingSection settingSection = list.get(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder{
        TextView name,content,info;
        MaterialCardView cardView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            name = itemView.findViewById(R.id.name);
            content = itemView.findViewById(R.id.content);
            info = itemView.findViewById(R.id.info);
        }
    }
}
