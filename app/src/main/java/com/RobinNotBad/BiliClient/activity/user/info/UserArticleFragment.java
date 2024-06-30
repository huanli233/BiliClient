package com.RobinNotBad.BiliClient.activity.user.info;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.activity.base.RefreshListFragment;
import com.RobinNotBad.BiliClient.adapter.article.ArticleCardAdapter;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;
import java.util.List;

//用户专栏
//2023-09-30
//2024-05-03

public class UserArticleFragment extends RefreshListFragment {

    private long mid;
    private ArrayList<ArticleCard> articleList;
    private ArticleCardAdapter adapter;

    public UserArticleFragment() {
    }

    public static UserArticleFragment newInstance(long mid) {
        UserArticleFragment fragment = new UserArticleFragment();
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

        articleList = new ArrayList<>();
        setOnLoadMoreListener(this::continueLoading);

        CenterThreadPool.run(() -> {
            try {
                bottom = (UserInfoApi.getUserArticles(mid, page, articleList) == 1);
                if (isAdded()) {
                    adapter = new ArticleCardAdapter(requireContext(), articleList);
                    setAdapter(adapter);
                    setRefreshing(false);
                    if (bottom && articleList.isEmpty()) showEmptyView();
                }
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading(int page) {
        CenterThreadPool.run(() -> {
            try {
                List<ArticleCard> list = new ArrayList<>();
                int result = UserInfoApi.getUserArticles(mid, page, list);
                if (result != -1) {
                    Log.e("debug", "下一页");
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        articleList.addAll(list);
                        adapter.notifyItemRangeInserted(articleList.size() - list.size(), list.size());
                    });
                    if (result == 1) {
                        Log.e("debug", "到底了");
                        bottom = true;
                    }
                }
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}