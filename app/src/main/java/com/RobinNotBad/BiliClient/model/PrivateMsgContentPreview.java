package com.RobinNotBad.BiliClient.model;
import com.RobinNotBad.BiliClient.model.PrivateMsgContentPreview;
import org.json.JSONObject;

public class PrivateMsgContentPreview {
    long talker_uid = 0;
    int unread = 0;
    JSONObject content = new JSONObject();
    public PrivateMsgContentPreview(){}
}
