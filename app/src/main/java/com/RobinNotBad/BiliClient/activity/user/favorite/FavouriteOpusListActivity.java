package com.RobinNotBad.BiliClient.activity.user.favorite;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.article.OpusAdapter;
import com.RobinNotBad.BiliClient.api.FavoriteApi;
import com.RobinNotBad.BiliClient.model.Opus;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONException;

public class FavouriteOpusListActivity extends BaseActivity {
    RecyclerView recycler;
    int page = 1;
    boolean isLoadingMore = false;
    ArrayList<Opus> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);
        
        recycler = findViewById(R.id.recyclerView);
        
        setPageName("图文收藏夹");
        CenterThreadPool.run(()->{
            try {
                list = FavoriteApi.getFavouriteOpus(page);
                OpusAdapter adapter = new OpusAdapter(this,list);
                Log.e("","amount:"+list.size());
                runOnUiThread(()->{
                    recycler.setLayoutManager(new LinearLayoutManager(this));
                    recycler.setAdapter(adapter);
                });
            } catch(IOException err) {
            	err.printStackTrace();
            } catch(JSONException err){
                err.printStackTrace();
            }
           
        });
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState){
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        if (!recyclerView.canScrollVertically(1)&&!isLoadingMore) {
                            loadMore();
                            Log.e("","滑动到底部，开始刷新");
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        
    }
    public void loadMore() {
    	page++;
        isLoadingMore = true;
        CenterThreadPool.run(()->{
        try {
        	ArrayList<Opus> listNew = FavoriteApi.getFavouriteOpus(page);
            
            runOnUiThread(()->{
                OpusAdapter adapter = (OpusAdapter)recycler.getAdapter();
                adapter.insertItem(listNew);
            });
        } catch(IOException err) {
        	err.printStackTrace();
        } catch(JSONException err){
            err.printStackTrace();
        }
           
        });
    isLoadingMore=false;
    }
}
