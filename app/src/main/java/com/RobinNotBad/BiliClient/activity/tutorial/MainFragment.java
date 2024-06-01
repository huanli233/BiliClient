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

public class MainFragment extends Fragment {
    public MainFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.argb(190,230,42,42));


        TextView text1_view = view.findViewById(R.id.text1);
        SpannableString text1 = new SpannableString("为了避免新用户不熟悉操作，导致错过某些功能，我们做了这个教程页面\n\n现在，从右往左滑动屏幕，查看下一项教程吧\n\n如果你执意跳过教程，我们将不会对造成的功能使用问题进行任何帮助");
        text1.setSpan(styleSpan,36,43, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        text1.setSpan(foregroundColorSpan,55,87,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        text1_view.setText(text1);
    }
}
