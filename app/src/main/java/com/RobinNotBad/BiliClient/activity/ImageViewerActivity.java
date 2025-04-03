package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.viewpager.ViewPagerViewAdapter;
import com.RobinNotBad.BiliClient.ui.widget.PhotoViewpager;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

public class ImageViewerActivity extends BaseActivity {

    //简简单单的图片查看页面
    //2023-07-21

    private long longClickTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_BiliClient);
        setContentView(R.layout.activity_image_viewer);
        Intent intent = getIntent();
        ArrayList<String> imageList = intent.getStringArrayListExtra("imageList");

        PhotoViewpager viewPager = findViewById(R.id.viewPager);
        TextView textView = findViewById(R.id.text_page);

        List<View> photoViewList = new ArrayList<>();

        ImageButton download = findViewById(R.id.btn_download);
        download.setOnClickListener(v -> {
            long time_now = System.currentTimeMillis();
            if(time_now - longClickTimestamp < 3000){
                Intent intent1 = new Intent(this, DownloadActivity.class)
                        .putExtra("link", imageList.get(viewPager.getCurrentItem()))
                        .putExtra("path", FileUtil.getPicturePath().getAbsolutePath())
                        .putExtra("type", 0);
                startActivity(intent1);
            }
            else MsgUtil.showMsg("再次点击下载");
            longClickTimestamp = time_now;
        });

        for (int i = 0; i < imageList.size(); i++) {
            PhotoView photoView = new PhotoView(this);
            try {
                Glide.with(this).asDrawable()
                        .load(GlideUtil.url_hq(imageList.get(i)))  //让b站自己压缩一下以加速获取
                        .transition(GlideUtil.getTransitionOptions())
                        .override(Target.SIZE_ORIGINAL)//override这一项一定要加，这样才会显示原图，不然一放大就糊成使
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(photoView);
                photoView.setMaximumScale(6.25f);
            } catch (OutOfMemoryError e) {
                MsgUtil.showMsg("超出内存，加载失败");
            } catch (Exception e){
                MsgUtil.err("图片查看", e);
            }

            photoViewList.add(photoView);

        }

        ViewPagerViewAdapter vpiAdapter = new ViewPagerViewAdapter(photoViewList);

        viewPager.setAdapter(vpiAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(positionOffset % 1 == 0)
                    textView.setText("第" + (position + 1) + "/" + imageList.size() + "张");
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


}