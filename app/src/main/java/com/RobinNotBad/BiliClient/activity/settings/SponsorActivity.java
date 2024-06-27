package com.RobinNotBad.BiliClient.activity.settings;

import android.os.Bundle;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.user.UserListAdapter;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;

public class SponsorActivity extends RefreshListActivity {

    private ArrayList<UserInfo> userList;
    private UserListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("捐赠列表");

        CenterThreadPool.run(() -> {
            try {
                userList = AppInfoApi.getSponsors(this.page);
                adapter = new UserListAdapter(this, userList);
                setOnLoadMoreListener(this::continueLoading);
                setRefreshing(false);
                setAdapter(adapter);

                if (userList.size() < 1) setBottom(true);
            } catch (Exception e) {
                report(e);
                runOnUiThread(() -> MsgUtil.toast("连接到哔哩终端接口时发生错误", this));
                setRefreshing(false);
            }
        });
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(() -> {
            try {
                int lastSize = userList.size();
                userList = AppInfoApi.getSponsors(page);
                runOnUiThread(() -> adapter.notifyItemRangeInserted(lastSize, userList.size() - lastSize));
                setRefreshing(false);

                if (userList.size() < 1) setBottom(true);
            } catch (Exception e) {
                runOnUiThread(() -> MsgUtil.toast("连接到哔哩终端接口时发生错误", this));
                loadFail(e);
            }
        });
    }
}
