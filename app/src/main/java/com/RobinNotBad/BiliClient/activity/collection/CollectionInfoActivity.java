package com.RobinNotBad.BiliClient.activity.collection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.adapter.QualityChooseAdapter;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardHolder;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.Collection;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.Cookies;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.PreInflateHelper;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CollectionInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_simple_list);
        setPageName("合集详情");
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        findViewById(R.id.top).setOnClickListener(view -> finish());

        Collection collection = (Collection) getIntent().getSerializableExtra("collection");
        int season_id = getIntent().getIntExtra("season_id", -1);
        long mid = getIntent().getLongExtra("mid", -1);
        if (collection == null/* && (season_id == -1 || mid == -1)*/) {
            Toast.makeText(this, "合集不存在", Toast.LENGTH_SHORT).show();
            finish();
        }

        Adapter adapter = new Adapter(this, Objects.requireNonNull(collection), recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        Collection collection;
        Context context;
        List<Collection.Section> data;
        List<Integer> types = new ArrayList<>();
        PreInflateHelper preInflateHelper;

        public Adapter(Context context, Collection collection, RecyclerView recyclerView){
            this.context = context;
            this.data = collection.sections;
            this.collection = collection;
            this.preInflateHelper = new PreInflateHelper(context);
            this.preInflateHelper.preload(recyclerView, R.layout.cell_video_list);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return -1;
            return getTypes().get(--position);
        }

        private List<Integer> getTypes() {
            synchronized (this) {
                types.clear();
                for (Collection.Section section : data) {
                    types.add(1);
                    for (int i = 0; i < section.episodes.size(); i++) {
                        types.add(0);
                    }
                }
                return types;
            }
        }

        private int getSectionPos(int pos) {
            List<Integer> list = getTypes();
            int sectionPos = -1;
            for (int i = 0; i <= pos; i++) {
                if (list.get(i) == 1) sectionPos++;
            }
            return sectionPos;
        }

        private int getEpisodePos(int pos) {
            List<Integer> list = getTypes();
            int episodePos = -1;
            for (int i = pos; i >= 0; i--) {
                if (list.get(i) == 1) return episodePos;
                episodePos++;
            }
            return 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == -1) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.cell_collection_info, parent, false);
                return new CollectionInfoHolder(view);
            } else if (viewType == 0) {
                View view = preInflateHelper.getView(parent, R.layout.cell_video_list);
                return new VideoCardHolder(view);
            } else {
                return new SectionHolder(new TextView(context));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof SectionHolder) {
                position--;
                ((SectionHolder) holder).item.setText(data.get(getSectionPos(position)).title);
            } else if (holder instanceof VideoCardHolder) {
                position--;
                VideoCardHolder videoCardHolder = (VideoCardHolder) holder;
                VideoInfo videoInfo = data.get(getSectionPos(position)).episodes.get(getEpisodePos(position)).arc;
                VideoCard videoCard = new VideoCard(videoInfo.title, "", ToolsUtil.toWan(videoInfo.stats.view), videoInfo.cover, videoInfo.aid, videoInfo.bvid);
                videoCardHolder.itemView.setOnClickListener((view) -> context.startActivity(new Intent(context, VideoInfoActivity.class).putExtra("aid", videoInfo.aid).putExtra("bvid", videoInfo.bvid)));
                videoCardHolder.showVideoCard(videoCard, context);
            } else if (holder instanceof CollectionInfoHolder) {
                CollectionInfoHolder collectionInfoHolder = (CollectionInfoHolder) holder;
                collectionInfoHolder.name.setText(collection.title);
                collectionInfoHolder.desc.setText(TextUtils.isEmpty(collection.intro) ? "这里没有简介哦" : collection.intro);
                Glide.with(context).asDrawable().load(GlideUtil.url(collection.cover))
                        .placeholder(R.mipmap.placeholder)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5,context))).sizeMultiplier(0.85f).dontAnimate())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(collectionInfoHolder.cover);
                collectionInfoHolder.cover.setOnClickListener(view -> {
                       context.startActivity(new Intent(context, ImageViewerActivity.class).putExtra("imageList", new ArrayList<>(Collections.singletonList(collection.cover))));
                   });
            }
        }

        @Override
        public int getItemCount() {
            int count = 0;
            for (Collection.Section section : data) {
                count++;
                count += section.episodes.size();
            }
            return ++count;
        }

        static class SectionHolder extends RecyclerView.ViewHolder {
            private final TextView item;
            public SectionHolder(@NonNull TextView itemView) {
                super(itemView);
                this.item = itemView;
            }
        }

        static class CollectionInfoHolder extends RecyclerView.ViewHolder {
            TextView name, desc;
            ImageView cover;
            public CollectionInfoHolder(@NonNull View itemView) {
                super(itemView);
                this.name = itemView.findViewById(R.id.name);
                this.desc = itemView.findViewById(R.id.desc);
                this.cover = itemView.findViewById(R.id.cover);
            }
        }
    }
}