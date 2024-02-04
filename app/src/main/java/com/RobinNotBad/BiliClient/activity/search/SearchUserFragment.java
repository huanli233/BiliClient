package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.RobinNotBad.BiliClient.adapter.FollowListAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONArray;

import java.util.ArrayList;

public class SearchUserFragment extends Fragment {
    RecyclerView recyclerView;
    private ArrayList<UserInfo> userInfoList;

    private FollowListAdapter userInfoAdapter;

    private String keyword;
    private boolean refreshing = false;
    private boolean bottom = false;
    private int page = 0;


    public SearchUserFragment(){}

    public static SearchUserFragment newInstance(ArrayList<UserInfo> userInfoList, String keyword) {
        SearchUserFragment fragment = new SearchUserFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("list",userInfoList);
        bundle.putString("keyword",keyword);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            userInfoList = getArguments().getParcelableArrayList("list");
            keyword = getArguments().getString("keyword");
        }
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
        CenterThreadPool.run(() -> {
            if(isAdded()) requireActivity().runOnUiThread(() -> {
                userInfoAdapter = new FollowListAdapter(requireContext(), userInfoList);
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
                        if (lastItemPosition >= (itemCount - 3) && dy>0 && !refreshing && !bottom) {// 滑动到倒数第三个就可以刷新了
                            refreshing = true;
                            CenterThreadPool.run(() -> continueLoading(requireContext())); //加载第二页
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

    private void continueLoading(Context context){
        refreshing = true;
        page++;
        Log.e("debug","加载下一页");
        int lastSize = userInfoList.size();
        try {
            JSONArray result =  SearchApi.searchType(keyword,page,"bili_user");
            if(result!=null) {
                SearchApi.getUsersFromSearchResult(result, userInfoList);
                CenterThreadPool.runOnUiThread(() -> userInfoAdapter.notifyItemRangeInserted(lastSize + 1,userInfoList.size()-lastSize));
            }
            else {
                bottom = true;
                MsgUtil.toast("已经到底啦OwO",context);
            }
        } catch (Exception e){CenterThreadPool.runOnUiThread(()-> MsgUtil.err(e,context));}
        refreshing = false;
    }


    public void refresh(ArrayList<UserInfo> userInfoList, String keyword){
        this.keyword = keyword;
        int size_old = this.userInfoList.size();
        this.userInfoList = userInfoList;
        CenterThreadPool.runOnUiThread(()-> {
            userInfoAdapter.notifyItemRangeRemoved(0,size_old);
            userInfoAdapter.notifyItemRangeInserted(0,userInfoList.size());

            refreshing = false;
            bottom = false;
            page = 0;
        });
    }
}
