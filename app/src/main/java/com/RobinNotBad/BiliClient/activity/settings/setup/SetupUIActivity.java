package com.RobinNotBad.BiliClient.activity.settings.setup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.settings.UIPreviewActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SetupUIActivity extends BaseActivity {

    private EditText uiScaleInput, uiPaddingH, uiPaddingV;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_ui);

        uiScaleInput = findViewById(R.id.ui_scale_input);
        uiScaleInput.setText(String.valueOf(SharedPreferencesUtil.getFloat("dpi", 1.0F)));

        uiPaddingH = findViewById(R.id.ui_padding_horizontal);
        uiPaddingH.setText(String.valueOf(SharedPreferencesUtil.getInt("paddingH_percent", 0)));
        uiPaddingV = findViewById(R.id.ui_padding_vertical);
        uiPaddingV.setText(String.valueOf(SharedPreferencesUtil.getInt("paddingV_percent", 0)));

        SwitchMaterial round = findViewById(R.id.switch_round);
        round.setChecked(SharedPreferencesUtil.getBoolean("player_ui_round",false));
        round.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                uiPaddingH.setText("11");
                uiPaddingV.setText("11");
                SharedPreferencesUtil.putBoolean("player_ui_round",true);
            }
            else{
                uiPaddingH.setText("0");
                uiPaddingV.setText("0");
                SharedPreferencesUtil.putBoolean("player_ui_round",false);
            }
        });

        findViewById(R.id.preview).setOnClickListener(view -> {
            save();
            Intent intent = new Intent();
            intent.setClass(SetupUIActivity.this, UIPreviewActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.confirm).setOnClickListener(view -> {
            save();
            Intent intent = new Intent();
            intent.setClass(SetupUIActivity.this, IntroductionActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.reset).setOnClickListener(view -> {
            SharedPreferencesUtil.putInt("paddingH_percent", 0);
            SharedPreferencesUtil.putInt("paddingV_percent", 0);
            SharedPreferencesUtil.putFloat("dpi", 1.0f);
            SharedPreferencesUtil.putBoolean("player_ui_round",false);
            uiScaleInput.setText("1.0");
            uiPaddingH.setText("0");
            uiPaddingV.setText("0");
            round.setChecked(false);
            MsgUtil.showMsg("恢复完成");
        });
    }

    private void save() {
        if (!uiScaleInput.getText().toString().isEmpty()) {
            float dpiTimes = Float.parseFloat(uiScaleInput.getText().toString());
            if (dpiTimes >= 0.1F && dpiTimes <= 10.0F)
                SharedPreferencesUtil.putFloat("dpi", dpiTimes);
            Log.e("dpi", uiScaleInput.getText().toString());
        }

        if (!uiPaddingH.getText().toString().isEmpty()) {
            int paddingH = Integer.parseInt(uiPaddingH.getText().toString());
            if (paddingH <= 30) SharedPreferencesUtil.putInt("paddingH_percent", paddingH);
            Log.e("paddingH", uiPaddingH.getText().toString());
        }

        if (!uiPaddingV.getText().toString().isEmpty()) {
            int paddingV = Integer.parseInt(uiPaddingV.getText().toString());
            if (paddingV <= 30) SharedPreferencesUtil.putInt("paddingV_percent", paddingV);
            Log.e("paddingV", uiPaddingV.getText().toString());
        }
    }
}