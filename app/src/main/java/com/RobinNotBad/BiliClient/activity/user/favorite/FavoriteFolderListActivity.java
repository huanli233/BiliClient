package com.RobinNotBad.BiliClient.activity.user.favorite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.user.favorite.FavouriteOpusListActivity;
import com.RobinNotBad.BiliClient.adapter.favorite.FavoriteFolderAdapter;
import com.RobinNotBad.BiliClient.api.FavoriteApi;
import com.RobinNotBad.BiliClient.model.FavoriteFolder;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;

//收藏夹列表
//2023-08-07

public class FavoriteFolderListActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ImageView ArticleFavCover;
    private TextView ArticleFavTitle;
    private TextView ArticleFavAmount;
    private MaterialCardView ArticleFavFolder;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav_folders);

        long mid = SharedPreferencesUtil.getLong("mid",0);

        recyclerView = findViewById(R.id.recyclerView);
        ArticleFavCover = findViewById(R.id.cover);
        ArticleFavTitle = findViewById(R.id.title);
        ArticleFavFolder = findViewById(R.id.opus_folder);
        ((TextView)findViewById(R.id.itemCount)).setText("");
        
        ArticleFavTitle.setText("图文收藏夹");
        Glide.with(this).asDrawable().load(getResources().getDrawable(R.drawable.article_fav_cover))
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5,this))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(ArticleFavCover);
        ArticleFavFolder.setOnClickListener(v->{
            Intent intent = new Intent(this,FavouriteOpusListActivity.class);
            startActivity(intent);
        });
        
        setPageName("收藏");

        CenterThreadPool.run(()->{
            try {
                ArrayList<FavoriteFolder> folderList = FavoriteApi.getFavoriteFolders(mid);
                FavoriteFolderAdapter adapter = new FavoriteFolderAdapter(this,folderList,mid);
                runOnUiThread(()->{
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                });
            } catch (Exception e) {report(e);}
        });
        
    }
}