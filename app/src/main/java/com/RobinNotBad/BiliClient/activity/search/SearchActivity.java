package com.RobinNotBad.BiliClient.activity.search;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.SearchHistoryAdapter;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.JsonUtil;
import com.RobinNotBad.BiliClient.util.LinkUrlUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

public class SearchActivity extends InstanceActivity {
    private String lastKeyword = "≠~`";
    private RecyclerView historyRecyclerview;
    SearchHistoryAdapter searchHistoryAdapter;
    EditText keywordInput;
    private ConstraintLayout searchBar;
    private boolean searchBarVisible = true;
    private boolean refreshing = false;
    private long animate_last;
    Handler handler;
    ArrayList<String> searchHistory;

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_search, null, (layoutView, id, parent) -> {
            setContentView(layoutView);
            setMenuClick();
            Log.e("debug", "进入搜索页");

            TutorialHelper.show(R.xml.tutorial_search, this, "search", 3);

            handler = new Handler();

            ViewPager2 viewPager = findViewById(R.id.viewPager);

            View searchBtn = findViewById(R.id.search);
            keywordInput = findViewById(R.id.keywordInput);
            searchBar = findViewById(R.id.searchbar);
            historyRecyclerview = findViewById(R.id.history_recyclerview);
            viewPager.setOffscreenPageLimit(4);
            keywordInput.setOnFocusChangeListener((view, b) -> historyRecyclerview.setVisibility(b ? View.VISIBLE : View.GONE));
            historyRecyclerview.setVisibility(View.VISIBLE);
            FragmentStateAdapter vpfAdapter = new FragmentStateAdapter(this) {

                @Override
                public int getItemCount() {
                    return 4;
                }

                @NonNull
                @Override
                public Fragment createFragment(int position) {
                    if (position == 0) return SearchVideoFragment.newInstance();
                    if (position == 1) return SearchArticleFragment.newInstance();
                    if (position == 2) return SearchUserFragment.newInstance();
                    if (position == 3) return SearchLiveFragment.newInstance();
                    throw new IllegalArgumentException("return value of getItemCount() method maybe not associate with argument position");
                }
            };
            viewPager.setAdapter(vpfAdapter);
        /*
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                onScrolled(6);
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        });
         */
            searchBtn.setOnClickListener(view -> searchKeyword(keywordInput.getText().toString()));
            searchBtn.setOnLongClickListener(this::jumpToTargetId);
            keywordInput.setOnEditorActionListener((textView, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction()) {
                    searchKeyword(keywordInput.getText().toString());
                }
                return false;
            });

            try {
                searchHistory = JsonUtil.jsonToArrayList(new JSONArray(SharedPreferencesUtil.getString(SharedPreferencesUtil.search_history, "[]")), false);
            } catch (JSONException e) {
                runOnUiThread(() -> MsgUtil.err(e, this));
                searchHistory = new ArrayList<>();
            }
            searchHistoryAdapter = new SearchHistoryAdapter(this, searchHistory);
            searchHistoryAdapter.setOnClickListener(position -> keywordInput.setText(searchHistory.get(position)));
            searchHistoryAdapter.setOnLongClickListener(position -> {
                MsgUtil.showMsg("删除成功", this);
                searchHistory.remove(position);
                searchHistoryAdapter.notifyItemRemoved(position);
                searchHistoryAdapter.notifyItemRangeChanged(position, searchHistory.size() - position);
                SharedPreferencesUtil.putString(SharedPreferencesUtil.search_history, new JSONArray(searchHistory).toString());
            });
            historyRecyclerview.setLayoutManager(new LinearLayoutManager(this));
            historyRecyclerview.setAdapter(searchHistoryAdapter);


            if (getIntent().getStringExtra("keyword") != null) {
                findViewById(R.id.top).setOnClickListener(view1 -> finish());
                keywordInput.setText(getIntent().getStringExtra("keyword"));
                MsgUtil.showMsg("可点击标题栏返回详情页", this);
            }
        });
    }

    public boolean jumpToTargetId(View view) {
        String text = keywordInput.getText().toString();
        LinkUrlUtil.handleId(this, text);
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void searchKeyword(String str) {
        if (str.contains("Robin") || str.contains("robin")) {
            if (str.contains("撅")) {
                MsgUtil.showText(this, "特殊彩蛋", getString(R.string.egg_special));
                return;
            }
            if (str.contains("纳西妲")) {
                MsgUtil.showText(this, "特殊彩蛋", getString(R.string.egg_robin_nahida));
                return;
            }
        }

        if (!refreshing) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View curFocus;
            if ((curFocus = getCurrentFocus()) != null) {
                manager.hideSoftInputFromWindow(curFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            if (str.isEmpty()) {
                runOnUiThread(() -> MsgUtil.showMsg("还没输入内容喵~", this));
            } else if (Objects.equals(lastKeyword, str)) {
                runOnUiThread(() -> {
                    keywordInput.clearFocus();
                    historyRecyclerview.setVisibility(View.GONE);
                });
            } else {
                refreshing = true;
                lastKeyword = str;

                //搜索记录
                runOnUiThread(() -> {
                    historyRecyclerview.setVisibility(View.GONE);
                    keywordInput.clearFocus();
                });

                if (!searchHistory.contains(str)) {
                    try {
                        searchHistory.add(0, str);
                        SharedPreferencesUtil.putString(SharedPreferencesUtil.search_history, new JSONArray(searchHistory).toString());
                        runOnUiThread(() -> {
                            searchHistoryAdapter.notifyItemInserted(0);
                            searchHistoryAdapter.notifyItemRangeChanged(0, searchHistory.size());
                            historyRecyclerview.scrollToPosition(0);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> MsgUtil.err(e, this));
                    }
                } else {
                    try {
                        int pos = searchHistory.indexOf(str);
                        searchHistory.remove(str);
                        searchHistory.add(0, str);
                        SharedPreferencesUtil.putString(SharedPreferencesUtil.search_history, new JSONArray(searchHistory).toString());
                        runOnUiThread(() -> {
                            searchHistoryAdapter.notifyItemMoved(pos, 0);
                            searchHistoryAdapter.notifyItemRangeChanged(0, searchHistory.size());
                            historyRecyclerview.scrollToPosition(0);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> MsgUtil.err(e, this));
                    }
                }

                try {
                    for (int i = 0; i < 4; i++) {
                        //从viewpager中拿真正added的fragment的方法: tag = "f{position}", 得到的fragment将会是真实存在的
                        Fragment fragmentById = getSupportFragmentManager().findFragmentByTag("f" + i);
                        if (fragmentById != null) {
                            ((SearchRefreshable) fragmentById).refresh(str);
                        }
                    }
                    refreshing = false;
                } catch (Exception e) {
                    refreshing = false;
                    runOnUiThread(() -> MsgUtil.err(e, this));
                }
            }
        }
    }

    public void onScrolled(int dy) {
        float height = searchBar.getHeight() + ToolsUtil.dp2px(2f, this);

        if (System.currentTimeMillis() - animate_last > 200) {
            if (dy > 0 && searchBarVisible) {
                animate_last = System.currentTimeMillis();
                this.searchBarVisible = false;
                @SuppressLint("ObjectAnimatorBinding") ObjectAnimator animator = ObjectAnimator.ofFloat(searchBar, "translationY", 0, -height);
                animator.start();
                handler.postDelayed(() -> searchBar.setVisibility(View.GONE), 200);
            }
            if (dy < -1 && !searchBarVisible) {
                animate_last = System.currentTimeMillis();
                this.searchBarVisible = true;
                searchBar.setVisibility(View.VISIBLE);
                @SuppressLint("ObjectAnimatorBinding") ObjectAnimator animator = ObjectAnimator.ofFloat(searchBar, "translationY", -height, 0);
                animator.start();
            }
        }
    }

}