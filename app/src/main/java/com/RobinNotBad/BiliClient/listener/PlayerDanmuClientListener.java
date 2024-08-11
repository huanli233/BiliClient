package com.RobinNotBad.BiliClient.listener;

import android.util.Log;

import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class PlayerDanmuClientListener extends WebSocketListener {
    public long mid = 0;
    public long roomid = 0;
    public String key = "";

    private boolean isSign = false;


    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.e("debug","WebSocket已连接");

        try {
            JSONObject object = new JSONObject();
            object.put("uid",mid);
            object.put("roomid",roomid);
            object.put("protover",3);
            object.put("platform","web");
            object.put("type",2);
            object.put("key",2);

            int l = 0x0010 + object.toString().length(); //封包总大小（头部大小+正文大小）
            short h = 0x0010; //头部大小（一般为0x0010，16字节）
            short k = 0x1; //协议版本
            int c = 7; //操作码（封包类型）
            int p = 1; //每次发包时向上递增

            //不写了
            //研究不出来了
            //其他人试试吧
            //反正这里就拼接一下头部数据

            ByteString byteString = ByteString.of((byte) l,(byte) h,(byte) k,(byte) c,(byte) p);
            byteString = ByteString.of(ToolsUtil.byteMerger(byteString.toByteArray(),object.toString().getBytes()));

            webSocket.send(byteString);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        Log.e("debug","Websoctet回复：" + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        Log.e("debug","Websoctet回复：" + bytes);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        Log.e("debug","WebSocket连接关闭：" + reason + "(" + code + ")");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        t.printStackTrace(printWriter);

        Log.e("debug","WebSocket连接失败：" + writer.toString());
    }
}
