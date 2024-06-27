package com.RobinNotBad.BiliClient.activity.video.info;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.RecommendApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;

//视频下推荐页面
//2023-11-26

public class VideoRcmdFragment extends Fragment {

    private long aid;
    private RecyclerView recyclerView;

    public VideoRcmdFragment() {

    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);

        Log.e("debug-av号", String.valueOf(aid));


        CenterThreadPool.run(() -> {
            try {

                ArrayList<VideoCard> videoList = RecommendApi.getRelated(aid);
                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    VideoCardAdapter adapter = new VideoCardAdapter(requireActivity(), videoList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
                    recyclerView.setAdapter(adapter);
                });

            } catch (Exception e) {
                if (isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e, getContext()));
            }
        });
    }

}