package com.RobinNotBad.BiliClient.adapter.video;

import androidx.annotation.NonNull;
import android.content.Context;
import com.RobinNotBad.BiliClient.activity.collection.CollectionInfoActivity;
import java.util.List;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardHolder;
import com.RobinNotBad.BiliClient.model.Collection;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.R;

public class SeasonCardAdapter extends RecyclerView.Adapter<VideoCardHolder>{
    Context context;
    List<Collection> seasonList;

    public SeasonCardAdapter(Context context, List<Collection> seasonList) {
        this.context = context;
        this.seasonList = seasonList;
    }

    @NonNull
    @Override
    public VideoCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list,parent,false);
        return new VideoCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoCardHolder holder, int position) {
        VideoCard videoCard = new VideoCard("","","","",0,"",seasonList.get(position));
        holder.showVideoCard(videoCard,context);    //此函数在VideoCardHolder里

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context,CollectionInfoActivity.class);
            intent.putExtra("collection",seasonList.get(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return seasonList.size();
    }
}
