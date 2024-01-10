package com.RobinNotBad.BiliClient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.util.Consumer;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.databinding.CellButtonOnlyTextBinding;
import com.RobinNotBad.BiliClient.model.MediaSectionInfo;

public class MediaEpisodesAdapter extends RecyclerView.Adapter<MediaEpisodesAdapter.MediaEpisodesViewHolder> {
    private MediaSectionInfo.EpisodeInfo[] data;

    private Consumer<MediaSectionInfo.EpisodeInfo> onItemClickListener;

    public void setOnItemClickListener(Consumer<MediaSectionInfo.EpisodeInfo> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private int selectedItemIndex = 0;

    public int getSelectedItemIndex() {
        return selectedItemIndex;
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        //cancel previous selected item.
        //and set current item.
        //record previous.
        int previousIndex = this.selectedItemIndex;
        //set current.
        this.selectedItemIndex = selectedItemIndex;
        //notify.
        notifyItemChanged(previousIndex);
        notifyItemChanged(selectedItemIndex);
    }

    public void setData(MediaSectionInfo.EpisodeInfo[] data) {
        this.data = data;
        selectedItemIndex = 0;
    }

    @NonNull
    @Override
    public MediaEpisodesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new MediaEpisodesViewHolder(CellButtonOnlyTextBinding
                .inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MediaEpisodesViewHolder holder, int position) {
        MediaSectionInfo.EpisodeInfo episodeInfo = data[position];
        holder.setOnClickListener(() -> {
            if (onItemClickListener != null) {
                onItemClickListener.accept(episodeInfo);
            }
        });
        holder.bind(position, selectedItemIndex == position);
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public class MediaEpisodesViewHolder extends RecyclerView.ViewHolder {

        private Runnable outerClickListener;

        CellButtonOnlyTextBinding binding;

        MediaEpisodesViewHolder(View view) {
            super(view);
        }

        MediaEpisodesViewHolder(CellButtonOnlyTextBinding binding) {
            this(binding.getRoot());
            this.binding = binding;
        }

        void bind(int currentIndex, boolean isSelected) {
            binding.btn.setText(String.valueOf(currentIndex + 1));
            if (isSelected) {
                binding.btn.setTextColor(0x78242424);
                ViewCompat.setBackgroundTintList(binding.btn, AppCompatResources.getColorStateList(binding.getRoot().getContext(), R.color.background_button_selected));
                binding.btn.setOnClickListener(null);
            } else {
                binding.btn.setTextColor(0xFFFFFFFF);
                ViewCompat.setBackgroundTintList(binding.btn, AppCompatResources.getColorStateList(binding.getRoot().getContext(), R.color.background_button));
                binding.btn.setOnClickListener(v -> {
                    setSelectedItemIndex(currentIndex);
                    if (outerClickListener != null) {
                        outerClickListener.run();
                    }
                });
            }
        }
        void setOnClickListener(Runnable runnable){
            outerClickListener = runnable;
        }
    }
}


