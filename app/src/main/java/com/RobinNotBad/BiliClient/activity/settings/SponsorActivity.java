package com.RobinNotBad.BiliClient.activity.settings;

import android.os.Bundle;

import android.widget.Toast;
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

                if(userList.size() < 1) setBottom(true);
            }catch(Exception e){
                if(e.getMessage().contains("<!DOCTYPE")) runOnUiThread(() -> MsgUtil.toast("哔哩终端域名疑似失效或被屏蔽",this));
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
                
                if(userList.size() < 1) setBottom(true);
            } catch (Exception e){
                if(e.getMessage().contains("<!DOCTYPE")) runOnUiThread(() -> MsgUtil.toast("哔哩终端域名疑似失效或被屏蔽",this));
                report(e);
                setRefreshing(false);
                this.page--;
            }
        });
    }
}
