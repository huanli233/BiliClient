package com.RobinNotBad.BiliClient.activity.user.info;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.activity.base.RefreshListFragment;
import com.RobinNotBad.BiliClient.adapter.UserInfoAdapter;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;

//用户动态
//2023-09-30
//2024-05-03

public class UserDynamicFragment extends RefreshListFragment {

    private long mid;
    private ArrayList<Dynamic> dynamicList;
    private UserInfoAdapter adapter;
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

        CenterThreadPool.run(()->{
            try {
                UserInfo userInfo = UserInfoApi.getUserInfo(mid);
                Log.e("debug","获取到用户信息");

                try {
                    offset = DynamicApi.getDynamicList(dynamicList, offset, mid);
                    bottom = (offset == -1);
                    Log.e("debug", "获取到用户动态");
                }catch (Exception e){loadFail(e);}

                if(isAdded()){
                    adapter = new UserInfoAdapter(requireContext(), dynamicList, userInfo);
                    setAdapter(adapter);
                    setRefreshing(false);
                }
            } catch (Exception e){loadFail(e);}
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading() {
        CenterThreadPool.run(()->{
            try {
                int lastSize = dynamicList.size();
                offset = DynamicApi.getDynamicList(dynamicList,offset,mid);
                runOnUiThread(()-> adapter.notifyItemRangeInserted(lastSize + 1, dynamicList.size() + 1 - lastSize));
                bottom = (offset==-1);
                setRefreshing(false);
            } catch (Exception e){loadFail(e);}
        });
    }
}