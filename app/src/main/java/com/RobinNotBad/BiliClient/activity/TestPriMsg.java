package com.RobinNotBad.BiliClient.activity;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgAdapter;
import com.RobinNotBad.BiliClient.api.PrivateMsgApi;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import org.json.JSONException;

public class TestPriMsg extends AppCompatActivity {
    ArrayList<PrivateMessage> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test_pri_msg);
        new Thread(()->{
            try {
            	list = PrivateMsgApi.getPrivateMsg(521241135,20);
                    for(PrivateMessage i : list) {
                	Log.e("msgaaaa",i.name+"."+i.uid+"."+i.msgId+"."+i.timestamp+"."+i.content+"."+i.type);
                }
                    PrivateMsgAdapter adapter = new PrivateMsgAdapter(list,this);
                runOnUiThread(()->{
                    RecyclerView view = findViewById(R.id.test_recycler);
                    view.setLayoutManager(new LinearLayoutManager(this));
                    view.setAdapter(adapter);
                });    
            } catch(IOException err) {
            	Log.e("",err.toString());
            } catch(JSONException err){
                Log.e("",err.toString());
            }
        }).start();
        /*
        CenterThreadPool.run(()->{
            try {
            	list = PrivateMsgApi.getPrivateMsg(521241135,10);
                PrivateMsgAdapter adapter = new PrivateMsgAdapter(PrivateMsgApi.getPrivateMsg(521241135,10));
                runOnUiThread(()->{
                    RecyclerView view = findViewById(R.id.test_recycler);
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
        //Log.e("",list.toString());
        
        
        
        
    }
}
