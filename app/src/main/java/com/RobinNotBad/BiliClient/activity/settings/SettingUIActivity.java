package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.EditText;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingUIActivity extends BaseActivity {

    private EditText uiScaleInput, uiPaddingH, uiPaddingV, density_input;

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_setting_ui, null, (layoutView, resId, parent) -> {
            setContentView(R.layout.activity_setting_ui);
            setTopbarExit();
            Log.e("debug", "进入界面设置");

            uiScaleInput = findViewById(R.id.ui_scale_input);
            uiScaleInput.setText(String.valueOf(SharedPreferencesUtil.getFloat("dpi", 1.0F)));

            uiPaddingH = findViewById(R.id.ui_padding_horizontal);
            uiPaddingH.setText(String.valueOf(SharedPreferencesUtil.getInt("paddingH_percent", 0)));
            uiPaddingV = findViewById(R.id.ui_padding_vertical);
            uiPaddingV.setText(String.valueOf(SharedPreferencesUtil.getInt("paddingV_percent", 0)));

            density_input = findViewById(R.id.density_input);
            int density = SharedPreferencesUtil.getInt("density", -1);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            density_input.setText(String.valueOf((density == -1 ? displayMetrics.densityDpi + "(默认)" : density)));

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
                intent.setClass(SettingUIActivity.this, UIPreviewActivity.class);
                startActivity(intent);
            });
            findViewById(R.id.reset).setOnClickListener(view -> {
                SharedPreferencesUtil.putInt("paddingH_percent", 0);
                SharedPreferencesUtil.putInt("paddingV_percent", 0);
                SharedPreferencesUtil.putFloat("dpi", 1.0f);
                SharedPreferencesUtil.putInt("density", -1);
                SharedPreferencesUtil.putBoolean("player_ui_round",false);
                uiScaleInput.setText("1.0");
                uiPaddingH.setText("0");
                uiPaddingV.setText("0");
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                density_input.setText(displayMetrics.densityDpi + "(默认)");
                round.setChecked(false);
                MsgUtil.showMsg("恢复完成");
            });
        });
    }

    private void save() {
        if (!uiScaleInput.getText().toString().isEmpty()) {
            float dpiScale = Float.parseFloat(uiScaleInput.getText().toString());
            if (dpiScale >= 0.25F && dpiScale <= 5.0F) {
                SharedPreferencesUtil.putFloat("dpi", dpiScale);
                BiliTerminal.DPI_FORCE_CHANGE = true;
                BiliTerminal.context = BiliTerminal.getFitDisplayContext(BiliTerminal.context);
            }
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

        if (!density_input.getText().toString().isEmpty()) {
            try {
                int density = Integer.parseInt(density_input.getText().toString());
                if (density >= 72) SharedPreferencesUtil.putInt("density", density);
            } catch (Throwable ignored) {
            }
        }

    }

    @Override
    protected void onDestroy() {
        if(uiScaleInput != null) save();
        super.onDestroy();
    }
}