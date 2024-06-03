package com.RobinNotBad.BiliClient.activity.tutorial;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.MenuActivity;
import com.RobinNotBad.BiliClient.activity.tutorial.TutorialActivity;
import com.RobinNotBad.BiliClient.activity.video.RecommendActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;
import java.util.Map;
import java.util.Objects;

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
            Toast.makeText(getContext().getApplicationContext(),"教程更新前将不会再显示",Toast.LENGTH_SHORT).show();
            SharedPreferencesUtil.putInt(SharedPreferencesUtil.tutorial_version,TutorialActivity.tutorial_version);
            int firstItemId = -1;

            String sortConf = SharedPreferencesUtil.getString(SharedPreferencesUtil.MENU_SORT, "");
            if (!TextUtils.isEmpty(sortConf)) {
                String[] splitName = sortConf.split(";");
                for (String name : splitName) {
                    if (!MenuActivity.btnNames.containsKey(name)) {
                        for (Map.Entry<String, Pair<String, Integer>> entry : MenuActivity.btnNames.entrySet()) {
                            firstItemId = entry.getValue().second;
                            break;
                        }
                    } else {
                        firstItemId = Objects.requireNonNull(MenuActivity.btnNames.get(name)).second;
                    }
                    break;
                }
            } else {
                for (Map.Entry<String, Pair<String, Integer>> entry : MenuActivity.btnNames.entrySet()) {
                    firstItemId = entry.getValue().second;
                    break;
                }
            }
                
            Intent intent = new Intent();
            Class<?> activityClass = MenuActivity.activityClasses.get(firstItemId);
            intent.setClass(requireContext(), activityClass != null ? activityClass : RecommendActivity.class);
            intent.putExtra("from", R.id.menu_recommend);
            requireContext().startActivity(intent);
                
            requireActivity().finish();
        });
    }
}
