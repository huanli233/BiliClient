package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.Emote;
import com.RobinNotBad.BiliClient.model.EmotePackage;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmoteApi {
    public static final String BUSINESS_REPLY = "reply";
    public static final String BUSINESS_DYNAMIC = "dynamic";

    // TODO 表情面板
    public List<EmotePackage> getEmotes(String business) throws JSONException, IOException {
        List<EmotePackage> result = new ArrayList<>();
        String url = "https://api.bilibili.com/x/emote/user/panel/web?business=" + business;
        JSONObject emotePackages = NetWorkUtil.getJson(url, NetWorkUtil.webHeaders);
        if (emotePackages.getInt("code") != 0) throw new JSONException(emotePackages.getString("message"));

        JSONObject data = emotePackages.getJSONObject("data");
        JSONArray packages = data.getJSONArray("packages");
        for (int i = 0; i < packages.length(); i++) {
            EmotePackage emotePackage = new EmotePackage();
            JSONObject curObj = packages.getJSONObject(i);
            emotePackage.id = curObj.getInt("id");
            emotePackage.text = curObj.getString("text");
            emotePackage.url = curObj.getString("url");
            emotePackage.type = curObj.getInt("type");
            emotePackage.attr = curObj.getInt("attr");
            if (curObj.has("meta") && !curObj.isNull("meta")) {
                JSONObject meta = curObj.getJSONObject("meta");
                if (meta.has("size")) emotePackage.size = meta.getInt("size");
                if (meta.has("item_id")) emotePackage.item_id = meta.getInt("item_id");
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
                if (curEmote.has("meta") && !curEmote.isNull("meta") && curEmote.getJSONObject("meta").has("size")) {
                    emote.size = curEmote.getJSONObject("meta").getInt("size");
                }
                emotes.add(emote);
            }
            emotePackage.emotes = emotes;
            result.add(emotePackage);
        }
        return result;
    }
}
