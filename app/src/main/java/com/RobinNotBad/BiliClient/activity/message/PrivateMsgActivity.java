package com.RobinNotBad.BiliClient.activity.message;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.BottomOffsetDecoration;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgAdapter;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgSessionsAdapter;
import com.RobinNotBad.BiliClient.api.PrivateMsgApi;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_msg);
        
        msgView = findViewById(R.id.msg_view);
        contentEt = findViewById(R.id.msg_input_et);
        sendBtn = findViewById(R.id.send_btn);
        
        Intent intent = getIntent();
        long uid = intent.getLongExtra("uid",114514);
        Log.e("",String.valueOf(uid));
        
    
        CenterThreadPool.run(()->{
            try {
            	allMsg = PrivateMsgApi.getPrivateMsg(uid,50);
                list = PrivateMsgApi.getPrivateMsgList(allMsg);
                Collections.reverse(list);
                adapter = new PrivateMsgAdapter(list,PrivateMsgApi.getEmoteJsonArray(allMsg),this);
                runOnUiThread(()->{
                    msgView.setLayoutManager(new LinearLayoutManager(this));
                    msgView.setAdapter(adapter);
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
                        PrivateMessage msg = new PrivateMessage(uid,PrivateMessage.TYPE_TEXT,new JSONObject("{\"content\":\""+contentEt.getText()+"\"}"),System.currentTimeMillis()/1000,UserInfoApi.getUserInfo(uid).name,0);
                        JSONObject result = PrivateMsgApi.sendMsg(SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,114514),uid,PrivateMessage.TYPE_TEXT,msg.timestamp,msg.content.toString());
                        msg.msgId = result.getJSONObject("data").getLong("msg_key");
                        runOnUiThread(()->{
                            try {
                            	if(result.getInt("code")==0) {
                                    MsgUtil.toast("发送成功",this);
                                    list.add(msg);
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
}
