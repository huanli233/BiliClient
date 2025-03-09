package com.RobinNotBad.BiliClient.activity.settings.login;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
        MaterialCardView refuse = findViewById(R.id.refuse);
        TextView desc = findViewById(R.id.desc);

        Intent intent = getIntent();

        if (intent.getBooleanExtra("login", true)) {
            refuse.setOnClickListener(v -> {
                if (getIntent().getBooleanExtra("from_setup", false))
                    startActivity(new Intent(this, SplashActivity.class));
                else finish();
            });

            confirm.setOnClickListener(view -> {
                String loginInfo = textInput.getText().toString();
                try {
                    JSONObject jsonObject = new JSONObject(loginInfo);
                    String cookies = jsonObject.getString("cookies");
                    SharedPreferencesUtil.putLong(SharedPreferencesUtil.mid, Long.parseLong(NetWorkUtil.getInfoFromCookie("DedeUserID", cookies)));
                    SharedPreferencesUtil.putString(SharedPreferencesUtil.csrf, NetWorkUtil.getInfoFromCookie("bili_jct", cookies));
                    SharedPreferencesUtil.putString(SharedPreferencesUtil.cookies, cookies);
                    SharedPreferencesUtil.putString(SharedPreferencesUtil.refresh_token, jsonObject.getString("refresh_token"));
                    runOnUiThread(() -> MsgUtil.showMsg("登录成功！"));
                    SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.setup, true);

                    NetWorkUtil.refreshHeaders();

                    Intent intent1 = new Intent();
                    intent1.setClass(SpecialLoginActivity.this, SplashActivity.class);
                    startActivity(intent1);
                    finish();
                } catch (JSONException e) {
                    runOnUiThread(() -> MsgUtil.showMsg("请检查输入的内容，不要有多余空格或字符"));
                }
            });
        } else {
            desc.setText(R.string.special_login_export);
            TextView buttonText = findViewById(R.id.confirm_text);
            buttonText.setText("复制");
            buttonText.setCompoundDrawables(null, null, null, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                buttonText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("cookies", SharedPreferencesUtil.getString("cookies", ""));
                jsonObject.put("refresh_token", SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            textInput.setText(jsonObject.toString());
            textInput.clearFocus();
            refuse.setVisibility(View.GONE);
            confirm.setOnClickListener((view) -> {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", textInput.getText());
                clipboardManager.setPrimaryClip(clipData);
                MsgUtil.showMsg("已复制");
            });
        }
    }

}