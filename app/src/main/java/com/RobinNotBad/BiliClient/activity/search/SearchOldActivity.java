package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.MenuActivity;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.SearchAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class SearchOldActivity extends InstanceActivity {

    private RecyclerView recyclerView;
    private ConstraintLayout searchBar;
    private ArrayList<VideoCard> videoCardList;
    private SearchAdapter searchAdapter;
    private String keyword;
    private boolean refreshing = false;
    private boolean bottom = false;
    private int page = 0;
    private boolean firstRun = true;
    private int searchbar_alpha = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_old);
        Log.e("debug","进入搜索页");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchOldActivity.this));
        videoCardList = new ArrayList<>();
        searchAdapter = new SearchAdapter(this,videoCardList);

        findViewById(R.id.top).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(SearchOldActivity.this, MenuActivity.class);
            intent.putExtra("from",1);
            startActivity(intent);
        });

        View searchBtn = findViewById(R.id.search);
        EditText keywordInput = findViewById(R.id.keywordInput);
        searchBar = findViewById(R.id.searchbar);

        searchBtn.setOnClickListener(view -> searchKeyword(keywordInput.getText().toString()));
        keywordInput.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction()) {
                searchKeyword(keywordInput.getText().toString());
            }
            return false;
        });

        recyclerView.setAdapter(searchAdapter);
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
                    new Thread(() -> continueLoading()).start(); //加载第二页
                }
                searchbar_alpha = searchbar_alpha - dy;
                if(searchbar_alpha < 0){
                    searchbar_alpha = 0;
                    searchBar.setVisibility(View.GONE);
                }else{
                    searchBar.setVisibility(View.VISIBLE);
                }
                if(searchbar_alpha > 100){
                    searchbar_alpha = 100;
                }
                searchBar.setAlpha(searchbar_alpha / 100f);
                Log.e("debug","dx=" + dx + ",dy=" + dy);
            }
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchKeyword(String str){
        if(str.contains("Robin") || str.contains("robin")){
            if(str.contains("撅")){
                MsgUtil.showText(this,"特殊彩蛋",getString(R.string.egg_special));
                return;
            }
            if(str.contains("纳西妲")){
                MsgUtil.showText(this,"特殊彩蛋",getString(R.string.egg_robin_nahida));
                return;
            }
        }

        if(!refreshing) {
            refreshing = true;
            bottom = false;

            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);



            if (str.equals("")) {
                runOnUiThread(() -> Toast.makeText(this, "你还木有输入内容哦~", Toast.LENGTH_SHORT).show());
            } else {
                keyword = str;

                if (firstRun) {
                    recyclerView.setAdapter(searchAdapter);
                    firstRun = false;
                } else {
                    int size = videoCardList.size();
                    videoCardList.clear();
                    searchAdapter.notifyItemRangeRemoved(0,size);
                    Log.e("debug", "清空");
                }

                new Thread(() -> {
                    try {
                        page = 1;
                        JSONArray result = SearchApi.search(keyword, 1);
                        if (result != null) {
                            SearchApi.getVideosFromSearchResult(result, videoCardList);
                            runOnUiThread(() -> {
                                searchAdapter.notifyItemRangeInserted(0, videoCardList.size());
                                Log.e("debug", "刷新");
                            });
                        } else runOnUiThread(() -> MsgUtil.toast("搜索结果为空OwO", this));
                    } catch (IOException e) {
                        runOnUiThread(() -> MsgUtil.quickErr(MsgUtil.err_net, this));
                        e.printStackTrace();
                    } catch (JSONException e) {
                        runOnUiThread(() -> MsgUtil.jsonErr(e,this));
                        e.printStackTrace();
                    }
                    refreshing = false;
                }).start();
            }
        }
    }

    private void continueLoading(){
        refreshing = true;
        page++;
        Log.e("debug","加载下一页");
        int lastSize = videoCardList.size();
        try {
            JSONArray result =  SearchApi.search(keyword,page);
            if(result!=null) {
                SearchApi.getVideosFromSearchResult(result, videoCardList);
                runOnUiThread(() -> searchAdapter.notifyItemRangeInserted(lastSize + 1,videoCardList.size()-lastSize));
            }
            else {
                bottom = true;
                MsgUtil.toast("已经到底啦OwO",this);
            }
        } catch (IOException e){
            runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,this));
            e.printStackTrace();
        } catch (JSONException e) {
            runOnUiThread(()-> MsgUtil.jsonErr(e,this));
            e.printStackTrace();
        }
        refreshing = false;
    }
    private Point startPoint;

    /**
     * Called when a touch screen event was not handled by any of the views under it.
     * if is swipe up, hide search bar, else show search bar
     * @param event The touch screen event being processed.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startPoint = new Point((int)event.getX(),(int)event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                //先判断是不是竖向滑动， 如果是竖向滑动， 再考虑是上滑还是下滑
                boolean isVerticalMove = Math.abs(event.getY() - startPoint.y) > Math.abs(event.getX() - startPoint.x);
                if(isVerticalMove) {
                    //上滑显示
                    boolean isSwipeUp = event.getY() - startPoint.y < 0;
                    if(isSwipeUp) {
                        searchBar.setVisibility(View.VISIBLE);
                    }else {
                        searchBar.setVisibility(View.GONE);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}