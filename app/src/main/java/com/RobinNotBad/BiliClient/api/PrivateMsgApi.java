package com.RobinNotBad.BiliClient.api;
import android.util.Log;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PrivateMsgApi {
    
    //返回的是倒序的消息列表，使用时记得列表倒置
    public static ArrayList<PrivateMessage> getPrivateMsg(long talkerId,int size) throws IOException, JSONException {
        String url = "https://api.vc.bilibili.com/svr_sync/v1/svr_sync/fetch_session_msgs?session_type=1&talker_id="+talkerId+"&size="+size;
        JSONObject root = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, ConfInfoApi.webHeaders).body()).string());
        ArrayList<PrivateMessage> list = new ArrayList<>();
        if(root.has("data")&&!root.isNull("data")){
            JSONArray messages = root.getJSONObject("data").getJSONArray("messages");
            UserInfo myInfo = UserInfoApi.getCurrentUserInfo();
            UserInfo targetInfo = new UserInfo();
            
            //list.add(new PrivateMessage(114514,1,new JSONObject("{\"content\":\" 。\"}"),11111111,"aaaaa",111));
            
            boolean isReqTargetInfo = false;
            if(messages!=null){
            	for(int i = 0; i < messages.length(); i++) {
            		PrivateMessage msgObject = new PrivateMessage();
                    JSONObject msgJson = messages.getJSONObject(i);
                    msgObject.uid = msgJson.getLong("sender_uid");
                    msgObject.type = msgJson.getInt("msg_type");
                    if(msgObject.uid==myInfo.mid) {
                    	msgObject.name=myInfo.name;
                    }else{
                        if(!isReqTargetInfo) {
                        	targetInfo = UserInfoApi.getUserInfo(msgObject.uid);
                            isReqTargetInfo = true;
                        }
                        msgObject.name = targetInfo.name;
                    }
                    msgObject.content = new JSONObject("{\"content\":\" 。\"}");   //防止内容不为json时解析错误
                    if(msgJson.getString("content").endsWith("}")&&msgJson.getString("content").startsWith("{")){
                    msgObject.content = new JSONObject(msgJson.getString("content").replace("\\",""));
                    }
                    msgObject.timestamp = msgJson.getLong("timestamp");
                    msgObject.msgId = msgJson.getLong("msg_key");
                    boolean isPuted = list.add(msgObject);
                    Log.e("puted?",String.valueOf(isPuted));
                    Log.e("msg",msgObject.name+"."+msgObject.uid+"."+msgObject.msgId+"."+msgObject.timestamp+"."+msgObject.content+"."+msgObject.type);
            	}
                Log.e("","返回msgList");
                for(PrivateMessage i : list) {
                	Log.e("msg",i.name+"."+i.uid+"."+i.msgId+"."+i.timestamp+"."+i.content+"."+i.type);
                }
                return list;
            }else return new ArrayList<PrivateMessage>();
        }else return new ArrayList<PrivateMessage>();
    }
}
