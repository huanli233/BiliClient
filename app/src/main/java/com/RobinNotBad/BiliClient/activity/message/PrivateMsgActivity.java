package com.RobinNotBad.BiliClient.activity.message;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.BottomOffsetDecoration;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgAdapter;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgSessionsAdapter;
import com.RobinNotBad.BiliClient.api.PrivateMsgApi;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.listener.AutoHideListener;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.model.PrivateMsgSession;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import org.json.JSONObject;

public class PrivateMsgActivity extends BaseActivity {
    JSONObject allMsg = new JSONObject();
    ArrayList<PrivateMessage> list = new ArrayList<>();
    RecyclerView msgView;
    EditText contentEt;
    ImageButton sendBtn;
    PrivateMsgAdapter adapter;
    LinearLayout inputLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_msg);
        
        msgView = findViewById(R.id.msg_view);
        contentEt = findViewById(R.id.msg_input_et);
        sendBtn = findViewById(R.id.send_btn);
        inputLayout = findViewById(R.id.layout_input);
        
        
        Intent intent = getIntent();
        long uid = intent.getLongExtra("uid",114514);
        Log.e("",String.valueOf(uid));

        findViewById(R.id.top).setOnClickListener(view -> finish());
    
        CenterThreadPool.run(()->{
            try {
            	allMsg = PrivateMsgApi.getPrivateMsg(uid,50);
                list = PrivateMsgApi.getPrivateMsgList(allMsg);
                Collections.reverse(list);
                adapter = new PrivateMsgAdapter(list,PrivateMsgApi.getEmoteJsonArray(allMsg),this);
                runOnUiThread(()->{
                    msgView.setLayoutManager(new LinearLayoutManager(this));
                    msgView.setAdapter(adapter);
                    setViewAutoHide(this,inputLayout,msgView,0);
                    setViewAutoHide(this,(ConstraintLayout)findViewById(R.id.top),msgView,1);
                    ((LinearLayoutManager)msgView.getLayoutManager()).scrollToPositionWithOffset(list.size()-1,0);
                });    
                
            } catch(IOException err) {
            	Log.e("",err.toString());
            } catch(JSONException err){
                Log.e("",err.toString());
            }
        });
        
        sendBtn.setOnClickListener(view ->{
            CenterThreadPool.run(()->{
                try {
                	if(!contentEt.getText().equals("")) {
                        PrivateMessage msg = new PrivateMessage(SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,114514),PrivateMessage.TYPE_TEXT,new JSONObject("{\"content\":\""+contentEt.getText()+"\"}"),System.currentTimeMillis()/1000,UserInfoApi.getCurrentUserInfo().name,0);
                        JSONObject result = PrivateMsgApi.sendMsg(SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,114514),uid,PrivateMessage.TYPE_TEXT,msg.timestamp,msg.content.toString());
                        msg.msgId = result.getJSONObject("data").getLong("msg_key");
                        runOnUiThread(()->{
                            try {
                            	if(result.getInt("code")==0) {
                                    MsgUtil.toast("发送成功",this);
                                    list.add(msg);
                                    contentEt.setText("");
                                    adapter.notifyItemInserted(list.size()-1);
                                    adapter.notifyItemRangeChanged(list.size()-1,list.size()-1);
                                }else{
                                    MsgUtil.toast("发送失败",this);
                                }
                            } catch(Exception err) {
                            	
                            }
                        });
                    }else{
                    runOnUiThread(()->{
                        MsgUtil.toast("空消息，不予发送",this);
                    });
                }
                } catch(Exception err) {
                	err.printStackTrace();
                }
            });
            
        });
        
    }
    //1在上面0在下面
    private static void setViewAutoHide(final Activity activity, final View view, final RecyclerView list,int azimuth) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = view.getMeasuredHeight() + 2;
                Log.i("TAGAAA", "height=" + height);
                TranslateAnimation hide;
                if(azimuth==0){hide = new TranslateAnimation(0, 0, 0, height);}
                else {hide = new TranslateAnimation(0, 0, 0, 0-height);}
                final int hideDuration = 250;
                hide.setDuration(hideDuration);
                AccelerateDecelerateInterpolator i = new AccelerateDecelerateInterpolator();
                hide.setInterpolator(i);
                hide.setFillAfter(true);
                TranslateAnimation show;
                if(azimuth==0){show = new TranslateAnimation(0, 0, height, 0);}
                else{show = new TranslateAnimation(0, 0, 0-height, 0);}
                final int showDuration = 250;
                show.setDuration(showDuration);
                show.setInterpolator(i);
                show.setFillAfter(true);
                
                list.addOnScrollListener(new AutoHideListener(activity,view,hide,hideDuration,show,showDuration));
            }
        });
    }
}
