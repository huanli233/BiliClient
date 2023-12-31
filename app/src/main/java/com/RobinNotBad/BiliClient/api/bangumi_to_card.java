package com.RobinNotBad.BiliClient.api;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import com.RobinNotBad.BiliClient.model.Media;
import com.RobinNotBad.BiliClient.model.MediaSectionInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class bangumi_to_card {

    //获取番剧信息, 详情页需要有基本的cover, 信息等
    public static void getMediaInfo(Activity activity, String mediaId, NetWorkUtil.Callback<Media> callback) {
        CenterThreadPool.run(() -> {
            try {
                String url = "https://api.bilibili.com/pgc/review/user?media_id=" + mediaId;
                ResponseBody body = NetWorkUtil.get(url, ConfInfoApi.defHeaders).body();
                if (body == null) {
                    throw new NetworkErrorException("baseMedia info body is null");
                }
                JSONObject all = new JSONObject(body.string());
                int code = all.getInt("code");
                if (code != 0) {
                    throw new IOException("从服务器获取剧集失败, code = " + code);
                }
                JSONObject result = all.getJSONObject("result");
                Media baseMediaInfo = new Media(result);
                if (activity != null && !activity.isDestroyed()) {
                    activity.runOnUiThread(() -> callback.onSuccess(baseMediaInfo));
                }
                body.close();
            } catch (Exception e) {
                if (activity != null && !activity.isDestroyed()) {
                    activity.runOnUiThread(() -> callback.onFailed(e));
                }
            }
        });
    }
    /** @noinspection unchecked*/
    public static MediaSectionInfo getSectionInfo(String seasonId) throws JSONException, IOException {
        JSONArray cardArray = bangumi_to_car(seasonId);
        //先建main_section
        JSONObject mainSection = (JSONObject) cardArray.get(0);
        ArrayList<VideoCard> mainSectionCards = (ArrayList<VideoCard>) mainSection.get("card");
        MediaSectionInfo.SectionInfo mainSectionInfo = new MediaSectionInfo.SectionInfo();
        mainSectionInfo.title = mainSection.getString("title");
        mainSectionInfo.episodes = new MediaSectionInfo.EpisodeInfo[mainSectionCards.size()];
        for (int i = 0; i < mainSectionCards.size(); i++) {
            mainSectionInfo.episodes[i] = new MediaSectionInfo.EpisodeInfo(mainSectionCards.get(i));
        }

        //再建sections
        MediaSectionInfo.SectionInfo[] sectionsInfo = new MediaSectionInfo.SectionInfo[cardArray.length() - 1];
        for (int i = 1; i < cardArray.length(); i++) {
            JSONObject section = (JSONObject) cardArray.get(i);
            String sectionTitle = section.getString("title");
            ArrayList<VideoCard> sectionCards = (ArrayList<VideoCard>) section.get("card");
            MediaSectionInfo.SectionInfo sectionInfo = new MediaSectionInfo.SectionInfo();
            sectionInfo.title = sectionTitle;
            sectionInfo.episodes = new MediaSectionInfo.EpisodeInfo[sectionCards.size()];
            for (int j = 0; j < sectionCards.size(); j++) {
                sectionInfo.episodes[j] = new MediaSectionInfo.EpisodeInfo(sectionCards.get(j));
            }
            sectionsInfo[i - 1] = sectionInfo;
        }
        // 建完回传, done.
        return new MediaSectionInfo(mainSectionInfo, sectionsInfo);
    }

    public static JSONArray bangumi_to_car(String season_id) throws JSONException, IOException {
        JSONObject result = GetMain_section(season_id);
        JSONArray cardArray = new JSONArray();
        JSONObject main_section = result.getJSONObject("main_section");
        JSONArray episodes = main_section.getJSONArray("episodes");
        JSONObject input = new JSONObject();
        ArrayList<VideoCard> list = new ArrayList<>();
        for (int j = 0; j < episodes.length(); j++) {
            JSONObject array = episodes.getJSONObject(j);
            String title = array.getString("long_title");
            String upname = array.getString("badge");
            String playTimesStr = "敬请期待" + "观看";
            String cover = array.getString("cover");
            String bvid = BilibiliIDConverter.aidtobv(array.getLong("aid"));
            long aid = array.getLong("aid");
            long cid = array.getLong("cid");
            list.add(new VideoCard(title,upname,playTimesStr,cover,aid,bvid,cid));
        }
        input.put("card", list);
        input.put("title", main_section.getString("title"));
        cardArray.put(input);
        JSONArray section = result.getJSONArray("section");
        for (int j = 0; j < section.length(); j++) {
            input = new JSONObject();
            list = new ArrayList<>();
            JSONObject card = section.getJSONObject(j);
            JSONArray CardArray = card.getJSONArray("episodes");
            for (int i = 0; i < CardArray.length(); i++) {
                JSONObject array = CardArray.getJSONObject(i);
                String title = array.getString("long_title");
                String upname = array.getString("badge");
                String playTimesStr = "敬请期待" + "观看";
                String cover = array.getString("cover");
                String bvid = BilibiliIDConverter.aidtobv(array.getLong("aid"));
                long aid = array.getLong("aid");
                long cid = array.getLong("cid");
                list.add(new VideoCard(title,upname,playTimesStr,cover,aid,bvid,cid));
            }
            input.put("card", list);
            input.put("title", card.getString("title"));
            cardArray.put(input);
        }
        return cardArray;
    }
    public static JSONObject GetMain_section(String season_id) throws IOException, JSONException {
        String url = "https://api.bilibili.com/pgc/web/season/section?season_id=" + season_id;
        JSONObject all = new JSONObject(Objects.requireNonNull(Objects.requireNonNull(NetWorkUtil.get(url)).body()).string());  //得到一整个json
        return all.getJSONObject("result");
    }
}

