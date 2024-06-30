package com.RobinNotBad.BiliClient.api;

import android.util.Log;
import android.util.Pair;

import com.RobinNotBad.BiliClient.model.Collection;
import com.RobinNotBad.BiliClient.model.FavoriteFolder;
import com.RobinNotBad.BiliClient.model.Opus;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//收藏API

public class FavoriteApi {
    // TODO 合集收藏

    public static ArrayList<FavoriteFolder> getFavoriteFolders(long mid) throws IOException, JSONException {
        String url = "https://space.bilibili.com/ajax/fav/getBoxList?mid=" + mid;
        JSONObject result = NetWorkUtil.getJson(url);
        JSONObject data = result.getJSONObject("data");
        ArrayList<FavoriteFolder> folderList = new ArrayList<>();
        if (data.has("list") && !data.isNull("list")) {
            JSONArray list = data.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject folder = list.getJSONObject(i);

                FavoriteFolder favoriteFolder = new FavoriteFolder();

                favoriteFolder.id = folder.getLong("fav_box");
                favoriteFolder.name = folder.getString("name");

                if (!folder.isNull("videos"))
                    favoriteFolder.cover = folder.getJSONArray("videos").getJSONObject(0).getString("pic");
                else favoriteFolder.cover = "";

                favoriteFolder.videoCount = folder.getInt("count");
                favoriteFolder.maxCount = folder.getInt("max_count");
                Log.e("debug-收藏夹ID", String.valueOf(favoriteFolder.id));
                Log.e("debug-收藏夹名称", favoriteFolder.name);
                Log.e("debug-收藏夹封面", favoriteFolder.cover);
                Log.e("debug-收藏夹视频数量", String.valueOf(favoriteFolder.videoCount));
                Log.e("debug-收藏夹视频上限", String.valueOf(favoriteFolder.maxCount));
                Log.e("debug-收藏夹", "----------------");
                folderList.add(favoriteFolder);
            }
        }
        return folderList;
    }

    /**
     * 获取收藏的合集
     *
     * @param mid            目标用户
     * @param page           页数
     * @param collectionList collection对象List
     * @return 返回码与has_more
     */
    public static Pair<Integer, Boolean> getFavoritedCollections(long mid, int page, List<Collection> collectionList) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/v3/fav/folder/collected/list" + new NetWorkUtil.FormData()
                .setUrlParam(true)
                .put("platform", "web")
                .put("up_mid", mid)
                .put("pn", page)
                .put("ps", 10);
        JSONObject result = NetWorkUtil.getJson(url);
        JSONObject data = result.optJSONObject("data");
        boolean has_more = false;
        if (data != null) {
            has_more = data.optBoolean("has_more", false);
            JSONArray list = data.optJSONArray("list");
            if (list != null) {
                for (int i = 0; i < list.length(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    Collection collection = new Collection();
                    collection.id = item.optInt("id", -1);
                    collection.mid = item.optLong("mid", -1);
                    collection.title = item.optString("title");
                    collection.cover = item.optString("cover");
                    collection.intro = item.optString("intro");
                    collection.view = ToolsUtil.toWan(item.optInt("view_count", -1));
                    collectionList.add(collection);
                }
            }
        }
        return new Pair<>(result.optInt("code", -1), has_more);
    }

    public static int getFolderVideos(long mid, long fid, int page, ArrayList<VideoCard> videoList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/space/fav/arc?vmid=" + mid
                + "&ps=30&fid=" + fid + "&tid=0&keyword=&pn=" + page + "&order=fav_time";
        JSONObject result = NetWorkUtil.getJson(url);
        JSONObject data = result.getJSONObject("data");
        if (data.has("archives") && !data.isNull("archives")) {
            JSONArray archives = data.getJSONArray("archives");
            if (archives.length() != 0) {
                for (int i = 0; i < archives.length(); i++) {
                    JSONObject video = archives.getJSONObject(i);
                    String title = video.getString("title");
                    String cover = video.getString("pic");
                    long aid = video.getLong("aid");

                    JSONObject owner = video.getJSONObject("owner");
                    String upName = owner.getString("name");

                    JSONObject stat = video.getJSONObject("stat");
                    String view = ToolsUtil.toWan(stat.getLong("view")) + "观看";
                    videoList.add(new VideoCard(title, upName, view, cover, aid, ""));
                }
                return 0;
            } else return 1;
        } else return -1;
    }

    public static ArrayList<Opus> getFavouriteOpus(int page) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/opus/favlist?page_size=10&page=" + page;
        JSONObject result = NetWorkUtil.getJson(url);
        Log.e("OpusFav", result.toString());
        boolean hasMore = result.getJSONObject("data").getBoolean("has_more");
        if (!hasMore) Log.e("图文", "没有更多啦");
        JSONArray items = result.getJSONObject("data").getJSONArray("items");
        ArrayList<Opus> list = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            Opus opus = new Opus();
            opus.content = item.getString("content");
            if (item.has("cover")) {
                opus.cover = item.getString("cover");
            } else opus.cover = "";
            if (item.has("title")) {
                opus.title = item.getString("title");
            } else {
                opus.title = item.getString("content");
            }
            opus.opusId = Long.parseLong(item.getString("opus_id"));
            opus.timeText = item.getString("time_text");
            list.add(opus);
        }
        return list;
    }

    public static void getFavoriteState(long aid, ArrayList<String> folderList, ArrayList<Long> fidList, ArrayList<Boolean> stateList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v3/fav/folder/created/list-all?type=2&jsonp=jsonp&rid=" + aid + "&up_mid=" + SharedPreferencesUtil.getLong("mid", 0);
        JSONObject result = NetWorkUtil.getJson(url);
        JSONObject data = result.getJSONObject("data");

        if (data.has("list") && !data.isNull("list")) {
            JSONArray list = data.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject folder = list.getJSONObject(i);
                folderList.add(folder.getString("title"));
                fidList.add(folder.getLong("fid"));
                stateList.add(folder.getInt("fav_state") == 1);
            }
        }
    }

    public static int addFavorite(long aid, long fid) throws IOException, JSONException {
        String strMid = String.valueOf(SharedPreferencesUtil.getLong("mid", 0));
        String addFid = fid + strMid.substring(strMid.length() - 2);
        String url = "https://api.bilibili.com/medialist/gateway/coll/resource/deal";
        String per = "rid=" + aid + "&type=2&add_media_ids=" + addFid + "&del_media_ids=&csrf=" + SharedPreferencesUtil.getString("csrf", "");

        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, per, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-添加收藏", result.toString());
        return result.getInt("code");
    }


    public static int deleteFavorite(long aid, long fid) throws IOException, JSONException {
        String strMid = String.valueOf(SharedPreferencesUtil.getLong("mid", 0));
        String delFid = fid + strMid.substring(strMid.length() - 2);    //腕上哔哩那边是错的，fid后面要加上mid的后两位而不是定值，虽然这不影响什么
        String url = "https://api.bilibili.com/medialist/gateway/coll/resource/batch/del";
        String per = "resources=" + aid + ":2&media_id=" + delFid + "&csrf=" + SharedPreferencesUtil.getString("csrf", "");

        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, per, NetWorkUtil.webHeaders).body()).string());
        Log.e("debug-删除收藏", result.toString());
        return result.getInt("code");
    }

}
