package com.RobinNotBad.BiliClient.activity.user.info;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import com.RobinNotBad.BiliClient.activity.base.RefreshListFragment;
import com.RobinNotBad.BiliClient.adapter.video.SeasonCardAdapter;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.Collection;
import java.util.ArrayList;
import android.os.Bundle;
import android.view.View;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import java.util.List;
import android.util.Log;

public class UserSeasonFragment extends RefreshListFragment {
    private long mid;
    private ArrayList<Collection> seasonList;
    private SeasonCardAdapter adapter;

    public UserSeasonFragment() {

    }

    public static UserSeasonFragment newInstance(long mid) {
        UserSeasonFragment fragment = new UserSeasonFragment();
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

        seasonList = new ArrayList<>();
        setOnLoadMoreListener(this::continueLoading);

        CenterThreadPool.run(()->{
            try {
                bottom = (UserInfoApi.getUserSeasons(mid,page,seasonList) == 1);
                if(isAdded()) {
                    setRefreshing(false);
                    adapter = new SeasonCardAdapter(requireContext(), seasonList);
                    setAdapter(adapter);
                }
            } catch (Exception e){loadFail(e);}
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading(int page) {
        CenterThreadPool.run(()->{
            try {
                List<Collection> list = new ArrayList<>();
                int result = UserInfoApi.getUserSeasons(mid,page,list);
                if(result != -1){
                    Log.e("debug","下一页");
                    runOnUiThread(()-> {
                        seasonList.addAll(list);
                        adapter.notifyItemRangeInserted(seasonList.size() - list.size(), list.size());
                    });
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
