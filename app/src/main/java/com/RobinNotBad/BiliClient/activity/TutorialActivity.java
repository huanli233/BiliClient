package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.utils.widget.ImageFilterView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.model.Tutorial;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class TutorialActivity extends BaseActivity {
    private int wait_time = 3;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_tutorial, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            setTopbarExit();

            Intent intent = getIntent();

            Tutorial tutorial = Objects.requireNonNull(TutorialHelper.loadTutorial(getResources().getXml(intent.getIntExtra("xml_id", R.xml.tutorial_recommend))));

            ((TextView) findViewById(R.id.title)).setText(tutorial.name);
            ((TextView) findViewById(R.id.content)).setText(TutorialHelper.loadText(tutorial.content));

            try {
                if (tutorial.imgid != null) {
                    int indentify = getResources().getIdentifier(getPackageName() + ":" + tutorial.imgid, null, null);
                    if (indentify > 0)
                        ((ImageFilterView) findViewById(R.id.image_view)).setImageDrawable(getResources().getDrawable(indentify));
                } else findViewById(R.id.image_view).setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MaterialButton close_btn = findViewById(R.id.close_btn);
            close_btn.setEnabled(false);
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        if (wait_time > 0) {
                            close_btn.setText(String.format(Locale.getDefault(), "已阅(%ds)", wait_time));
                            close_btn.setEnabled(false);
                            wait_time--;
                        } else {
                            close_btn.setText("已阅");
                            close_btn.setEnabled(true);
                            timer.cancel();
                        }
                    });
                }
            }, 0, 1000);
            close_btn.setOnClickListener(view -> {
                SharedPreferencesUtil.putInt("tutorial_ver_" + intent.getStringExtra("tag"), intent.getIntExtra("version", -1));
                finish();
            });
        });
    }

    @Override
    public void onBackPressed() {
    }
}
