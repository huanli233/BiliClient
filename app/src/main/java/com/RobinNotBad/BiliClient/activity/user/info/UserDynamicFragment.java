package com.RobinNotBad.BiliClient.activity.user.info;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.activity.base.RefreshListFragment;
import com.RobinNotBad.BiliClient.adapter.dynamic.DynamicHolder;
import com.RobinNotBad.BiliClient.adapter.dynamic.UserDynamicAdapter;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

//用户动态
//2023-09-30
//2024-05-03

public class UserDynamicFragment extends RefreshListFragment {

    private long mid;
    private ArrayList<Dynamic> dynamicList;
    private UserDynamicAdapter adapter;
    private long offset = 0;

    public UserDynamicFragment() {

    }

    public static UserDynamicFragment newInstance(long mid) {
        UserDynamicFragment fragment = new UserDynamicFragment();
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

        dynamicList = new ArrayList<>();
        setOnLoadMoreListener(page -> continueLoading());

        CenterThreadPool.run(() -> {
            try {
                UserInfo userInfo = UserInfoApi.getUserInfo(mid);
                if (userInfo == null) {
                    runOnUiThread(() -> {
                        MsgUtil.showMsg("用户不存在", requireContext());
                        requireActivity().finish();
                    });
                    return;
                }
                Log.e("debug", "获取到用户信息");

                try {
                    offset = DynamicApi.getDynamicList(dynamicList, offset, mid, null);
                    bottom = (offset == -1);
                    Log.e("debug", "获取到用户动态");
                } catch (Exception e) {
                    loadFail(e);
                }

                if (isAdded()) {
                    adapter = new UserDynamicAdapter(requireContext(), dynamicList, userInfo);
                    setAdapter(adapter);
                    setRefreshing(false);
                }
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading() {
        CenterThreadPool.run(() -> {
            try {
                List<Dynamic> list = new ArrayList<>();
                offset = DynamicApi.getDynamicList(list, offset, mid, null);
                runOnUiThread(() -> {
                    dynamicList.addAll(list);
                    adapter.notifyItemRangeInserted(dynamicList.size() - list.size() + 1, list.size());
                });
                bottom = (offset == -1);
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    public void onDynamicRemove(int position) {
        try {
            DynamicHolder.removeDynamicFromList(dynamicList, position, adapter);
        } catch (Throwable ignored) {
        }
    }
}