package com.RobinNotBad.BiliClient.activity.article;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.article.ArticleContentAdapter;
import com.RobinNotBad.BiliClient.api.ArticleApi;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.ArticleLine;
import com.RobinNotBad.BiliClient.ui.widget.recycler.CustomLinearManager;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.JsonUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.Result;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.TerminalContext;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

public class ArticleInfoFragment extends Fragment {
    private static final String TAG = "ArticleInfoFragment";
    ArticleInfo articleInfo;
    long cvid;
    RecyclerView recyclerView;
    ArrayList<ArticleLine> lineList;

    Runnable onFinishLoad;

    public ArticleInfoFragment() {
    }

    public static ArticleInfoFragment newInstance(long cvid) {
        ArticleInfoFragment fragment = new ArticleInfoFragment();
        Bundle args = new Bundle();
        args.putLong("cvid", cvid);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnFinishLoad(Runnable onFinishLoad) {
        this.onFinishLoad = onFinishLoad;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cvid = getArguments().getLong("cvid");
        }
        Result<ArticleInfo> articleInfoResult = TerminalContext.getInstance().getArticleInfoByCvId(cvid).getValue();
        if (articleInfoResult == null) {
            articleInfoResult = Result.failure(new TerminalContext.IllegalTerminalStateException("articleInfoResult is null"));
        }
        articleInfoResult
                .onSuccess((articleInfo) -> this.articleInfo = articleInfo)
                .onFailure((e) -> {
                    Log.wtf(TAG, e);
                    MsgUtil.showMsg("找不到专栏信息QAQ");
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);

        if(SharedPreferencesUtil.getBoolean("ui_landscape",false)) {
            WindowManager windowManager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            if(Build.VERSION.SDK_INT >= 17) display.getRealMetrics(metrics);
            else display.getMetrics(metrics);
            int paddings = metrics.widthPixels / 6;
            recyclerView.setPadding(paddings,0,paddings,0);
        }

        //开始解析内容
        lineList = new ArrayList<>();

        CenterThreadPool.run(() -> {
            try {
                if (articleInfo == null) articleInfo = ArticleApi.getArticle(cvid);

                if (articleInfo == null) {
                    if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) MsgUtil.showMsg("登录后再尝试");
                    else MsgUtil.showMsg("获取信息失败！\n可能是专栏不存在？");
                    requireActivity().finish();
                    return;
                }

                //专栏分为html和json两种格式
                if (articleInfo.content.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(articleInfo.content);
                    for (int i = 0; i < jsonObject.getJSONArray("ops").length(); i++) { //遍历Array
                        JSONObject element = jsonObject.getJSONArray("ops").getJSONObject(i);
                        if (element.has("insert")) {
                            if (element.get("insert") instanceof JSONObject) { //有图片
                                lineList.add(new ArticleLine(1, JsonUtil.searchString(element.getJSONObject("insert"), "url", ""), ""));
                            } else { //纯文字
                                lineList.add(new ArticleLine(0, element.getString("insert"), ""));
                            }
                        }
                    }
                } else {
                    Document document = Jsoup.parse(articleInfo.content);
                    loadContentHtml(document.select("body").get(0));
                }
                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    ArticleContentAdapter adapter = new ArticleContentAdapter(requireActivity(), articleInfo, lineList);
                    recyclerView.setLayoutManager(new CustomLinearManager(requireContext()));
                    recyclerView.setAdapter(adapter);
                    if (onFinishLoad != null) onFinishLoad.run();
                });
            } catch (Exception e) {MsgUtil.err(e);}
        });
    }

    private void loadContentHtml(Element element) {
        for (Element e : element.children()) {
            if (e.is("p")) {
                lineList.add(new ArticleLine(0, e.text(), ""));
            } else if (e.is("strong")) {
                lineList.add(new ArticleLine(0, e.text(), "strong"));
            } else if (e.is("br")) {
                lineList.add(new ArticleLine(0, "", "br"));
            } else if (e.is("img")) {
                lineList.add(new ArticleLine(1, "http:" + e.attr("src"), ""));
            } else loadContentHtml(e);
        }
    }
}
