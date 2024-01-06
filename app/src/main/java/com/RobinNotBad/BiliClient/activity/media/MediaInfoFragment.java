package com.RobinNotBad.BiliClient.activity.media;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerActivity;
import com.RobinNotBad.BiliClient.activity.video.JumpToPlayerActivity;
import com.RobinNotBad.BiliClient.adapter.MediaEpisodesAdapter;
import com.RobinNotBad.BiliClient.api.BilibiliIDConverter;
import com.RobinNotBad.BiliClient.api.bangumi_to_card;
import com.RobinNotBad.BiliClient.databinding.FragmentMediaInfoBinding;
import com.RobinNotBad.BiliClient.model.Media;
import com.RobinNotBad.BiliClient.model.MediaSectionInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Objects;

public class MediaInfoFragment extends Fragment {
    private String mediaId;
    private FragmentMediaInfoBinding binding;
    private int selectedSectionIndex = 0;
    private MediaSectionInfo sectionInfo;
    private Dialog dialog;

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
        binding = FragmentMediaInfoBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //拉数据
        bangumi_to_card.getMediaInfo(mediaId, new NetWorkUtil.Callback<Media>() {
            @Override
            public void onSuccess(Media baseMediaInfo) {
                getSectionInfo(baseMediaInfo);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "获取番剧信息失败\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setSelectSectionIndex(int index) {
        MediaSectionInfo.SectionInfo section = index == 0 ? sectionInfo.mainSection : sectionInfo.sections[index - 1];
        binding.btnEpisode.setText(section.title + " 点击切换");
        MediaEpisodesAdapter adapter = (MediaEpisodesAdapter) binding.rvEposideList.getAdapter();
        if (adapter != null) {
            (Objects.requireNonNull(adapter)).setData(section.episodes);
            binding.rvEposideList.scrollToPosition(0);
            adapter.notifyDataSetChanged();
        }
        selectedSectionIndex = index;
    }

    private void initView(Media baseMediaInfo, MediaSectionInfo mediaSectionInfo) {
        //init data.
        selectedSectionIndex = 0;
        sectionInfo = mediaSectionInfo;
        Glide.with(this)
                .load(baseMediaInfo.horizontalCover)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.imageMediaCover);
        binding.title.setText(baseMediaInfo.title);
        //section selector setting.
        RecyclerView rv = binding.rvEposideList;
        MediaEpisodesAdapter adapter = new MediaEpisodesAdapter();
        binding.btnEpisode.setOnClickListener(v -> getSectionChooseDialog(mediaSectionInfo).show());
        rv.setAdapter(adapter);
        adapter.setData(mediaSectionInfo.mainSection.episodes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        //play button setting
        binding.btnPlay.setOnClickListener(v -> {
            MediaSectionInfo.SectionInfo section = selectedSectionIndex == 0 ? mediaSectionInfo.mainSection : mediaSectionInfo.sections[selectedSectionIndex - 1];
            MediaSectionInfo.EpisodeInfo episodeInfo = section.episodes[adapter.getSelectedItemIndex()];
            Glide.get(requireContext()).clearMemory();
            Intent intent = new Intent(v.getContext(), JumpToPlayerActivity.class);
            intent.putExtra("cid", (int)episodeInfo.cid);
            intent.putExtra("title", episodeInfo.longTitle);
            intent.putExtra("bvid", BilibiliIDConverter.aidtobv(episodeInfo.aid));
            intent.putExtra("aid", episodeInfo.aid);
            startActivity(intent);
        });
        binding.btnPlay.setOnLongClickListener(v -> {
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

    private void getSectionInfo(Media baseMediaInfo) {
        CenterThreadPool.run(() -> {
            try {
                MediaSectionInfo mediaSectionInfo = bangumi_to_card.getSectionInfo(String.valueOf(baseMediaInfo.seasonId));
                this.requireActivity().runOnUiThread(() -> initView(baseMediaInfo, mediaSectionInfo));

            } catch (Exception e) {
                Toast.makeText(requireContext(), "解析剧集详细信息失败\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
