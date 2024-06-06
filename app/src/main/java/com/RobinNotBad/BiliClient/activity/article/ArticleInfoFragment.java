package com.RobinNotBad.BiliClient.activity.article;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cvid = getArguments().getLong("cvid");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_article_info, container, false);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);

        ImageButton like = view.findViewById(R.id.btn_like);
        ImageButton coin = view.findViewById(R.id.btn_coin);
        TextView likeLabel = view.findViewById(R.id.like_label);
        TextView coinLabel = view.findViewById(R.id.coin_label);
        TextView favLabel = view.findViewById(R.id.fav_label);
        ImageButton fav = view.findViewById(R.id.btn_fav);

        //开始解析内容
        lineList = new ArrayList<>();

        CenterThreadPool.run(()-> {
            try {
                articleInfo = ArticleApi.getArticle(cvid);

                if (articleInfo == null) {
                    requireActivity().runOnUiThread(() -> MsgUtil.toast("获取信息失败！\n可能是专栏不存在？", requireContext()));
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
                    ArticleContentAdapter adapter = new ArticleContentAdapter(requireContext(),articleInfo,lineList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    recyclerView.setAdapter(adapter);

                    likeLabel.setText(ToolsUtil.toWan(articleInfo.stats.like));
                    coinLabel.setText(ToolsUtil.toWan(articleInfo.stats.coin));
                    favLabel.setText(ToolsUtil.toWan(articleInfo.stats.favorite));
                });

                like.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    try {
                        int result = ArticleApi.like(articleInfo.id, !articleInfo.stats.liked);
                        if (result == 0) {
                            articleInfo.stats.liked = !articleInfo.stats.liked;
                            if (isAdded()) requireActivity().runOnUiThread(() -> {
                                MsgUtil.toast((articleInfo.stats.liked ? "点赞成功" : "取消成功"), requireContext());

                                if (articleInfo.stats.liked) likeLabel.setText(ToolsUtil.toWan(++articleInfo.stats.like));
                                else likeLabel.setText(ToolsUtil.toWan(--articleInfo.stats.like));
                                like.setBackground(ContextCompat.getDrawable(requireContext(), (articleInfo.stats.liked ? R.drawable.icon_like_1 : R.drawable.icon_like_0)));
                            });
                        } else if (isAdded())
                            requireActivity().runOnUiThread(() -> MsgUtil.toast("操作失败：" + result, requireContext()));
                    } catch (Exception e) {
                        if (isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
                    }
                }));

                coin.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    if (articleInfo.stats.coined < articleInfo.stats.allow_coin) {
                        try {
                            int result = ArticleApi.addCoin(articleInfo.id, articleInfo.upInfo.mid, 1);
                            if (result == 0) {
                                articleInfo.stats.coined++;
                                if (isAdded()) requireActivity().runOnUiThread(() -> {
                                    MsgUtil.toast("投币成功！", requireContext());
                                    coinLabel.setText(ToolsUtil.toWan(++articleInfo.stats.coin));
                                    coin.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_coin_1));
                                });
                            } else if (isAdded()) {
                                String msg = "投币失败：" + result;
                                switch (result) {
                                    case 34002:
                                        msg = "不能给自己投币哦！";
                                }
                                String finalMsg = msg;
                                requireActivity().runOnUiThread(() -> MsgUtil.toast(finalMsg, requireContext()));
                            }
                        } catch (Exception e) {
                            if (isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
                        }
                    } else {
                        if (isAdded())
                            requireActivity().runOnUiThread(() -> MsgUtil.toast("投币数量到达上限", requireContext()));
                    }
                }));

                fav.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    try {
                        if (articleInfo.stats.favoured) {
                            ArticleApi.delFavorite(articleInfo.id);
                            requireActivity().runOnUiThread(() -> fav.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_favourite_0)));
                            articleInfo.stats.favorite--;
                        } else {
                            ArticleApi.favorite(articleInfo.id);
                            requireActivity().runOnUiThread(() -> fav.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_favourite_1)));
                            articleInfo.stats.favorite++;
                        }
                        articleInfo.stats.favoured = !articleInfo.stats.favoured;
                        requireActivity().runOnUiThread(() -> {
                            favLabel.setText(ToolsUtil.toWan(articleInfo.stats.favorite));
                            MsgUtil.toast("操作成功~", requireContext());
                        });
                    } catch (IOException e) {
                        if (isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
                    }
                }));

                try {
                    articleInfo.stats = ArticleApi.getArticleViewInfo(articleInfo.id).stats;
                    articleInfo.stats.allow_coin = 1;
                    if(isAdded()) requireActivity().runOnUiThread(()->{
                        if(articleInfo.stats.coined!=0) coin.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_coin_1));
                        if(articleInfo.stats.liked) like.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_like_1));
                        if(articleInfo.stats.favoured) fav.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_favourite_1));
                    });
                } catch (Exception e) {
                    if (isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
                }

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
