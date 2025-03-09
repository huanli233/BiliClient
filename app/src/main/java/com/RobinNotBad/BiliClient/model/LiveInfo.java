package com.RobinNotBad.BiliClient.model;

public class LiveInfo {
    private UserInfo userInfo;
    private LiveRoom liveRoom;
    private LivePlayInfo livePlayInfo;

    public LiveInfo(UserInfo userInfo, LiveRoom liveRoom, LivePlayInfo livePlayInfo) {
        this.userInfo = userInfo;
        this.liveRoom = liveRoom;
        this.livePlayInfo = livePlayInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public LiveRoom getLiveRoom() {
        return liveRoom;
    }

    public LivePlayInfo getLivePlayInfo() {
        return livePlayInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setLiveRoom(LiveRoom liveRoom) {
        this.liveRoom = liveRoom;
    }

    public void setLivePlayInfo(LivePlayInfo livePlayInfo) {
        this.livePlayInfo = livePlayInfo;
    }
}
