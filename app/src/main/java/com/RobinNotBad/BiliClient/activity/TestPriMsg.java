package com.RobinNotBad.BiliClient.activity;

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

import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class TestPriMsg extends AppCompatActivity {
    ArrayList<PrivateMessage> list = new ArrayList<>();
    RecyclerView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test_pri_msg);
        view = findViewById(R.id.test_recycler);
        /*
        //私信界面测试
        CenterThreadPool.run(()->{
            try {
            	list = PrivateMsgApi.getPrivateMsg(521241135,20);
                    for(PrivateMessage i : list) {
                	Log.e("msgaaaa",i.name+"."+i.uid+"."+i.msgId+"."+i.timestamp+"."+i.content+"."+i.type);
                }
                    PrivateMsgAdapter adapter = new PrivateMsgAdapter(list,this);
                runOnUiThread(()->{
                    view.setLayoutManager(new LinearLayoutManager(this));
                    view.setAdapter(adapter);
                });    
            } catch(IOException err) {
            	Log.e("",err.toString());
            } catch(JSONException err){
                Log.e("",err.toString());
            }
        });
        */
        
        //聊天列表测试
        CenterThreadPool.run(()->{
            try {
            	ArrayList<PrivateMsgSession> list = PrivateMsgApi.getSessionsList(20);
                ArrayList<Long> uidList = new ArrayList<>();
                for(PrivateMsgSession item :list) {
                	uidList.add(item.talkerUid);
                }    
                HashMap<Long,UserInfo> userMap = PrivateMsgApi.getUsersInfo(uidList);
                for(long i : uidList) {
                	//Log.e("test",userMap.get(i).name+userMap.get(i).mid+userMap.get(i).avatar);
                }
                    
                for(PrivateMsgSession i : list) {
                	Log.e("test",i.contentType+i.content.toString()+i.talkerUid);
                }    
                PrivateMsgSessionsAdapter adapter = new PrivateMsgSessionsAdapter(this,list,userMap);
                runOnUiThread(()->{
                    view.setLayoutManager(new LinearLayoutManager(this));
                    view.setAdapter(adapter);
                });        
            } catch(IOException err) {
            	Log.e("",err.toString());
            }catch(JSONException err){
                Log.e("",err.toString());
            }
        });
    }
}
