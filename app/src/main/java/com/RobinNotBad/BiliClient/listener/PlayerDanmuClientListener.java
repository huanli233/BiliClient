package com.RobinNotBad.BiliClient.listener;

import android.util.Log;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.netease.hearttouch.brotlij.Brotli;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

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

    private boolean isSign = false;


    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        super.onOpen(webSocket, response);
        Log.e("debug","WebSocket已连接");

        try {
            JSONObject object = new JSONObject();
            object.put("uid",mid);
            object.put("roomid",roomid);
            object.put("protover",3);
            object.put("platform","web");
            object.put("buvid", NetWorkUtil.getCookies().getOrDefault("buvid3", ""));
            object.put("type",2);
            object.put("key", key);

            webSocket.send(messageData.getData(1, 7, object.toString().getBytes(Charset.forName("UTF-8"))));
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
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        super.onMessage(webSocket, text);
        Log.e("debug","Websoctet回复：" + text);
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        super.onMessage(webSocket, bytes);
        Log.e("debug","Websoctet回复：" + bytes);
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosed(webSocket, code, reason);
        Log.e("debug","WebSocket连接关闭：" + reason + "(" + code + ")");
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
        super.onFailure(webSocket, t, response);

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        t.printStackTrace(printWriter);

        Log.e("debug","WebSocket连接失败：" + writer);
    }
}
