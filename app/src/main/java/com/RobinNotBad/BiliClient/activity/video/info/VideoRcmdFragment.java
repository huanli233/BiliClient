package com.RobinNotBad.BiliClient.activity.video.info;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.activity.base.RefreshListFragment;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.RecommendApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;

//视频下推荐页面
//2023-11-26

public class VideoRcmdFragment extends RefreshListFragment {

    private long aid;

    public VideoRcmdFragment() {}

    public static VideoRcmdFragment newInstance(long aid) {
        VideoRcmdFragment fragment = new VideoRcmdFragment();
        Bundle args = new Bundle();
        args.putLong("aid", aid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            aid = getArguments().getLong("aid");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.e("debug-av号", String.valueOf(aid));

        CenterThreadPool.supplyAsyncWithLiveData(() -> RecommendApi.getRelated(aid)
        ).observe(getViewLifecycleOwner(), (ArrayList<VideoCard> videoList) -> {
            try {
                VideoCardAdapter adapter = new VideoCardAdapter(requireContext(), videoList);
                setAdapter(adapter);
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

}