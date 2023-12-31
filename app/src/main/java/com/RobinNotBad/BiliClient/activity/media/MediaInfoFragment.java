package com.RobinNotBad.BiliClient.activity.media;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.RobinNotBad.BiliClient.api.bangumi_to_card;
import com.RobinNotBad.BiliClient.databinding.FragmentMediaInfoBinding;
import com.RobinNotBad.BiliClient.model.Media;
import com.RobinNotBad.BiliClient.model.MediaSectionInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class MediaInfoFragment extends Fragment {
    private String mediaId;
    private FragmentMediaInfoBinding binding;
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
        bangumi_to_card.getMediaInfo(requireActivity(), mediaId, new NetWorkUtil.Callback<Media>() {
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

    private void initView(Media baseMediaInfo, MediaSectionInfo mediaSectionInfo){
        Glide.with(this)
                .load(baseMediaInfo.cover)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.imageMediaCover);
    }
    private void getSectionInfo(Media baseMediaInfo){
        CenterThreadPool.run(() -> {
            try {
                MediaSectionInfo mediaSectionInfo= bangumi_to_card.getSectionInfo(String.valueOf(baseMediaInfo.seasonId));
                this.requireActivity().runOnUiThread(() -> {
                    initView(baseMediaInfo, mediaSectionInfo);
                });

            }catch (Exception e){
                Toast.makeText(requireContext(), "解析剧集详细信息失败\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static MediaInfoFragment newInstance(long mediaId) {
        Bundle args = new Bundle();
        args.putString("media_id", String.valueOf(mediaId));
        MediaInfoFragment fragment = new MediaInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
