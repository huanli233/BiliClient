package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Process;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.service.DownloadService;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;

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
                reason_str = new SpannableString("可能的崩溃原因：\n外部库加载出错，请不要乱删文件");
            else if (stack.contains("org.json.JSONException"))
                reason_str = new SpannableString("可能的崩溃原因：\n数据解析错误");
            else if (stack.contains("java.lang.OutOfMemoryError"))
                reason_str = new SpannableString("可能的崩溃原因：\n内存爆了，这在小内存设备上很正常");
            else
                allow_upload = true;

            if(allow_upload) btn_upload.setOnClickListener(view -> {
                if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, -1) == -1)
                    MsgUtil.toast("我们不对未登录时遇到的问题负责\n——除非它真的经常出现且非常影响使用");
                else {
                    CenterThreadPool.run(() -> {
                        Pair<Integer,String> res = AppInfoApi.uploadStack(stack, this);
                        runOnUiThread(() -> {
                            if(res.first >= 0) {
                                btn_upload.setText("请带着你的报错ID：" + res.first + "\n去找开发者\n（提醒：开发者不保证会修好也不保证随时回复你）");
                                btn_upload.setEnabled(false);
                            }
                            if(res.first == -114){
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                StringBuilder text = new StringBuilder();
                                text.append("已上传，但应该没什么用\n请带着下面的信息找开发者：");
                                text.append("\n报错类别：");
                                for (int i = 0; i < Math.max(50,stack.length()); i++) {
                                    char c = stack.charAt(i);
                                    if(c == '\n') break;
                                    text.append(c);
                                }
                                text.append("\n上传时间：");
                                text.append(dateFormat.format(System.currentTimeMillis()));
                                text.append("\n（提醒：开发者不保证会修好也不保证随时回复你）");

                                btn_upload.setText(text.toString());
                                btn_upload.setEnabled(false);
                            }
                            MsgUtil.toast(res.second);
                        });
                    });
                }
            });
            else btn_upload.setOnClickListener(v ->
                    MsgUtil.toast("此类型报错不可上传\n非特殊情况请勿打扰开发者谢谢喵"));

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
