package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

public class SetupUIActivity extends BaseActivity {

    private EditText uiScaleInput,uiPaddingH,uiPaddingV;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_ui);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        uiScaleInput = findViewById(R.id.ui_scale_input);
        uiScaleInput.setText(String.valueOf(SharedPreferencesUtil.getFloat("dpi",1.0F)));

        uiPaddingH = findViewById(R.id.ui_padding_horizontal);
        uiPaddingH.setText(String.valueOf(SharedPreferencesUtil.getInt("paddingH_percent",0)));
        uiPaddingV = findViewById(R.id.ui_padding_vertical);
        uiPaddingV.setText(String.valueOf(SharedPreferencesUtil.getInt("paddingV_percent",0)));

        MaterialCardView preview = findViewById(R.id.preview);
        MaterialCardView confirm = findViewById(R.id.confirm);

        preview.setOnClickListener(view -> {
            save();
            Intent intent = new Intent();
            intent.setClass(SetupUIActivity.this, UIPreviewActivity.class);
            startActivity(intent);
        });

        confirm.setOnClickListener(view -> {
            save();
            Intent intent = new Intent();
            intent.setClass(SetupUIActivity.this, IntroductionActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void save() {
        if(!uiScaleInput.getText().toString().isEmpty()) {
            float dpiTimes = Float.parseFloat(uiScaleInput.getText().toString());
            if (dpiTimes >= 0.1F && dpiTimes <= 10.0F)
                SharedPreferencesUtil.putFloat("dpi", dpiTimes);
            Log.e("dpi", uiScaleInput.getText().toString());
        }

        if(!uiPaddingH.getText().toString().isEmpty()) {
            int paddingH = Integer.parseInt(uiPaddingH.getText().toString());
            if(paddingH <= 30) SharedPreferencesUtil.putInt("paddingH_percent", paddingH);
            Log.e("paddingH", uiPaddingH.getText().toString());
        }

        if(!uiPaddingV.getText().toString().isEmpty()) {
            int paddingV = Integer.parseInt(uiPaddingV.getText().toString());
            if(paddingV <= 30) SharedPreferencesUtil.putInt("paddingV_percent", paddingV);
            Log.e("paddingV", uiPaddingV.getText().toString());
        }
    }
}