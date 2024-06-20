package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;
import java.util.List;

public class LivePlayInfo implements Serializable {
    public long roomid;
    public long short_id;
    public long uid;
    public boolean isHidden;
    public boolean isLocked;
    public boolean isPortrait;
    public int live_status;
    public boolean encrypted;
    public boolean pwd_verified;
    public long live_time;
    public String conf_json;
    public PlayUrl playUrl;
    public int official_type;
    public int official_room_id;
    public int risk_with_delay;

    public static class PlayUrl implements Serializable {
        public long cid;
        public List<QnDesc> g_qn_desc;
        public List<ProtocolInfo> stream;
        public P2PData p2p_data;
        public int dolby_qn;
    }

    public static class P2PData implements Serializable {
        public boolean p2p;
        public int p2p_type;
        public boolean m_p2p;
        public List<String> m_servers;
    }

    public static class QnDesc implements Serializable {
        public int qn;
        public String desc;
        public String hdr_desc;
        public String attr_desc;
    }

    public static class ProtocolInfo implements Serializable {
        public String protocol_name;
        public List<Format> format;
    }

    public static class Format implements Serializable {
        public String format_name;
        public List<Codec> codec;
    }

    public static class Codec implements Serializable {
        public String codec_name;
        public int current_qn;
        public List<Integer> accept_qn;
        public String base_url;
        public List<UrlInfo> url_info;
        public int hdr_qn;
        public int dolby_type;
        public String attr_name;
        public String master_url;
    }

    public static class UrlInfo implements Serializable {
        public String host;
        public String extra;
        public int stream_ttl;
    }
}
