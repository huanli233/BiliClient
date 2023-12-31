package com.RobinNotBad.BiliClient.activity.user;

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
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

//用户专栏
//2023-09-30

public class UserArticleFragment extends Fragment {

    private long mid;
    private RecyclerView recyclerView;
    private ArrayList<ArticleInfo> articleList;
    private ArticleCardAdapter adapter;
    private boolean refreshing = false;
    private boolean bottom = false;
    private int page = 1;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);

        articleList = new ArrayList<>();

        new Thread(()->{
            try {
                bottom = (UserInfoApi.getUserArticles(mid,page,articleList) == 1);
                if(isAdded()) requireActivity().runOnUiThread(()-> {
                    adapter = new ArticleCardAdapter(requireContext(), articleList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
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
                                new Thread(() -> continueLoading()).start(); //加载第二页
                            }
                        }
                    });
                });

            } catch (IOException e){
                if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,requireContext()));
                e.printStackTrace();
            } catch (JSONException e) {
                if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.jsonErr(e,requireContext()));
                e.printStackTrace();
            }
        }).start();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading() {
        page++;
        try {
            int lastSize = articleList.size();
            int result = UserInfoApi.getUserArticles(mid,page,articleList);
            if(result != -1){
                Log.e("debug","下一页");
                if(isAdded()) requireActivity().runOnUiThread(()-> adapter.notifyItemRangeInserted(lastSize, articleList.size() - lastSize));
                if(result == 1) {
                    Log.e("debug","到底了");
                    bottom = true;
                }
            }
            refreshing = false;
        } catch (IOException e){
            if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,requireContext()));
            e.printStackTrace();
        } catch (JSONException e) {
            if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.jsonErr(e,requireContext()));
            e.printStackTrace();
        }
    }
}