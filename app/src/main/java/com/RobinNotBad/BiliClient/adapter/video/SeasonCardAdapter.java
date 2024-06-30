package com.RobinNotBad.BiliClient.adapter.video;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.collection.CollectionInfoActivity;
import com.RobinNotBad.BiliClient.model.Collection;
import com.RobinNotBad.BiliClient.model.VideoCard;

import java.util.List;

public class SeasonCardAdapter extends RecyclerView.Adapter<VideoCardHolder> {
    final Context context;
    final List<Collection> seasonList;

    public SeasonCardAdapter(Context context, List<Collection> seasonList) {
        this.context = context;
        this.seasonList = seasonList;
    }

    @NonNull
    @Override
    public VideoCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list, parent, false);
        return new VideoCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoCardHolder holder, int position) {
        VideoCard videoCard = new VideoCard("", "", "", "", 0, "", seasonList.get(position));
        holder.showVideoCard(videoCard, context);    //此函数在VideoCardHolder里

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, CollectionInfoActivity.class);
            intent.putExtra("collection", seasonList.get(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return seasonList.size();
    }
}
