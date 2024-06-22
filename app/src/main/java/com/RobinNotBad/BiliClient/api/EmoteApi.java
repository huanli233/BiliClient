package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.Emote;
import com.RobinNotBad.BiliClient.model.EmotePackage;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmoteApi {
    public static final String BUSINESS_REPLY = "reply";
    public static final String BUSINESS_DYNAMIC = "dynamic";

    /**
     * 获取表情包面板内容
     * @param business business
     * @return 获取到的面板内容（表情包）
     */
    public static List<EmotePackage> getEmotes(String business) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/emote/user/panel/web?business=" + business;
        JSONObject emotePackages = NetWorkUtil.getJson(url, NetWorkUtil.webHeaders);
        if (emotePackages.getInt("code") != 0) throw new JSONException(emotePackages.getString("message"));

        JSONObject data = emotePackages.optJSONObject("data");
        if (data != null) {
            return analyzeEmotePackages(data.optJSONArray("packages"));
        }
        return null;
    }

    // TODO 表情包管理页

    /**
     * 获取“使用中”的表情包
     * @param business business
     * @return 获取到的表情包
     */
    public static List<EmotePackage> getInUsePackages(String business) throws JSONException, IOException {
        String url = "https://api.bilibili.com/bapis/main.community.interface.emote.EmoteService/InUsePackages" + new NetWorkUtil.FormData().setUrlParam(true)
                .put("business", business)
                .put("csrf", SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, ""));
        JSONObject emotePackages = NetWorkUtil.getJson(url, NetWorkUtil.webHeaders);
        if (emotePackages.getInt("code") != 0) throw new JSONException(emotePackages.getString("message"));

        JSONObject data = emotePackages.optJSONObject("data");
        if (data != null) {
            return analyzeEmotePackages(data.optJSONArray("packages"));
        }
        return null;
    }

    /** 全部表情 */
    public static final int PACKAGES_TYPE_ALL = 0;
    /** 装扮商城 */
    public static final int PACKAGES_TYPE_SHOP = 1;
    /** 会员专属 */
    public static final int PACKAGES_TYPE_VIP = 2;
    /** 包月充电 */
    public static final int PACKAGES_TYPE_MONTH_CHARGE = 3;

    /**
     * 获取“待使用”的表情包
     * @param business business
     * @param type 见PACKAGES_TYPE常量
     * @param pn page页数
     * @return 获取到的表情包
     */
    public static List<EmotePackage> getMyPackages(String business, int type, int pn) throws JSONException, IOException {
        String url = "https://api.bilibili.com/bapis/main.community.interface.emote.EmoteService/MyPackages" + new NetWorkUtil.FormData().setUrlParam(true)
                .put("business", business)
                .put("csrf", SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, ""))
                .put("pn", pn)
                .put("ps", 12)
                .put("type", type);
        JSONObject emotePackages = NetWorkUtil.getJson(url, NetWorkUtil.webHeaders);
        if (emotePackages.getInt("code") != 0) throw new JSONException(emotePackages.getString("message"));

        JSONObject data = emotePackages.optJSONObject("data");
        if (data != null) {
            return analyzeEmotePackages(data.optJSONArray("packages"));
        }
        return null;
    }

    /**
     * 获取”更多表情“中的内容，或许不会用到，因为其中的内容都是需要前往购买/获取说明页的。
     * @param business business
     * @param pn page页数
     * @param search 搜索关键词
     * @return 获取到的表情包
     */
    public static List<EmotePackage> getAllPackages(String business, int pn, String search) throws JSONException, IOException {
        String url = "https://api.bilibili.com/bapis/main.community.interface.emote.EmoteService/AllPackages" + new NetWorkUtil.FormData().setUrlParam(true)
                .put("business", business)
                .put("csrf", SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, ""))
                .put("pn", pn)
                .put("ps", 12)
                .put("search", search);
        JSONObject emotePackages = NetWorkUtil.getJson(url, NetWorkUtil.webHeaders);
        if (emotePackages.getInt("code") != 0) throw new JSONException(emotePackages.getString("message"));

        JSONObject data = emotePackages.optJSONObject("data");
        if (data != null) {
            return analyzeEmotePackages(data.optJSONArray("packages"));
        }
        return null;
    }

    /**
     * 添加/移除表情包
     * @param business business
     * @param isAdd 是否为添加，否则为移除
     * @param ids 要添加/移除的表情包id（们）
     * @return 返回值
     */
    public static int setPackage(String business, boolean isAdd, int... ids) throws JSONException, IOException {
        StringBuilder idsSb = new StringBuilder();
        boolean flag = true;
        for (int i : ids) {
            if (flag) {
                flag = false;
            } else {
                idsSb.append(",");
            }
            idsSb.append(i);
        }
        String url = "https://api.bilibili.com/bapis/main.community.interface.emote.EmoteService/AllPackages" + new NetWorkUtil.FormData().setUrlParam(true)
                .put("business", business)
                .put("csrf", SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, ""))
                .put("ids", idsSb.toString())
                .put("type", isAdd ? 0 : 1);
        JSONObject emotePackages = NetWorkUtil.getJson(url, NetWorkUtil.webHeaders);
        return emotePackages.getInt("code");
    }

    public static List<EmotePackage> analyzeEmotePackages(JSONArray packages) throws JSONException {
        if (packages == null) return null;
        List<EmotePackage> result = new ArrayList<>();
        for (int i = 0; i < packages.length(); i++) {
            EmotePackage emotePackage = new EmotePackage();
            JSONObject curObj = packages.getJSONObject(i);
            emotePackage.id = curObj.getInt("id");
            emotePackage.text = curObj.getString("text");
            emotePackage.url = curObj.getString("url");
            emotePackage.type = curObj.getInt("type");
            emotePackage.attr = curObj.getInt("attr");
            JSONObject meta = curObj.optJSONObject("meta");
            if (meta != null) {
                emotePackage.size = meta.optInt("size", 1);
                emotePackage.item_id = meta.optInt("item_id", -1);
            }
            JSONObject flags = curObj.optJSONObject("flags");
            if (flags != null) {
                emotePackage.permanent = flags.optBoolean("permanent", false);
            }
            List<Emote> emotes = new ArrayList<>();
            JSONArray jsonEmotes = curObj.getJSONArray("emote");
            for (int j = 0; j < jsonEmotes.length(); j++) {
                Emote emote = new Emote();
                JSONObject curEmote = jsonEmotes.getJSONObject(j);
                emote.id = curEmote.getInt("id");
                emote.packageId = curEmote.getInt("package_id");
                emote.name = curEmote.getString("text");
                emote.url = curEmote.getString("url");
                JSONObject childMeta = curEmote.optJSONObject("meta");
                if (childMeta != null) {
                    emote.size = childMeta.optInt("size", 1);
                    emote.alias = childMeta.optString("alias");
                }
                emotes.add(emote);
            }
            emotePackage.emotes = emotes;
            result.add(emotePackage);
        }
        return result;
    }
}
