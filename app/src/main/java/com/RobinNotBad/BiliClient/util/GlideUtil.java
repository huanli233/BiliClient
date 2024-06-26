package com.RobinNotBad.BiliClient.util;


import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

public class GlideUtil {
    public static final int QUALITY_HIGH = 80;
    public static final int QUALITY_LOW = 15;

    public static String url(String url){
        if(url.endsWith("gif") || url.endsWith("webp") || url.contains("afdiancdn.com"))
            return url;
        return url + "@" + QUALITY_LOW + "q.webp";
    }

    public static String url_hq(String url){
        if(url.endsWith("gif") || url.endsWith("webp"))
            return url;
        return url + "@" + QUALITY_HIGH + "q.webp";
    }

    public static void request(ImageView view, String url, int placeholder){
        Glide.with(view).asDrawable().load(url(url))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .format(DecodeFormat.PREFER_RGB_565)
                .transition(GlideUtil.getTransitionOptions())
                .placeholder(placeholder)
                .into(view);
    }

    public static void requestRound(ImageView view, String url, int placeholder){
        Glide.with(view).asDrawable().load(url(url))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .format(DecodeFormat.PREFER_RGB_565)
                .transition(GlideUtil.getTransitionOptions())
                .placeholder(placeholder)
                .apply(RequestOptions.circleCropTransform())
                .into(view);
    }

    public static void request(ImageView view, String url, int roundCorners, int placeholder){
        Glide.with(view).asDrawable().load(url(url))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .format(DecodeFormat.PREFER_RGB_565)
                .transition(GlideUtil.getTransitionOptions())
                .placeholder(placeholder)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(roundCorners, view.getContext()))))
                .into(view);
    }

    public static TransitionOptions<?, ? super Drawable> getTransitionOptions() {
        if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.IMAGE_LOAD_TRANSITION, true)) {
            return DrawableTransitionOptions.with(new DrawableCrossFadeFactory.Builder(300).setCrossFadeEnabled(true).build());
        } else {
            return new DrawableTransitionOptions();
        }
    }
}
