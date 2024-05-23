package com.RobinNotBad.BiliClient.activity.dynamic.send;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.Set;

/**
 * 发送动态输入Activity，直接copy的WriteReplyActivity
 * 换成了ActivityResult
 * （我并不怎么会写）
 */
public class SendDynamicActivity extends BaseActivity {

    // idk
//    private static final Map<Integer,String> msgMap = new HashMap<Integer,String>(){{
//        put(-101,"没有登录or登录信息有误？");
//        put(-102,"账号被封禁！");
//        put(-509,"请求过于频繁！");
//        put(12016,"包含敏感内容！");
//        put(12025,"字数过多啦QAQ");
//    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_dynamic);

        if(SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,0)==0){
            MsgUtil.toast("还没有登录喵~",this);
            setResult(RESULT_CANCELED);
            finish();
        }

        EditText editText = findViewById(R.id.editText);
        MaterialCardView send = findViewById(R.id.send);

        send.setOnClickListener(view -> {
            // 不了解遂直接保留cookie刷新判断了
            if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.cookie_refresh,true)){
                String text = editText.getText().toString();
                Intent result = new Intent();
                // 原神级的传数据
                Bundle bundle = SendDynamicActivity.this.getIntent().getExtras();
                if (bundle != null) result.putExtras(bundle);
                result.putExtra("text", text);
                setResult(RESULT_OK, result);
                finish();
            } else MsgUtil.showDialog(this,"无法发送","上一次的Cookie刷新失败了，\n您可能需要重新登录以进行敏感操作",-1);
        });
    }
}