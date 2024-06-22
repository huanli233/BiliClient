package com.RobinNotBad.BiliClient.util;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import okhttp3.HttpUrl;

public class DmImgParamUtil {

    public static Map<String, String> getDmImgParams() {
        HashMap<String, String> map = new HashMap<>();
        map.put("dm_img_str", "V2ViR0wgMS4wIChPcGVuR0wgRVMgMi4wIENocm9taXVtKQ");
        map.put("dm_cover_img_str", "QU5HTEUgKEludGVsLCBJbnRlbChSKSBVSEQgR3JhcGhpY3MgNjMwIERpcmVjdDNEMTEgdnNfNV8wIHBzXzVfMCwgRDNEMTEpR29vZ2xlIEluYy4gKEludGVsKQ");
        try {
            map.put("dm_img_list", generateDmImgList());
            map.put("dm_img_inter", generateDmImgInter());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static String getDmImgParamsUrl(String orig_url) {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(orig_url)).newBuilder();
        Map<String, String> params = getDmImgParams();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build().toString();
    }

    public static int[] f114i(int a, int b, int i) {
        Random random = new Random();
        int t = (int) (random.nextDouble() * (114 * i));
        return new int[]{3 * a + 2 * b + t, 4 * a - 5 * b + t, t};
    }

    public static int[] f114(int a, int b) {
        Random random = new Random();
        int t = (int) (random.nextDouble() * 114);
        return new int[]{2 * a + 2 * b + 3 * t, 4 * a - b + t, t};
    }

    public static int[] f514(int a, int b) {
        Random random = new Random();
        int t = (int) (random.nextDouble() * 514);
        return new int[]{3 * a + 2 * b + t, 4 * a - 4 * b + 2 * t, t};
    }

    private static final int[] timestamps = {2943, 3046, 3152, 3252, 3354, 3454, 3558, 3665, 3767, 5566, 5676, 5778, 5881, 7296, 7573, 135289};
    private static String generateDmImgList() throws JSONException {
        Random random = new Random();
        JSONArray dmImgListJson = new JSONArray();
        dmImgListJson.put(new JSONObject()
                .put("x", 2355)
                .put("y", 725)
                .put("z", 0)
                .put("timestamp", 2145)
                .put("k", (int) (random.nextDouble() * 67 + 60))
                .put("type", 0));
        int num = random.nextInt(timestamps.length - 6) + 6;
        for (int i = 0; i <= num; i++) {
            dmImgListJson.put(generateDmImgItem(random.nextInt(200), random.nextInt(200), i + 1, timestamps[i], i == num ? 1 : 0));
        }
        return dmImgListJson.toString();
    }

    private static JSONObject generateDmImgItem(int x, int y, int index, int timestamp, int type) {
        int[] xyz = f114i(x, y, index);
        Random random = new Random();
        try {
            return new JSONObject()
                    .put("x", xyz[0])
                    .put("y", xyz[1])
                    .put("z", xyz[2])
                    .put("timestamp", timestamp)
                    .put("k", (int) (random.nextDouble() * 67 + 60))
                    .put("type", type);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String[] classNames = {
            "clearfix", "g-search", "section", "loading", "full-rows", "n-btn", "router-link-exact-active", "router-link-active", "active"};
    private static String generateDmImgInter() throws JSONException {
        Random random = new Random();
        JSONObject result = new JSONObject();
        JSONArray ds = new JSONArray();
        int y = getRandomNumberInRange(-1500, -300);
        int x = getRandomNumberInRange(100, 700);
        int width = getRandomNumberInRange(100, 8000);
        int height = getRandomNumberInRange(20, 500);
        int[] xyz1 = f114(y, x);
        int[] xyz2 = f514(width, height);
        ds.put(new JSONObject()
                .put("t", random.nextInt(6))
                .put("c", Base64.encodeToString((classNames[random.nextInt(classNames.length)] + " " + classNames[random.nextInt(classNames.length)]).getBytes(), Base64.DEFAULT))
                .put("p", new JSONArray().put(xyz1[0]).put(xyz1[2]).put(xyz1[1]))
                .put("s", new JSONArray().put(xyz2[2]).put(xyz2[0]).put(xyz2[1])));
        result.put("ds", ds)
                .put("wh", new JSONArray(List.of(f114(width, height))))
                .put("of", new JSONArray(List.of(f514(y, x))));
        return result.toString();
    }

    private static int getRandomNumberInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("max must be greater than or equal to min");
        }
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

}
