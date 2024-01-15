package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.util.Consumer;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.MediaSectionInfo;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class MediaEpisodesAdapter extends RecyclerView.Adapter<MediaEpisodesAdapter.MediaEpisodesViewHolder> {
    private List<MediaSectionInfo.EpisodeInfo> data;

    private Consumer<MediaSectionInfo.EpisodeInfo> onItemClickListener;
    private int selectedItemIndex = 0;

    public void setOnItemClickListener(Consumer<MediaSectionInfo.EpisodeInfo> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public int getSelectedItemIndex() {
        return selectedItemIndex;
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        // Cancel previous selected item and set current item
        int previousSelectedIndex = this.selectedItemIndex;
        this.selectedItemIndex = selectedItemIndex;
        notifyItemChanged(previousSelectedIndex);
        notifyItemChanged(selectedItemIndex);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(MediaSectionInfo.EpisodeInfo[] data) {
        this.data = Arrays.asList(data);
        selectedItemIndex = 0;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MediaEpisodesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_button_only_text, parent, false);
        return new MediaEpisodesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaEpisodesViewHolder holder, int position) {
        MediaSectionInfo.EpisodeInfo episodeInfo = data.get(position);
        if(onItemClickListener != null){
            holder.outerClickListener = () -> onItemClickListener.accept(episodeInfo);
        }
        holder.bind(position, selectedItemIndex == position);
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size(): 0;
    }

    public class MediaEpisodesViewHolder extends RecyclerView.ViewHolder {

        private Runnable outerClickListener;
        private final MaterialButton button;


        public MediaEpisodesViewHolder(View view) {
            super(view);
            button = itemView.findViewById(R.id.btn);
        }

        void bind(int currentIndex, boolean isSelected) {
            button.setText(String.valueOf(currentIndex + 1));
            if (isSelected) {
                button.setTextColor(0x78242424);
                ViewCompat.setBackgroundTintList(button, AppCompatResources.getColorStateList(itemView.getContext(), R.color.background_button_selected));
                button.setOnClickListener(null);
            } else {
                button.setTextColor(0xFFFFFFFF);
                ViewCompat.setBackgroundTintList(button, AppCompatResources.getColorStateList(itemView.getContext(), R.color.background_button));
                button.setOnClickListener(v -> {
                    setSelectedItemIndex(currentIndex);
                    if (outerClickListener != null) {
                        outerClickListener.run();
                    }
                });
            }
        }
    }
}


