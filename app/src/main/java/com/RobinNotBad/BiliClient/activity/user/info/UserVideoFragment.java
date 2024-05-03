package com.RobinNotBad.BiliClient.activity.user.info;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.activity.base.RefreshListFragment;
import com.RobinNotBad.BiliClient.adapter.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;

//用户视频
//2023-09-30
//2024-05-03

public class UserVideoFragment extends RefreshListFragment {

    private long mid;
    private ArrayList<VideoCard> videoList;
    private VideoCardAdapter adapter;

    public UserVideoFragment() {

    }

    public static UserVideoFragment newInstance(long mid) {
        UserVideoFragment fragment = new UserVideoFragment();
        Bundle args = new Bundle();
        args.putLong("mid", mid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mid = getArguments().getLong("mid");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoList = new ArrayList<>();
        setOnLoadMoreListener(this::continueLoading);

        CenterThreadPool.run(()->{
            try {
                bottom = (UserInfoApi.getUserVideos(mid,page,"",videoList) == 1);
                if(isAdded()) {
                    setRefreshing(false);
                    adapter = new VideoCardAdapter(requireContext(), videoList);
                    setAdapter(adapter);
                }
            } catch (Exception e){loadFail(e);}
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading(int page) {
        CenterThreadPool.run(()->{
            try {
                int lastSize = videoList.size();
                int result = UserInfoApi.getUserVideos(mid,page,"",videoList);
                if(result != -1){
                    Log.e("debug","下一页");
                    runOnUiThread(()-> adapter.notifyItemRangeInserted(lastSize, videoList.size() - lastSize));
                    if(result == 1) {
                        Log.e("debug","到底了");
                        bottom = true;
                    }
                }
                setRefreshing(false);
            } catch (Exception e){loadFail(e);}
        });
    }
}