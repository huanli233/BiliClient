package com.RobinNotBad.BiliClient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.ViewPagerImageAdapter;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
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
        setContentView(R.layout.activity_image_viewer);
        Intent intent = getIntent();
        ArrayList<String> imageList = intent.getStringArrayListExtra("imageList");

        ViewPager viewPager = findViewById(R.id.viewPager);
        findViewById(R.id.top).setOnClickListener(view -> finish());

        List<PhotoView> photoViewList = new ArrayList<>();

        for (int i = 0; i < imageList.size(); i++) {
            PhotoView photoView = new PhotoView(this);
            try {
                Glide.with(this).load(imageList.get(i) + "@80q.webp")  //让b站自己压缩一下以加速获取
                        .override(Target.SIZE_ORIGINAL)//override这一项一定要加，这样才会显示原图，不然一放大就糊成使
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(photoView);
            }catch (OutOfMemoryError e){
                e.printStackTrace();
                MsgUtil.toast("内存溢出哩（悲",this);
            }

            int id = i;
            photoView.setOnLongClickListener(view -> {
                if(longClickPosition != id){
                    Toast.makeText(this, "再次长按下载图片", Toast.LENGTH_SHORT).show();
                    longClickPosition = id;
                }
                else{
                    Intent intent1 = new Intent()
                            .setClass(ImageViewerActivity.this,DownloadActivity.class)
                            .putExtra("link",imageList.get(id))
                            .putExtra("type",0)
                            .putExtra("title", LittleToolsUtil.getFileNameFromLink(imageList.get(id)));
                    startActivity(intent1);
                }
                return true;
            });

            photoViewList.add(photoView);

        }

        ViewPagerImageAdapter vpiAdapter = new ViewPagerImageAdapter(photoViewList);

        viewPager.setAdapter(vpiAdapter);
    }


}