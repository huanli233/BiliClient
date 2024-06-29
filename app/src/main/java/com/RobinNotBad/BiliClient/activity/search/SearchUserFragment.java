package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.user.UserListAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class SearchUserFragment extends Fragment implements SearchRefreshable {
    RecyclerView recyclerView;
    private List<UserInfo> userInfoList = new ArrayList<>();

    private UserListAdapter userInfoAdapter;

    private String keyword;
    private boolean isFirstLoad = true;
    private boolean refreshing = false;
    private boolean bottom = false;
    private int page = 0;
    private TextView emptyView;

    public SearchUserFragment() {
    }

    public static SearchUserFragment newInstance() {
        return new SearchUserFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyTip);
        userInfoList = new ArrayList<>();

        recyclerView.setHasFixedSize(true);

        CenterThreadPool.run(() -> {
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                userInfoAdapter = new UserListAdapter(requireContext(), userInfoList);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(userInfoAdapter);

                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        assert manager != null;
                        int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();  //获取最后一个完全显示的itemPosition
                        int itemCount = manager.getItemCount();
                        if (lastItemPosition >= (itemCount - 3) && dy > 0 && !refreshing && !bottom) {// 滑动到倒数第三个就可以刷新了
                            refreshing = true;
                            CenterThreadPool.run(() -> continueLoading()); //加载第二页
                        }

                        if (requireActivity() instanceof SearchActivity) {
                            SearchActivity activity = (SearchActivity) requireActivity();
                            activity.onScrolled(dy);
                        }
                    }
                });
            });
        });
    }

    private void continueLoading() {
        page++;
        Log.e("debug", "加载下一页");
        try {
            JSONArray result = (JSONArray) SearchApi.searchType(keyword, page, "bili_user");
            if (result != null) {
                List<UserInfo> list = new ArrayList<>();
                SearchApi.getUsersFromSearchResult(result, list);
                CenterThreadPool.runOnUiThread(() -> {
                    int lastSize = userInfoList.size();
                    userInfoList.addAll(list);
                    userInfoAdapter.notifyItemRangeInserted(lastSize + 1, userInfoList.size() - lastSize);
                });
            } else {
                bottom = true;
                if (isAdded() && !isFirstLoad) {
                    requireActivity().runOnUiThread(() -> MsgUtil.showMsg("已经到底啦OwO", requireContext()));
                }
                if (isFirstLoad) showEmptyView();
            }
            isFirstLoad = false;
        } catch (Exception e) {
            if (isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
        }
        refreshing = false;
    }

    @Override
    public void refresh(String keyword) {
        this.isFirstLoad = true;
        this.refreshing = true;
        this.page = 0;
        this.keyword = keyword;
        CenterThreadPool.runOnUiThread(() -> {
            if (this.userInfoAdapter == null)
                this.userInfoAdapter = new UserListAdapter(this.requireContext(), this.userInfoList);
            int size_old = this.userInfoList.size();
            this.userInfoList.clear();
            if (size_old != 0) this.userInfoAdapter.notifyItemRangeRemoved(0, size_old);
            CenterThreadPool.run(this::continueLoading);
        });
    }

    public void showEmptyView() {
        if (emptyView != null && isAdded()) {
            requireActivity().runOnUiThread(() -> {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            });
        }
    }
}
