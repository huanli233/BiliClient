package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.SplashActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

public class SpecialLoginActivity extends BaseActivity {

    private EditText textInput;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_special);
        Log.e("debug", "使用特殊登录方式");

        textInput = findViewById(R.id.loginInput);
        MaterialCardView confirm = findViewById(R.id.confirm);
        TextView desc = findViewById(R.id.desc);

        Intent intent = getIntent();

        if (intent.getBooleanExtra("login", true)) {
            confirm.setOnClickListener(view -> {
                String loginInfo = textInput.getText().toString();
                try {
                    JSONObject jsonObject = new JSONObject(loginInfo);
                    String cookies = jsonObject.getString("cookies");
                    SharedPreferencesUtil.putLong(SharedPreferencesUtil.mid, Long.parseLong(NetWorkUtil.getInfoFromCookie("DedeUserID", cookies)));
                    SharedPreferencesUtil.putString(SharedPreferencesUtil.csrf, NetWorkUtil.getInfoFromCookie("bili_jct", cookies));
                    SharedPreferencesUtil.putString(SharedPreferencesUtil.cookies, cookies);
                    SharedPreferencesUtil.putString(SharedPreferencesUtil.refresh_token, jsonObject.getString("refresh_token"));
                    runOnUiThread(() -> MsgUtil.toast("登录成功！", this));
                    SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.setup, true);

                    NetWorkUtil.refreshHeaders();

                    Intent intent1 = new Intent();
                    intent1.setClass(SpecialLoginActivity.this, SplashActivity.class);
                    startActivity(intent1);
                    finish();
                } catch (JSONException e) {
                    runOnUiThread(() -> MsgUtil.err(e, SpecialLoginActivity.this));
                }
            });
        } else {
            desc.setText(R.string.special_login_export);
            TextView buttonText = findViewById(R.id.confirm_text);
            buttonText.setText("复制");
            buttonText.setCompoundDrawables(null, null, null, null);
            buttonText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("cookies", SharedPreferencesUtil.getString("cookies", ""));
                jsonObject.put("refresh_token", SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            textInput.setText(jsonObject.toString());
            textInput.clearFocus();
            confirm.setOnClickListener((view) -> {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", textInput.getText());
                clipboardManager.setPrimaryClip(clipData);
                MsgUtil.toast("已复制", this);
            });
        }
    }

}