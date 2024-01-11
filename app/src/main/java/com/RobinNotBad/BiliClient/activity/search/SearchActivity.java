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
    private ConstraintLayout searchBar;
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
        setContentView(R.layout.activity_search);
        Log.e("debug","进入搜索页");

        viewPager = findViewById(R.id.viewPager);

        videoCardList = new ArrayList<>();
        userInfoList = new ArrayList<>();
        articleInfoList = new ArrayList<>();

        findViewById(R.id.top).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(SearchActivity.this, MenuActivity.class);
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

        if(SharedPreferencesUtil.getBoolean("first_search",true)){
            Toast.makeText(this, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
            SharedPreferencesUtil.putBoolean("first_search",false);
        }
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

                        if (resultVideo != null) {
                            SearchApi.getVideosFromSearchResult(resultVideo, videoCardList);
                            Log.e("debug", "刷新");
                        } else runOnUiThread(() -> MsgUtil.toast("视频搜索结果为空OwO", this));

                        if (resultUser != null) {
                            SearchApi.getUsersFromSearchResult(resultUser, userInfoList);
                            Log.e("debug", "刷新");
                        } else runOnUiThread(() -> MsgUtil.toast("用户搜索结果为空OwO", this));

                        if (resultArticle != null) {
                            SearchApi.getArticlesFromSearchResult(resultArticle, articleInfoList);
                            Log.e("debug", "刷新");
                        } else runOnUiThread(() -> MsgUtil.toast("文章搜索结果为空OwO", this));

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
        if(firstFragment) {
            List<Fragment> fragmentList = new ArrayList<>();
            searchVideoFragment = SearchVideoFragment.newInstance(videoCardList, searchBar, keyword);
            fragmentList.add(searchVideoFragment);
            searchArticleFragment = SearchArticleFragment.newInstance(articleInfoList, searchBar, keyword);
            fragmentList.add(searchArticleFragment);
            searchUserFragment = SearchUserFragment.newInstance(userInfoList, searchBar, keyword);
            fragmentList.add(searchUserFragment);
            viewPager.setOffscreenPageLimit(fragmentList.size());

            ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
            viewPager.setAdapter(vpfAdapter);  //没啥好说的，教科书式的ViewPager使用方法

            firstFragment=false;
        }
        else{
            searchVideoFragment.refresh(videoCardList,keyword);
            searchArticleFragment.refresh(articleInfoList,keyword);
            searchUserFragment.refresh(userInfoList,keyword);
        }
    }
}