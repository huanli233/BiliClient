package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Process;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.model.ApiResult;
import com.RobinNotBad.BiliClient.service.DownloadService;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;

public class CatchActivity extends BaseActivity {
    private boolean openStack = false;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catch);

        TextView reason_view = findViewById(R.id.catch_reason);
        TextView stack_view = findViewById(R.id.stack);
        MaterialButton btn_upload = findViewById(R.id.upload_btn);

        Intent intent = getIntent();
        String stack = intent.getStringExtra("stack");

        stack_view.setText(stack);

        findViewById(R.id.exit_btn).setOnClickListener(view -> System.exit(-1));

        SpannableString reason_str = null;

        if (stack != null) {

            boolean allow_upload = false;

            if (stack.contains("java.lang.NumberFormatException"))
                reason_str = new SpannableString("可能的崩溃原因：\n数值转换出错");
            else if (stack.contains("java.lang.UnsatisfiedLinkError"))
                reason_str = new SpannableString("可能的崩溃原因：\n外部库加载出错，可能设备太老或修改了安装包");
            else if (stack.contains("org.json.JSONException"))
                reason_str = new SpannableString("可能的崩溃原因：\n数据解析错误");
            else if (stack.contains("java.lang.OutOfMemoryError"))
                reason_str = new SpannableString("可能的崩溃原因：\n内存爆了，这在小内存设备上很正常");
            else
                allow_upload = true;

            if(allow_upload) btn_upload.setOnClickListener(view -> {
                btn_upload.setEnabled(false);
                if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, -1) == -1)
                    MsgUtil.toast("我们不对未登录时遇到的问题负责\n——除非它真的经常出现且非常影响使用");
                else {
                    CenterThreadPool.run(() -> {
                        ApiResult res = AppInfoApi.uploadStack(stack, this);
                        runOnUiThread(() -> {
                            if(res.code >= 0) btn_upload.setText("请带着你的报错ID：" + res.code + "\n和你崩溃前进行的操作\n去找开发者\n（提醒：开发者不保证会修好也不保证随时回复你）");
                            else btn_upload.setText(res.message);

                            if (res.code == -1) btn_upload.setEnabled(true);
                        });
                    });
                }
            });
            else btn_upload.setText("此类型报错不可上传\n非特殊情况请勿打扰开发者谢谢喵");

        } else finish();

        findViewById(R.id.restart_btn).setOnClickListener(view -> {
            finish();
            stopService(new Intent(this, DownloadService.class));
            startActivity(new Intent(this, SplashActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            Process.killProcess(Process.myPid());
        });

        if (reason_str != null) {
            reason_str.setSpan(new StyleSpan(Typeface.BOLD), 0, 8, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            reason_view.setText(reason_str);
        } else reason_view.setText("未知的崩溃原因");

        stack_view.setOnClickListener(view -> {
            openStack = !openStack;
            if (openStack) stack_view.setMaxLines(200);
            else stack_view.setMaxLines(5);
        });
    }

    @Override
    protected boolean eventBusEnabled() {
        return false;
    }
}
