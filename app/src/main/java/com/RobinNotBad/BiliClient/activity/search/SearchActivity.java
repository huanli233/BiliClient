package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends InstanceActivity {

    SearchVideoFragment searchVideoFragment;
    SearchArticleFragment searchArticleFragment;
    SearchUserFragment searchUserFragment;

    private View searchBar;
    private boolean searchBarVisible = true;

    //public int searchBarAlpha = 100;
    private String keyword;
    private boolean refreshing = false;

    Handler handler;

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        setMenuClick(3);
        Log.e("debug","进入搜索页");

        handler = new Handler();

        ViewPager viewPager = findViewById(R.id.viewPager);

        View searchBtn = findViewById(R.id.search);
        EditText keywordInput = findViewById(R.id.keywordInput);
        searchBar = findViewById(R.id.searchbar);

        List<Fragment> fragmentList = new ArrayList<>();
        searchVideoFragment = SearchVideoFragment.newInstance();
        fragmentList.add(searchVideoFragment);
        searchArticleFragment = SearchArticleFragment.newInstance();
        fragmentList.add(searchArticleFragment);
        searchUserFragment = SearchUserFragment.newInstance();
        fragmentList.add(searchUserFragment);
        viewPager.setOffscreenPageLimit(fragmentList.size());

        ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(vpfAdapter);

        searchBtn.setOnClickListener(view -> searchKeyword(keywordInput.getText().toString()));
        keywordInput.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction()) {
                searchKeyword(keywordInput.getText().toString());
            }
            return false;
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

        if(!SharedPreferencesUtil.getBoolean("tutorial_search",false)){
            MsgUtil.showDialog(this,"使用教程","新版搜索页面中展示出来的搜索结果从左向右或从右向左滑动可以切换页面，第一页为视频列表，第二页为专栏列表，第三页为用户列表",R.mipmap.tutorial_search,true,5);
            SharedPreferencesUtil.putBoolean("tutorial_search",true);
        }

        if(!refreshing) {
            refreshing = true;

            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            if (str.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "你还木有输入内容哦~", Toast.LENGTH_SHORT).show());
            } else {
                keyword = str;

                CenterThreadPool.run(()->{
                    try {
                        searchVideoFragment.refresh(keyword);
                        searchArticleFragment.refresh(keyword);
                        searchUserFragment.refresh(keyword);
                        refreshing = false;
                    }catch (Exception e){
                        refreshing = false;
                        runOnUiThread(()->MsgUtil.err(e,this));
                    }
                });

            }
        }
    }


    public void onScrolled(int dy) {
        int height = searchBar.getHeight() + LittleToolsUtil.dp2px(4f,this);

        if (dy > 0 && searchBarVisible) {
            if(searchBar.getAnimation()==null || searchBar.getAnimation().hasEnded()) {
                this.searchBarVisible = false;
                Log.e("debug", "dy>0");
                TranslateAnimation hide = new TranslateAnimation(0, 0, 0, -height);
                handler.postDelayed(()->searchBar.setVisibility(View.GONE),250);
                doAnimation(hide);
            }
        }
        if (dy < 0 && !searchBarVisible) {
            if(searchBar.getAnimation()==null || searchBar.getAnimation().hasEnded()) {
                this.searchBarVisible = true;
                Log.e("debug", "dy<0");
                TranslateAnimation show = new TranslateAnimation(0, 0, -height, 0);
                searchBar.setVisibility(View.VISIBLE);
                doAnimation(show);
            }
        }
    }

    private void doAnimation(Animation animation){
        animation.setDuration(250);
        animation.setFillAfter(true);
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        animation.setInterpolator(interpolator);
        searchBar.startAnimation(animation);
    }
}