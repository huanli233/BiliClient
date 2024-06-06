package com.RobinNotBad.BiliClient.activity.settings;

import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.user.UpListAdapter;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;

public class SponsorActivity extends RefreshListActivity {
    
    private ArrayList<UserInfo> userList;
    private UpListAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setPageName("捐赠列表");
        
        CenterThreadPool.run(() -> {
            try{
                userList = AppInfoApi.getSponsors(this.page);
                adapter = new UpListAdapter(this,userList);
                setOnLoadMoreListener(this::continueLoading);
                setRefreshing(false);
                setAdapter(adapter);
            }catch(Exception e){
                report(e);
                setRefreshing(false);
            }
        });
    }
    
    private void continueLoading(int page) {
        CenterThreadPool.run(()->{
            try {
                int lastSize = userList.size();
                userList = AppInfoApi.getSponsors(page);
                runOnUiThread(() -> adapter.notifyItemRangeInserted(lastSize, userList.size() - lastSize));
                setRefreshing(false);
            } catch (Exception e){
                report(e);
                setRefreshing(false);
                this.page--;
            }
        });
    }
}
