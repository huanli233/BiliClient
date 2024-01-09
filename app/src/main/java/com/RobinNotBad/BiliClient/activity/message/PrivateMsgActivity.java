package com.RobinNotBad.BiliClient.activity.message;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgAdapter;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgSessionsAdapter;
import com.RobinNotBad.BiliClient.api.PrivateMsgApi;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.model.PrivateMsgSession;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import org.json.JSONObject;

public class PrivateMsgActivity extends AppCompatActivity {
    JSONObject allMsg = new JSONObject();
    ArrayList<PrivateMessage> list = new ArrayList<>();
    RecyclerView msgView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_msg);
        msgView = findViewById(R.id.msg_view);
        
        Intent intent = getIntent();
        long uid = intent.getLongExtra("uid",114514);
        Log.e("",String.valueOf(uid));
        
    
        CenterThreadPool.run(()->{
            try {
            	allMsg = PrivateMsgApi.getPrivateMsg(uid,50);
                list = PrivateMsgApi.getPrivateMsgList(allMsg);
                Collections.reverse(list);
                PrivateMsgAdapter adapter = new PrivateMsgAdapter(list,PrivateMsgApi.getEmoteJsonArray(allMsg),this);
                runOnUiThread(()->{
                    msgView.setLayoutManager(new LinearLayoutManager(this));
                    msgView.setAdapter(adapter);
                    msgView.scrollToPosition(list.size()-1);
                });    
            } catch(IOException err) {
            	Log.e("",err.toString());
            } catch(JSONException err){
                Log.e("",err.toString());
            }
        });
        
    }
}
