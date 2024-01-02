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
    ArrayList<PrivateMessage> list ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test_pri_msg);
        CenterThreadPool.run(()->{
            try {
            	list = PrivateMsgApi.getPrivateMsg(521241135,10);
            } catch(IOException err) {
            	Log.e("",err.toString());
            } catch(JSONException err){
                Log.e("",err.toString());
            }
        });
        
        //Log.e("",list.toString());
        
        /*
        RecyclerView view = findViewById(R.id.test_recycler);
        PrivateMsgAdapter adapter = new PrivateMsgAdapter(list);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setAdapter(adapter);
        */
    }
}
