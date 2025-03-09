package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.adapter.article.ArticleCardAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONArray;

import java.util.ArrayList;

public class SearchArticleFragment extends SearchFragment {

    private ArrayList<ArticleCard> articleCardList = new ArrayList<>();
    private ArticleCardAdapter articleCardAdapter;

    public SearchArticleFragment() {
    }

    public static SearchArticleFragment newInstance() {
        return new SearchArticleFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        articleCardList = new ArrayList<>();
        articleCardAdapter = new ArticleCardAdapter(requireContext(), articleCardList);
        setAdapter(articleCardAdapter);

        setOnRefreshListener(this::refreshInternal);
        setOnLoadMoreListener(this::continueLoading);
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(()-> {
            Log.e("debug", "加载下一页");
            try {
                JSONArray result = (JSONArray) SearchApi.searchType(keyword, page, "article");
                if (result != null) {
                    if (page == 1) showEmptyView(false);
                    ArrayList<ArticleCard> list = new ArrayList<>();
                    SearchApi.getArticlesFromSearchResult(result, list);
                    if(list.size()==0) setBottom(true);
                    CenterThreadPool.runOnUiThread(() -> {
                        int lastSize = articleCardList.size();
                        articleCardList.addAll(list);
                        articleCardAdapter.notifyItemRangeInserted(lastSize + 1, articleCardList.size() - lastSize);
                    });
                }
                else setBottom(true);
            } catch (Exception e) {
                report(e);
            }
            setRefreshing(false);
        });
    }

    public void refreshInternal() {
        CenterThreadPool.runOnUiThread(() -> {
            page = 1;
            if (this.articleCardAdapter == null)
                this.articleCardAdapter = new ArticleCardAdapter(this.requireContext(), this.articleCardList);
            int size_old = this.articleCardList.size();
            this.articleCardList.clear();
            if (size_old != 0) this.articleCardAdapter.notifyItemRangeRemoved(0, size_old);
            CenterThreadPool.run(()->continueLoading(page));
        });
    }
}
