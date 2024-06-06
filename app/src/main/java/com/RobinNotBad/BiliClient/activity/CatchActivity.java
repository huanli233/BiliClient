package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.google.android.material.button.MaterialButton;

public class CatchActivity extends BaseActivity {
    private boolean openStack = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catch);

        TextView reason_view = findViewById(R.id.catch_reason);
        TextView stack_view = findViewById(R.id.stack);

        Intent intent = getIntent();
        String stack = intent.getStringExtra("stack");
        if(stack == null) finish();

        SpannableString stack_str = new SpannableString("错误堆栈：\n" + stack);
        stack_str.setSpan(new StyleSpan(Typeface.BOLD),0,5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        stack_str.setSpan(new RelativeSizeSpan(0.85f),5,stack_str.length(),Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        stack_view.setText(stack_str);

        findViewById(R.id.exit_btn).setOnClickListener(view -> System.exit(-1));

        SpannableString reason_str = null;

        if(stack!=null) {
            if (stack.contains("java.lang.IndexOutOfBoundsException"))
                reason_str = new SpannableString("可能的崩溃原因：\n滑动速度太快或触发bug");
            else if (stack.contains("org.json.JSONException"))
                reason_str = new SpannableString("可能的崩溃原因：\n数据解析错误");
            else if (stack.contains("java.lang.OutOfMemoryError"))
                reason_str = new SpannableString("可能的崩溃原因：\n内存爆了，在小内存设备上很正常");
            
            ((MaterialButton)findViewById(R.id.upload_btn)).setOnClickListener(view -> {
                if(stack.contains("java.lang.OutOfMemoryError")) Toast.makeText(this,"此问题没有必要上报",Toast.LENGTH_SHORT).show();
                else{
                    CenterThreadPool.run(() -> {
                        String res = AppInfoApi.uploadStack(stack,this);
                        runOnUiThread(() -> Toast.makeText(this,res,Toast.LENGTH_SHORT).show());
                    });
                }
            });
        }

        if(reason_str != null) {
            reason_str.setSpan(new StyleSpan(Typeface.BOLD),8,reason_str.length(),Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            reason_view.setText(reason_str);
        } else reason_view.setText("未知的崩溃原因");

        stack_view.setOnClickListener(view -> {
            if(openStack) stack_view.setMaxLines(200);
            else stack_view.setMaxLines(5);
            openStack = !openStack;
        });
    }
}
