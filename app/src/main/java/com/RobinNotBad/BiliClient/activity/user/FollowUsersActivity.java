package com.RobinNotBad.BiliClient.activity.user;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.user.UserListAdapter;
import com.RobinNotBad.BiliClient.api.FollowApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

//关注列表
//2023-07-22
//2024-05-01

public class FollowUsersActivity extends RefreshListActivity {

    private long mid;
    private ArrayList<UserInfo> userList;
    private UserListAdapter adapter;
    private int mode;
    private EditText searchEditText;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = getIntent().getIntExtra("mode", 0);
        mid = getIntent().getLongExtra("mid", -1);

        if (mode < 0 || mode > 1 || mid == -1) {
            finish();
            return;
        }

        setPageName(mode == 0 ? "关注列表" : "粉丝列表");

        recyclerView.setHasFixedSize(true);

        userList = new ArrayList<>();
        adapter = new UserListAdapter(this, userList);
        setAdapter(adapter);

        searchEditText = findViewById(R.id.search_edit_text);
        if (mode == 0) {
            findViewById(R.id.search_layout).setVisibility(View.VISIBLE);
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String query = s.toString();
                    if (TextUtils.isEmpty(query)) {
                        // if query is empty, load initial list
                        resetAndLoadInitialList();
                    } else {
                        // otherwise, perform search after a delay
                        searchRunnable = () -> performSearch(query);
                        searchHandler.postDelayed(searchRunnable, 300); // 300ms delay
                    }
                }
            });
        }

        resetAndLoadInitialList();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetAndLoadInitialList() {
        setRefreshing(true);
        page = 1;
        bottom = false;
        userList.clear();
        adapter.notifyDataSetChanged();
        setOnLoadMoreListener(this::continueLoading);
        loadInitialList();
    }

    private void loadInitialList() {
        CenterThreadPool.run(() -> {
            try {
                List<UserInfo> list = new ArrayList<>();
                int result = mode == 0 ? FollowApi.getFollowingList(mid, page, list) : FollowApi.getFollowerList(mid, page, list);
                runOnUiThread(() -> {
                    userList.addAll(list);
                    adapter.notifyDataSetChanged();
                    setRefreshing(false);
                    if (result == 1) {
                        setBottom(true);
                    }
                });
            } catch (Exception e) {
                handleLoadError(e);
            }
        });
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(() -> {
            try {
                List<UserInfo> list = new ArrayList<>();
                int result = mode == 0 ? FollowApi.getFollowingList(mid, page, list) : FollowApi.getFollowerList(mid, page, list);
                runOnUiThread(() -> {
                    userList.addAll(list);
                    adapter.notifyItemRangeInserted(userList.size() - list.size(), list.size());
                    setRefreshing(false);
                    if (result == 1) {
                        setBottom(true);
                    }
                });
            } catch (Exception e) {
                handleLoadError(e);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void performSearch(String query) {
        setRefreshing(true);
        setOnLoadMoreListener(null); // Disable load more for search results
        CenterThreadPool.run(() -> {
            try {
                List<UserInfo> searchResult = new ArrayList<>();
                FollowApi.searchFollowingList(mid, query, searchResult);
                runOnUiThread(() -> {
                    userList.clear();
                    userList.addAll(searchResult);
                    adapter.notifyDataSetChanged();
                    setRefreshing(false);
                    setBottom(true); // Search results are not paginated
                });
            } catch (Exception e) {
                handleLoadError(e);
            }
        });
    }

    private void handleLoadError(Exception e) {
        if (e.getMessage() != null && (e.getMessage().startsWith("22115") || e.getMessage().startsWith("22118"))) {
            finish();
            MsgUtil.showMsg(e.getMessage());
        } else {
            loadFail(e);
        }
    }
}