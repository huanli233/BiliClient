package com.RobinNotBad.BiliClient.activity.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerActivity;
import com.RobinNotBad.BiliClient.activity.video.JumpToPlayerActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.adapter.MediaEpisodesAdapter;
import com.RobinNotBad.BiliClient.api.bangumi_to_card;
import com.RobinNotBad.BiliClient.model.Media;
import com.RobinNotBad.BiliClient.model.MediaSectionInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class MediaInfoFragment extends Fragment {
    private String mediaId;
    private int selectedSectionIndex = 0;
    private MediaSectionInfo sectionInfo;
    private Dialog dialog;
    private View rootView;

    public static MediaInfoFragment newInstance(long mediaId) {
        Bundle args = new Bundle();
        args.putString("media_id", String.valueOf(mediaId));
        MediaInfoFragment fragment = new MediaInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mediaId = arguments.getString("media_id");
        }
        rootView = inflater.inflate(R.layout.fragment_media_info, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //拉数据
        LiveData<Pair<Media, MediaSectionInfo>> pairLiveData = CenterThreadPool.supplyAsync(() -> {
            try {
                Media mediaInfo = bangumi_to_card.getMediaInfo(mediaId);
                MediaSectionInfo sectionInfo = bangumi_to_card.getSectionInfo(String.valueOf(mediaInfo.seasonId));
                return new Pair<>(mediaInfo, sectionInfo);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "解析数据失败: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
        });
        pairLiveData.observe(getViewLifecycleOwner(), pair -> {
            if (pair != null) {
                initView(pair.first, pair.second);
                Activity activity = requireActivity();
                if(activity instanceof VideoInfoActivity){
                    ((VideoInfoActivity) activity).setCurrentEpisodeInfo(pair.second.mainSection.episodes[0]);
                }
            }
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void setSelectSectionIndex(int index) {
        TextView episodeButton = rootView.findViewById(R.id.btn_episode);
        RecyclerView eposideListRecyclerView = rootView.findViewById(R.id.rv_eposide_list);
        MediaSectionInfo.SectionInfo section = index == 0 ? sectionInfo.mainSection : sectionInfo.sections[index - 1];
        episodeButton.setText(section.title + " 点击切换");
        MediaEpisodesAdapter adapter = (MediaEpisodesAdapter) eposideListRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.setData(section.episodes);
            eposideListRecyclerView.scrollToPosition(0);
        }
        selectedSectionIndex = index;
    }

    private void initView(Media baseMediaInfo, MediaSectionInfo mediaSectionInfo) {
        //init data.
        ImageView imageMediaCover = rootView.findViewById(R.id.image_media_cover);
        TextView title = rootView.findViewById(R.id.title);
        RecyclerView rv = rootView.findViewById(R.id.rv_eposide_list);
        Button episodeButton = rootView.findViewById(R.id.btn_episode);
        Button playButton = rootView.findViewById(R.id.btn_play);
        selectedSectionIndex = 0;
        sectionInfo = mediaSectionInfo;
        Glide.with(this)
                .load(baseMediaInfo.horizontalCover)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.loading_2233)
                .into(imageMediaCover);
        title.setText(baseMediaInfo.title);
        //section selector setting.
        MediaEpisodesAdapter adapter = new MediaEpisodesAdapter();
        adapter.setOnItemClickListener(episodeInfo -> {
            Activity activity = requireActivity();
            if(activity instanceof VideoInfoActivity){
                ((VideoInfoActivity) activity).setCurrentEpisodeInfo(episodeInfo);
            }
        });
        episodeButton.setOnClickListener(v -> getSectionChooseDialog(mediaSectionInfo).show());
        adapter.setData(mediaSectionInfo.mainSection.episodes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);
        //play button setting
        playButton.setOnClickListener(v -> {
            MediaSectionInfo.SectionInfo section = selectedSectionIndex == 0 ? mediaSectionInfo.mainSection : mediaSectionInfo.sections[selectedSectionIndex - 1];
            MediaSectionInfo.EpisodeInfo episodeInfo = section.episodes[adapter.getSelectedItemIndex()];
            Glide.get(requireContext()).clearMemory();
            Intent intent = new Intent(v.getContext(), JumpToPlayerActivity.class);
            intent.putExtra("cid", episodeInfo.cid);
            intent.putExtra("title", episodeInfo.longTitle);
            intent.putExtra("aid", episodeInfo.aid);
            intent.putExtra("html5",false);
            startActivity(intent);
        });
        playButton.setOnLongClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SettingPlayerActivity.class);
            startActivity(intent);
            return true;
        });
    }

    private Dialog getSectionChooseDialog(MediaSectionInfo mediaSectionInfo) {
        if (dialog == null) {
            String[] choices = new String[mediaSectionInfo.sections.length + 1];
            choices[0] = mediaSectionInfo.mainSection.title;
            for (int i = 0; i < mediaSectionInfo.sections.length; i++) {
                choices[i + 1] = mediaSectionInfo.sections[i].title;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setSingleChoiceItems(choices, selectedSectionIndex, (dialog, which) -> {
                setSelectSectionIndex(which);
                dialog.dismiss();
            });
            dialog = builder.create();
        }
        return dialog;
    }
}
