package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.adapter.user.UserListAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class SearchUserFragment extends SearchFragment {

    private List<UserInfo> userInfoList = new ArrayList<>();
    private UserListAdapter userInfoAdapter;

    public SearchUserFragment() {
    }

    public static SearchUserFragment newInstance() {
        return new SearchUserFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userInfoList = new ArrayList<>();
        userInfoAdapter = new UserListAdapter(requireContext(), userInfoList);
        setAdapter(userInfoAdapter);

        setOnRefreshListener(this::refreshInternal);
        setOnLoadMoreListener(this::continueLoading);
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(()-> {
            Log.e("debug", "加载下一页");
            try {
                JSONArray result = (JSONArray) SearchApi.searchType(keyword, page, "bili_user");
                if (result != null) {
                    if (page == 1) showEmptyView(false);
                    List<UserInfo> list = new ArrayList<>();
                    SearchApi.getUsersFromSearchResult(result, list);
                    if(list.size()==0) setBottom(true);
                    CenterThreadPool.runOnUiThread(() -> {
                        int lastSize = userInfoList.size();
                        userInfoList.addAll(list);
                        userInfoAdapter.notifyItemRangeInserted(lastSize + 1, userInfoList.size() - lastSize);
                    });
                }
                else setBottom(true);
            } catch (Exception e) {
                loadFail(e);
            }
            setRefreshing(false);
        });
    }

    public void refreshInternal() {
        CenterThreadPool.runOnUiThread(() -> {
            page = 1;
            if (this.userInfoAdapter == null)
                this.userInfoAdapter = new UserListAdapter(this.requireContext(), this.userInfoList);
            int size_old = this.userInfoList.size();
            this.userInfoList.clear();
            if (size_old != 0) this.userInfoAdapter.notifyItemRangeRemoved(0, size_old);
            CenterThreadPool.run(() -> continueLoading(page));
        });
    }


}
