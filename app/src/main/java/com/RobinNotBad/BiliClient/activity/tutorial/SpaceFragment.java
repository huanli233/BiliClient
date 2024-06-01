package com.RobinNotBad.BiliClient.activity.tutorial;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.RobinNotBad.BiliClient.R;

public class SpaceFragment extends Fragment {
    public SpaceFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial_space, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.argb(190,230,42,42));

        TextView text1_view = view.findViewById(R.id.text1);
        SpannableString text1 = new SpannableString("这是个人主页，在登录后，未关注的用户会显示登录按钮，关注过的用户会显示取关和私信按钮\n这个页面也可以左右滑动\n第一页是动态列表\n第二页是投稿的视频列表\n第三页是投稿的专栏列表");
        //多个加粗只能每次new一个StyleSpan
        text1.setSpan(new StyleSpan(Typeface.BOLD),7,55, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        text1.setSpan(foregroundColorSpan,47,55,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        text1_view.setText(text1);

        TextView text2_view = view.findViewById(R.id.text2);
        SpannableString text2 = new SpannableString("注意：如果未登录或账号被风控，打开此页面时可能会报错或崩溃");
        //多个加粗只能每次new一个StyleSpan
        text2.setSpan(new StyleSpan(Typeface.BOLD),5,15, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        text2_view.setText(text2);
    }
}
