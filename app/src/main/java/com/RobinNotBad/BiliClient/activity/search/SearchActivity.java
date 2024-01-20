package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.MenuActivity;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends InstanceActivity {

    SearchVideoFragment searchVideoFragment;
    SearchArticleFragment searchArticleFragment;
    SearchUserFragment searchUserFragment;

    private ViewPager viewPager;
    public ConstraintLayout searchBar;
    public int searchBarAlpha = 100;
    private ArrayList<VideoCard> videoCardList;
    private ArrayList<UserInfo> userInfoList;
    private ArrayList<ArticleInfo> articleInfoList;
    private String keyword;
    private boolean refreshing = false;
    private boolean firstRun = true;
    private boolean firstFragment = true;

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(SharedPreferencesUtil.getBoolean("old_search_enable",false)){
            Log.e("debug","送到旧版搜索");
            Intent intent = new Intent(this,SearchOldActivity.class);
            startActivity(intent);
            finish();
        }
        
        setContentView(R.layout.activity_search);
        Log.e("debug","进入搜索页");

        viewPager = findViewById(R.id.viewPager);

        videoCardList = new ArrayList<>();
        userInfoList = new ArrayList<>();
        articleInfoList = new ArrayList<>();

        findViewById(R.id.top).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(SearchActivity.this, MenuActivity.class);
            intent.putExtra("from",3);
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

                if (firstRun) firstRun = false;
                else {
                    videoCardList.clear();
                    userInfoList.clear();
                    articleInfoList.clear();
                    Log.e("debug", "清空");
                }

                CenterThreadPool.run(() -> {
                    try {
                        JSONArray resultVideo = SearchApi.search(keyword, 1);
                        JSONArray resultUser = SearchApi.searchType(keyword, 1,"bili_user");
                        JSONArray resultArticle = SearchApi.searchType(keyword, 1,"article");

                        if (resultVideo != null) SearchApi.getVideosFromSearchResult(resultVideo, videoCardList);
                        else runOnUiThread(() -> MsgUtil.toast("视频搜索结果为空OwO", this));

                        if (resultUser != null) SearchApi.getUsersFromSearchResult(resultUser, userInfoList);
                        else runOnUiThread(() -> MsgUtil.toast("用户搜索结果为空OwO", this));

                        if (resultArticle != null) SearchApi.getArticlesFromSearchResult(resultArticle, articleInfoList);
                        else runOnUiThread(() -> MsgUtil.toast("文章搜索结果为空OwO", this));

                        runOnUiThread(this::reload_fragments);
                    } catch (IOException e) {
                        runOnUiThread(() -> MsgUtil.quickErr(MsgUtil.err_net, this));
                        e.printStackTrace();
                    } catch (JSONException e) {
                        runOnUiThread(() -> MsgUtil.jsonErr(e,this));
                        e.printStackTrace();
                    }
                    refreshing = false;
                });
            }
        }
    }

    private void reload_fragments(){
        if(firstFragment) { //第一次搜索
            List<Fragment> fragmentList = new ArrayList<>();
            searchVideoFragment = SearchVideoFragment.newInstance(videoCardList, keyword);
            fragmentList.add(searchVideoFragment);
            searchArticleFragment = SearchArticleFragment.newInstance(articleInfoList, keyword);
            fragmentList.add(searchArticleFragment);
            searchUserFragment = SearchUserFragment.newInstance(userInfoList, keyword);
            fragmentList.add(searchUserFragment);
            viewPager.setOffscreenPageLimit(fragmentList.size());

            ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
            viewPager.setAdapter(vpfAdapter);

            firstFragment = false;
        } else { //再次搜索
            searchVideoFragment.refresh(videoCardList,keyword);
            searchArticleFragment.refresh(articleInfoList,keyword);
            searchUserFragment.refresh(userInfoList,keyword);
        }
    }
}