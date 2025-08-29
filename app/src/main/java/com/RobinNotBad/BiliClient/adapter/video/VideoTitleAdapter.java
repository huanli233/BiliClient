package com.RobinNotBad.BiliClient.adapter.video;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;

import java.util.List;

public class VideoTitleAdapter extends RecyclerView.Adapter<VideoTitleAdapter.ViewHolder> {

    private final Context context;
    private final List<String> videoTitles;

    public VideoTitleAdapter(Context context, List<String> videoTitles) {
        this.context = context;
        this.videoTitles = videoTitles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = videoTitles.get(position);
        holder.title.setText(title);
    }

    @Override
    public int getItemCount() {
        return videoTitles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
        }
    }
}
