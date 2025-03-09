package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.viewpager.ViewPagerImageAdapter;
import com.RobinNotBad.BiliClient.ui.widget.PhotoViewpager;
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

    private int longClickPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_BiliClient);
        setContentView(R.layout.activity_image_viewer);
        Intent intent = getIntent();
        ArrayList<String> imageList = intent.getStringArrayListExtra("imageList");

        PhotoViewpager viewPager = findViewById(R.id.viewPager);
        TextView textView = findViewById(R.id.text_page);

        List<PhotoView> photoViewList = new ArrayList<>();

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
            }

            int id = i;
            photoView.setOnLongClickListener(view -> {
                if (longClickPosition != id) {
                    MsgUtil.showMsg("再次长按下载图片");
                    longClickPosition = id;
                } else {
                    Intent intent1 = new Intent()
                            .setClass(ImageViewerActivity.this, DownloadActivity.class)
                            .putExtra("link", imageList.get(id))
                            .putExtra("type", 0);
                    startActivity(intent1);
                    longClickPosition = -1;
                }
                return true;
            });

            photoViewList.add(photoView);

        }

        ViewPagerImageAdapter vpiAdapter = new ViewPagerImageAdapter(photoViewList);

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