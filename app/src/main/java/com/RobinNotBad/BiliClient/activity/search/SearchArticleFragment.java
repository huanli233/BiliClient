package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
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
import com.RobinNotBad.BiliClient.adapter.ArticleCardAdapter;
import com.RobinNotBad.BiliClient.adapter.FollowListAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONArray;

import java.util.ArrayList;

public class SearchArticleFragment extends Fragment {
    RecyclerView recyclerView;
    private ArrayList<ArticleCard> articleCardList;

    private ArticleCardAdapter articleCardAdapter;

    private String keyword;
    private boolean refreshing = false;
    private boolean bottom = false;
    private int page = 0;

    public SearchArticleFragment(){}

    public static SearchArticleFragment newInstance() {
        return new SearchArticleFragment();
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
        articleCardList = new ArrayList<>();

        CenterThreadPool.run(() -> {
            if(isAdded()) requireActivity().runOnUiThread(() -> {
                articleCardAdapter = new ArticleCardAdapter(requireContext(), articleCardList);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(articleCardAdapter);

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
                        if (lastItemPosition >= (itemCount - 3) && dy>0 && !refreshing && !bottom) {// 滑动到倒数第三个就可以刷新了
                            refreshing = true;
                            CenterThreadPool.run(()->continueLoading()); //加载第二页
                        }

                        if(requireActivity() instanceof SearchActivity) {
                            SearchActivity activity = (SearchActivity) requireActivity();
                            int searchbar_alpha = activity.searchBarAlpha - (4*dy);
                            if (searchbar_alpha < 0) {
                                searchbar_alpha = 0;
                                activity.searchBar.setVisibility(View.GONE);
                            } else {
                                activity.searchBar.setVisibility(View.VISIBLE);
                                if (searchbar_alpha > 100) {
                                    searchbar_alpha = 100;
                                }
                                activity.searchBar.setAlpha(searchbar_alpha / 100f);
                            }
                            activity.searchBarAlpha = searchbar_alpha;
                            //Log.e("debug", "dx=" + dx + ",dy=" + dy);
                        }
                    }
                });
            });
        });
    }

    private void continueLoading(){
        page++;
        Log.e("debug","加载下一页");
        int lastSize = articleCardList.size();
        try {
            JSONArray result = SearchApi.searchType(keyword,page,"article");
            if(result!=null) {
                SearchApi.getArticlesFromSearchResult(result, articleCardList);
                CenterThreadPool.runOnUiThread(() -> articleCardAdapter.notifyItemRangeInserted(lastSize + 1, articleCardList.size()-lastSize));
            }
            else {
                bottom = true;
                if(isAdded()) requireActivity().runOnUiThread(() ->  MsgUtil.toast("已经到底啦OwO",requireContext()));
            }
        } catch (Exception e){if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.err(e,requireContext()));}
        refreshing = false;
    }

    public void refresh(String keyword){
        refreshing = true;
        this.page = 0;
        this.keyword = keyword;
        int size_old = articleCardList.size();
        articleCardList.clear();
        requireActivity().runOnUiThread(()->{
            articleCardAdapter.notifyItemRangeRemoved(0,size_old);
            CenterThreadPool.run(this::continueLoading);
        });
    }
}
