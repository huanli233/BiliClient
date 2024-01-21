package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.SplashActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
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
        Log.e("debug","使用特殊登录方式");

        findViewById(R.id.top).setOnClickListener(view -> finish());

        textInput = findViewById(R.id.loginInput);
        MaterialCardView confirm = findViewById(R.id.confirm);
        TextView desc = findViewById(R.id.desc);

        Intent intent= getIntent();

        if(intent.getBooleanExtra("login",true)) {
            confirm.setOnClickListener(view -> {
                String loginInfo = textInput.getText().toString();
                try {
                    JSONObject jsonObject = new JSONObject(loginInfo);
                    String cookies = jsonObject.getString("cookies");
                    SharedPreferencesUtil.putLong(SharedPreferencesUtil.mid, Long.parseLong(NetWorkUtil.getInfoFromCookie("DedeUserID", cookies)));
                    SharedPreferencesUtil.putString(SharedPreferencesUtil.csrf, NetWorkUtil.getInfoFromCookie("bili_jct", cookies));
                    SharedPreferencesUtil.putString(SharedPreferencesUtil.cookies, cookies);
                    SharedPreferencesUtil.putString(SharedPreferencesUtil.refresh_token,jsonObject.getString("refresh_token"));
                    runOnUiThread(() -> Toast.makeText(SpecialLoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show());
                    SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.setup, true);

                    SharedPreferencesUtil.removeValue(SharedPreferencesUtil.cookie_refresh);

                    ConfInfoApi.refreshHeaders();

                    Intent intent1 = new Intent();
                    intent1.setClass(SpecialLoginActivity.this, SplashActivity.class);
                    startActivity(intent1);
                    finish();
                } catch (JSONException e) {
                    runOnUiThread(() -> MsgUtil.quickErr(MsgUtil.err_json, SpecialLoginActivity.this));
                    e.printStackTrace();
                }
            });
        }
        else{
            desc.setText("以下是你的登录信息。此功能是为低版本安卓用户无法登录的问题准备，如果你是意外进入此页面，请退出。\n同时，请勿将此页面的信息传给别人，以防盗号！");
            confirm.setVisibility(View.GONE);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("cookies",SharedPreferencesUtil.getString("cookies",""));
                jsonObject.put("refresh_token",SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token,""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            textInput.setText(jsonObject.toString());
        }
    }

}