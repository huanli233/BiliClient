package com.RobinNotBad.BiliClient.listener;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.activity.player.PlayerActivity;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.netease.hearttouch.brotlij.Brotli;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class PlayerDanmuClientListener extends WebSocketListener {
    public long mid = 0;
    public long roomid = 0;
    public String key = "";
    private int seq = 1;
    private final MessageData messageData = new MessageData();

    private Timer heartTimer = null;

    public PlayerActivity playerActivity;


    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        super.onOpen(webSocket, response);
        Log.e("debug","WebSocket已连接");

        if(heartTimer != null) heartTimer.cancel();

        //发送认证包
        try {
            JSONObject object = new JSONObject();
            if(SharedPreferencesUtil.getBoolean("live_by_guest",false)) object.put("uid",0);
            else object.put("uid",mid);
            object.put("roomid",roomid);
            object.put("protover",3);
            object.put("platform","web");
            object.put("buvid", NetWorkUtil.getCookies().getOrDefault("buvid3", ""));
            object.put("type",2);
            object.put("key", key);

            webSocket.send(messageData.getData(3, 7, object.toString().getBytes(Charset.forName("UTF-8"))));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private class MessageData {

        private byte[] getPacket(int protocolVersion, int actionCode, byte... data) {
            int headerSize = 16;
            int totalSize = headerSize + data.length;
            byte[] packet = new byte[totalSize];

            packet[0] = (byte) (totalSize >> 24);
            packet[1] = (byte) (totalSize >> 16);
            packet[2] = (byte) (totalSize >> 8);
            packet[3] = (byte) (totalSize);

            packet[4] = (byte) (0);
            packet[5] = (byte) (headerSize);

            packet[6] = (byte) 0;
            packet[7] = (byte) protocolVersion;

            packet[8] = (byte) 0;
            packet[9] = (byte) 0;
            packet[10] = (byte) 0;
            packet[11] = (byte) actionCode;

            packet[12] = (byte) (seq >> 24);
            packet[13] = (byte) (seq >> 16);
            packet[14] = (byte) (seq >> 8);
            packet[15] = (byte) (seq);
            seq++;

            System.arraycopy(data, 0, packet, headerSize, data.length);
            Log.d("BiliClient", "getPackage totalLen=" + totalSize + ", data=" + new String(data) + ", result=" + ByteString.of(packet).hex());
            return packet;
        }

        public ByteString getData(int protocolVersion, int actionCode, byte... data) {
            return ByteString.of(getPacket(protocolVersion, actionCode, data));
        }

        public ByteString getBrotliData(int protocolVersion, int actionCode, byte... data) {
            byte[] encodedData = Brotli.compress(getPacket(protocolVersion, actionCode, data));
            return ByteString.of(getPacket(3, actionCode, encodedData));
        }

    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        super.onMessage(webSocket, bytes);

        int actionCode = bytes.getByte(11);
        switch (actionCode){
            case 8:
                Log.e("debug","弹幕流认证成功");
                heartTimer = new Timer();
                TimerTask heartTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        Log.e("debug","发送心跳包");
                        try {
                            webSocket.send(messageData.getData(1, 2, "".getBytes(Charset.forName("UTF-8"))));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                };
                heartTimer.schedule(heartTimerTask,3000,32000);
                break;

            case 5:
                plainPackage(bytes);
                break;

            default:
                break;
        }
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosed(webSocket, code, reason);
        Log.e("debug","WebSocket连接关闭：" + reason + "(" + code + ")");

        if(heartTimer != null) heartTimer.cancel();
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
        super.onFailure(webSocket, t, response);

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        t.printStackTrace(printWriter);

        Log.e("debug","WebSocket连接失败：" + writer);

        if(heartTimer != null) heartTimer.cancel();
    }

    //处理普通包
    private void plainPackage(ByteString bytes){
        try {
            JSONObject result;

            //有些包不会压缩，要判断一下，虽然方式有点（
            ByteString bytes2 = bytes.substring((int)bytes.getByte(5));
            if(Brotli.decompress(bytes2.toByteArray()).length > 5){
                ByteString bytes3 = ByteString.of(Brotli.decompress(bytes2.toByteArray()));
                result = new JSONObject(bytes3.substring((int)bytes3.getByte(5)).utf8()); //问就是懒得用别的方式sub
            }
            else if(bytes2.utf8().contains("{"))
                result = new JSONObject(bytes2.utf8().substring(bytes2.utf8().indexOf("{")));
            else return;

            JSONObject data;
            switch (result.getString("cmd")){
                case "DANMU_MSG":
                    JSONArray info = result.getJSONArray("info");
                    String nickname = info.getJSONArray(0).getJSONObject(15).getJSONObject("user").getJSONObject("base").getString("name");
                    String content = info.getString(1);
                    if(SharedPreferencesUtil.getBoolean("player_danmaku_showsender",true)) playerActivity.adddanmaku(nickname + "：" + content, Color.WHITE);
                    else playerActivity.adddanmaku(content, Color.WHITE);
                    break;

                case "WATCHED_CHANGE":
                    data = result.getJSONObject("data");
                    playerActivity.online_number = data.getString("text_large");
                    break;

                case "INTERACT_WORD":
                    data = result.getJSONObject("data");

                    //进入直播间
                    if(data.getInt("msg_type") == 1) {
                        playerActivity.adddanmaku(data.getString("uname") + " 进入了直播间", Color.CYAN, 12, 4, 0);
                    }

                    break;
                
                case "SEND_GIFT":
                    data = result.getJSONObject("data");
                    String content2 = data.getString("uname") + " " + data.getString("action") + data.getInt("num") + "个" + data.getString("giftName");
                    playerActivity.adddanmaku(content2, Color.WHITE, 25, 1, Color.argb(160,255,80,80));
                    break;

                default:
                    break;
            }

        }catch (Exception e){
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);

            Log.e("debug","解析普通包时错误：" + writer);
        }
    }
}
