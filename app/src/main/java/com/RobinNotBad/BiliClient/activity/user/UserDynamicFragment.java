package com.RobinNotBad.BiliClient.activity.user;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.UserInfoAdapter;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

//用户动态
//2023-09-30

public class UserDynamicFragment extends Fragment {

    private long mid;
    private RecyclerView recyclerView;
    private ArrayList<Dynamic> dynamicList;
    private UserInfoAdapter adapter;
    private boolean refreshing = false;
    private boolean bottom = false;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);

        dynamicList = new ArrayList<>();

        CenterThreadPool.run(()->{
            try {
                UserInfo userInfo = UserInfoApi.getUserInfo(mid);

                JSONObject dynamic = DynamicApi.getUserDynamic(mid,offset);
                offset = DynamicApi.analyzeDynamicList(dynamic,dynamicList);

                bottom = (offset==-1);

                if(isAdded()) requireActivity().runOnUiThread(()-> {
                    adapter = new UserInfoAdapter(requireContext(), dynamicList, userInfo);
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
                                CenterThreadPool.run(() -> continueLoading()); //加载第二页
                            }
                        }
                    });
                });
            } catch (IOException e){
                if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,getContext()));
                e.printStackTrace();
            } catch (JSONException e) {
                if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_json,getContext()));
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading() {
        try {
            int lastSize = dynamicList.size();
            JSONObject jsonObject = DynamicApi.getUserDynamic(mid,offset);
            offset = DynamicApi.analyzeDynamicList(jsonObject,dynamicList);
            if(isAdded()) requireActivity().runOnUiThread(()-> adapter.notifyItemRangeInserted(lastSize + 1, dynamicList.size() + 1 - lastSize));
            bottom = (offset==-1);
            refreshing = false;
        } catch (IOException e){
            if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,getContext()));
            e.printStackTrace();
        } catch (JSONException e) {
            if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_json,getContext()));
            e.printStackTrace();
        }
    }
}