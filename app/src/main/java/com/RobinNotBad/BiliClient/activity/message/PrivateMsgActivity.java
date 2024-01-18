package com.RobinNotBad.BiliClient.activity.message;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgAdapter;
import com.RobinNotBad.BiliClient.api.PrivateMsgApi;
import com.RobinNotBad.BiliClient.listener.AutoHideListener;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PrivateMsgActivity extends BaseActivity {
    JSONObject allMsg = new JSONObject();
    ArrayList<PrivateMessage> list = new ArrayList<>();
    JSONArray emoteArray = new JSONArray();
    RecyclerView msgView;
    EditText contentEt;
    ImageButton sendBtn;
    PrivateMsgAdapter adapter;
    LinearLayout inputLayout;
    long uid;
    boolean isLoadingMore = false;
    long lastLoadTime = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_msg);
        
        msgView = findViewById(R.id.msg_view);
        contentEt = findViewById(R.id.msg_input_et);
        sendBtn = findViewById(R.id.send_btn);
        inputLayout = findViewById(R.id.layout_input);
        
        
        Intent intent = getIntent();
        uid = intent.getLongExtra("uid",114514);
        Log.e("",String.valueOf(uid));

        findViewById(R.id.top).setOnClickListener(view -> finish());
    
        CenterThreadPool.run(()->{
            try {
            	allMsg = PrivateMsgApi.getPrivateMsg(uid,50,0,0);
                list = PrivateMsgApi.getPrivateMsgList(allMsg);
                Collections.synchronizedList(list); 
                Collections.reverse(list);
                emoteArray = PrivateMsgApi.getEmoteJsonArray(allMsg);
                adapter = new PrivateMsgAdapter(list,emoteArray,this);
                runOnUiThread(()->{
                    msgView.setLayoutManager(new LinearLayoutManager(this));
                    msgView.setAdapter(adapter);
                    setViewAutoHide(this,inputLayout,msgView,0);
                    setViewAutoHide(this, findViewById(R.id.top),msgView,1);
                    ((LinearLayoutManager) Objects.requireNonNull(msgView.getLayoutManager())).scrollToPositionWithOffset(list.size()-1,0);
                    msgView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (!recyclerView.canScrollVertically(-1)&&!isLoadingMore&&newState==RecyclerView.SCROLL_STATE_DRAGGING) {
                                isLoadingMore =true;
                                loadMore();
                                Log.e("","滑动到顶部，开始刷新");
                            }
                        }
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            
                        }
                    });
                });    
                
            } catch(IOException err) {
            	Log.e("",err.toString());
            } catch(JSONException err){
                Log.e("",err.toString());
            }
        });
        
        sendBtn.setOnClickListener(view -> CenterThreadPool.run(()->{
            try {
                if(!contentEt.getText().toString().equals("")) {
                    String content = contentEt.getText().toString();
                    contentEt.setText("");
                    JSONObject result = PrivateMsgApi.sendMsg(SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,114514),uid,PrivateMessage.TYPE_TEXT,System.currentTimeMillis()/1000,"{\"content\":\""+content+"\"}");
                    runOnUiThread(()->{
                        try {
                            if(result.getInt("code")==0) {
                                MsgUtil.toast("发送成功",this);
                                refresh();
                                msgView.smoothScrollToPosition(list.size()-1);
                            }else{
                                if(result.getInt("code")==21047){
                                    MsgUtil.toast(result.getString("message"),this);
                                }
                                MsgUtil.toast("发送失败",this);
                            }
                        } catch (JSONException e) {
                            MsgUtil.toast("发送失败：\n"+ result,this);
                            e.printStackTrace();
                        }
                    });
                }else{
                runOnUiThread(()-> MsgUtil.toast("你还木有输入喵~",this));
            }
            } catch(Exception err) {
                err.printStackTrace();
            }
        }));
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Runnable task = this::refresh;
        scheduledExecutorService.scheduleAtFixedRate(task,60,60, TimeUnit.SECONDS);
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
                else {hide = new TranslateAnimation(0, 0, 0, -height);}
                final int hideDuration = 250;
                hide.setDuration(hideDuration);
                AccelerateDecelerateInterpolator i = new AccelerateDecelerateInterpolator();
                hide.setInterpolator(i);
                hide.setFillAfter(true);
                TranslateAnimation show;
                if(azimuth==0){show = new TranslateAnimation(0, 0, height, 0);}
                else{show = new TranslateAnimation(0, 0, -height, 0);}
                final int showDuration = 250;
                show.setDuration(showDuration);
                show.setInterpolator(i);
                show.setFillAfter(true);
                
                list.addOnScrollListener(new AutoHideListener(activity,view,hide,hideDuration,show,showDuration));
            }
        });
    }
    private void refresh() {
    	CenterThreadPool.run(()->{
            try {
                int oldListSize = list.size();
            	JSONObject msgResult = PrivateMsgApi.getPrivateMsg(uid,50,list.get(list.size()-1).msgSeqno,0);
                ArrayList<PrivateMessage> newList = PrivateMsgApi.getPrivateMsgList(msgResult);
                for(int i = 0; i < PrivateMsgApi.getEmoteJsonArray(msgResult).length(); ++i) {
                	JSONObject emote = PrivateMsgApi.getEmoteJsonArray(msgResult).getJSONObject(i);
                    emoteArray.put(emote);
                }
                Collections.reverse(newList);
                runOnUiThread(()->{
                    for(PrivateMessage msg : newList) {
                        list.add(msg);
                        adapter.notifyItemInserted(list.size()-1);
                    }
                    adapter.notifyItemRangeChanged(oldListSize-1,list.size());
                });    
                
            } catch(IOException err) {
            	Log.e("",err.toString());
            } catch(JSONException err){
                Log.e("",err.toString());
            }
        });
    }
    private void loadMore() {
    	CenterThreadPool.run(()->{
            try {
            	if(allMsg.getInt("has_more")==1) {
                    isLoadingMore = true;
            		allMsg = PrivateMsgApi.getPrivateMsg(uid,15,0,list.get(0).msgSeqno);
                    Log.e("",allMsg.toString());
                    ArrayList<PrivateMessage> newList = PrivateMsgApi.getPrivateMsgList(allMsg);
                    Collections.reverse(newList);
                    
                    for(int i = 0; i < PrivateMsgApi.getEmoteJsonArray(allMsg).length(); ++i) {
                	JSONObject emote = PrivateMsgApi.getEmoteJsonArray(allMsg).getJSONObject(i);
                    emoteArray.put(emote);
                    
                    for (PrivateMessage a : list) {
                        Log.e("msgAll",a.msgSeqno+a.name + "." + a.uid+ "." + a.msgId+ "." + a.timestamp + "." + a.content + "."  + a.type);
                    }
                            
                    Log.e("loadMore","loadMore");
                            
                    runOnUiThread(()->{
                        MsgUtil.toast("加载更多中。。",this);
                        boolean added = list.addAll(0,newList);
                        Log.e("added?",String.valueOf(added));                       
                        for(int a =0 ;a<newList.size();a++) {
                            adapter.notifyItemInserted(a);
                        }
                        adapter.notifyItemRangeChanged(0,list.size());
                    });    
                    isLoadingMore=false;
                    }
            	}else runOnUiThread(()-> MsgUtil.toast("没有更多消息了",this));
            } catch(IOException err) {
            	err.printStackTrace();
            } catch(JSONException err){
                err.printStackTrace();
            }
        });
    }
}
