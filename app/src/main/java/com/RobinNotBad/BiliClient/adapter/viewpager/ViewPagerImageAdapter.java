package com.RobinNotBad.BiliClient.adapter.viewpager;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

//图片查看专用Adapter

public class ViewPagerImageAdapter extends PagerAdapter {

    private final List<PhotoView> photoViewList;

    public ViewPagerImageAdapter(List<PhotoView> photoViewList) {
        this.photoViewList = photoViewList;
    }

    @Override
    public int getCount() {
        return photoViewList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        PhotoView photoView = photoViewList.get(position);
        container.addView(photoView);
        return photoView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
