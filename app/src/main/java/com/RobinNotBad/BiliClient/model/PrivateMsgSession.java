package com.RobinNotBad.BiliClient.model;
import org.json.JSONObject;

public class PrivateMsgSession {
    public long talkerUid = 0;
    public int unread = 0;
    public int contentType = 0;
    public JSONObject content = new JSONObject();
    public PrivateMsgSession(){}
}
