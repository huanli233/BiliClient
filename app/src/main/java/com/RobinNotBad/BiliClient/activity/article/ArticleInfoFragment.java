package com.RobinNotBad.BiliClient.activity.article;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.article.ArticleContentAdapter;
import com.RobinNotBad.BiliClient.api.ArticleApi;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.ArticleLine;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.JsonUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

public class ArticleInfoFragment extends Fragment {
    ArticleInfo articleInfo;
    long cvid;
    RecyclerView recyclerView;
    ArrayList<ArticleLine> lineList;
    public ArticleInfoFragment(){}

    public static ArticleInfoFragment newInstance(long cvid) {
        ArticleInfoFragment fragment = new ArticleInfoFragment();
        Bundle args = new Bundle();
        args.putLong("cvid", cvid);
        fragment.setArguments(args);
        return fragment;
    }

    public static ArticleInfoFragment newInstance(ArticleInfo articleInfo) {
        ArticleInfoFragment fragment = new ArticleInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable("article", articleInfo);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cvid = getArguments().getLong("cvid");
            articleInfo = (ArticleInfo) getArguments().getSerializable("article");
        }
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

        //开始解析内容
        lineList = new ArrayList<>();

        CenterThreadPool.run(()-> {
            try {
                if (articleInfo == null) articleInfo = ArticleApi.getArticle(cvid);

                if (articleInfo == null) {
                    if(SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,0) == 0) requireActivity().runOnUiThread(() -> MsgUtil.toast("登录后再尝试", requireContext()));
                    else requireActivity().runOnUiThread(() -> MsgUtil.toast("获取信息失败！\n可能是专栏不存在？", requireContext()));
                    requireActivity().finish();
                    return;
                }

                //专栏分为html和json两种格式
                if(articleInfo.content.startsWith("{")) {
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
                    ArticleContentAdapter adapter = new ArticleContentAdapter(requireActivity(),articleInfo,lineList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    recyclerView.setAdapter(adapter);
                });
            }  catch (Exception e) {if(isAdded()) requireActivity().runOnUiThread(()->MsgUtil.err(e,requireContext()));}
        });
    }

    private void loadContentHtml(Element element){
        for(Element e : element.children()){
            if (e.is("p")){
                lineList.add(new ArticleLine(0,e.text(),""));
            }
            else if (e.is("strong")){
                lineList.add(new ArticleLine(0,e.text(),"strong"));
            }
            else if (e.is("br")){
                lineList.add(new ArticleLine(0,"","br"));
            }
            else if (e.is("img")){
                lineList.add(new ArticleLine(1,"http:" + e.attr("src"),""));
            }
            else loadContentHtml(e);
        }
    }
}
