package com.RobinNotBad.BiliClient.activity.tutorial;

import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.video.RecommendActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;

public class OtherFragment extends Fragment {
    public OtherFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial_other, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MaterialButton)view.findViewById(R.id.ok_btn)).setOnClickListener(view1 -> {
            Toast.makeText(getContext().getApplicationContext(),"未来将不会再显示教程",Toast.LENGTH_SHORT).show();
            SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.tutorial_finished,true);
            Intent intent = new Intent(requireContext(), RecommendActivity.class);
            requireContext().startActivity(intent);
            requireActivity().finish();
        });
    }
}
