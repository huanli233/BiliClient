package com.RobinNotBad.BiliClient.activity.user;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.HistoryApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;

//历史记录
//2023-08-18

public class HistoryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ArrayList<VideoCard> videoList;
    private VideoCardAdapter videoCardAdapter;
    private boolean bottom = false;
    private int page = 1;
    private boolean refreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("历史记录");

        recyclerView = findViewById(R.id.recyclerView);

        videoList = new ArrayList<>();

        CenterThreadPool.run(()->{
            try {
                int result = HistoryApi.getHistory(page,videoList);
                if(result != -1) {
                    videoCardAdapter = new VideoCardAdapter(this, videoList);
                    runOnUiThread(()->{
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        recyclerView.setAdapter(videoCardAdapter);
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
                    if(result == 1) {
                        Log.e("debug","到底了");
                        bottom = true;
                    }
                }

            } catch (Exception e) {runOnUiThread(()-> MsgUtil.err(e,this));}
        });
    }

    private void continueLoading() {
        page++;
        try {
            int lastSize = videoList.size();
            int result = HistoryApi.getHistory(page,videoList);
            if(result != -1){
                Log.e("debug","下一页");
                runOnUiThread(()-> videoCardAdapter.notifyItemRangeInserted(lastSize,videoList.size()-lastSize));
                if(result == 1) {
                    Log.e("debug","到底了");
                    bottom = true;
                }
            }
            refreshing = false;
        } catch (Exception e) {runOnUiThread(()-> MsgUtil.err(e,this));}
    }
}