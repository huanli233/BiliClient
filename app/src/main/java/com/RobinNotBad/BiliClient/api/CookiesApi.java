package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.WindowManager;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.util.Cookies;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cookies相关API
 */
public class CookiesApi {

    public static  ArrayList<String> genWebHeaders(){
        return new ArrayList<>() {{
            addAll(NetWorkUtil.webHeaders);

            add("Sec-Fetch-Site");
            add("same-site");

            add("Sec-Fetch-Mode");
            add("cors");

            add("Sec-Fetch-Dest");
            add("empty");
        }};
    }

    /**
     * 调用ExClimbWuzhi API激活Cookies，并检查如果有一些可本地生成的Cookie没有就顺带生成一下
     * 注：payload是我瞎jb弄得
     *
     * @return 返回码
     */
    public static int activeCookieInfo() throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/internal/gaia-gateway/ExClimbWuzhi";
        //NetWorkUtil.postJson(url, genCookiePayload().toString(), genWebHeaders());    //b站自己请求两次，所以我也请求两次（？）
        return new JSONObject(Objects.requireNonNull(NetWorkUtil.postJson(url, genCookiePayload().toString(), genWebHeaders()).body()).string()).getInt("code");
    }

    public static JSONObject genCookiePayload() throws JSONException {
        JSONObject payload = new JSONObject("{\"5062\":0,\"39c8\":\"333.1007.fp.risk\",\"920b\":\"0\",\"df35\":\"UUID_HERE\",\"03bf\":\"https://www.bilibili.com/\",\"6e7c\":\"811x630\",\"3c43\":{\"2673\":0,\"5766\":24,\"6527\":0,\"7003\":1,\"807e\":1,\"b8ce\":\"UA_HERE\",\"641c\":0,\"07a4\":\"zh-CN\",\"1c57\":8,\"0bd0\":4,\"748e\":[768,1366],\"d61f\":[728,1366],\"fc9d\":-480,\"6aa9\":\"Asia/Shanghai\",\"75b8\":1,\"3b21\":1,\"8a1c\":0,\"d52f\":\"not available\",\"adca\":\"Win32\",\"80c9\":[[\"360SoftMgrPlugin\",\"360SoftMgrPlugin\",[[\"application/360softmgrplugin\",\"dll\"]]],[\"Alipay Security Control 3\",\"Alipay Security Control\",[[\"application/x-alisecctrl-plugin\",\"*\"]]],[\"Alipay security control\",\"npaliedit\",[[\"application/aliedit\",\"\"]]],[\"BaiduYunGuanjia Application\",\"YunWebDetect\",[[\"application/bd-npyunwebdetect-plugin\",\"\"]]],[\"Chromium PDF Plugin\",\"Portable Document Format\",[[\"application/x-google-chrome-pdf\",\"pdf\"]]],[\"Chromium PDF Viewer\",\"\",[[\"application/pdf\",\"pdf\"]]],[\"Java Deployment Toolkit 8.0.2910.10\",\"NPRuntime Script Plug-in Library for Java(TM) Deploy\",[[\"application/java-deployment-toolkit\",\"\"]]],[\"Java(TM) Platform SE 8 U291\",\"Next Generation Java Plug-in 11.291.2 for Mozilla browsers\",[[\"application/x-java-applet\",\"\"],[\"application/x-java-bean\",\"\"],[\"application/x-java-vm\",\"\"],[\"application/x-java-applet;version=1.1.1\",\"\"],[\"application/x-java-bean;version=1.1.1\",\"\"],[\"application/x-java-applet;version=1.1\",\"\"],[\"application/x-java-bean;version=1.1\",\"\"],[\"application/x-java-applet;version=1.2\",\"\"],[\"application/x-java-bean;version=1.2\",\"\"],[\"application/x-java-applet;version=1.1.3\",\"\"],[\"application/x-java-bean;version=1.1.3\",\"\"],[\"application/x-java-applet;version=1.1.2\",\"\"],[\"application/x-java-bean;version=1.1.2\",\"\"],[\"application/x-java-applet;version=1.3\",\"\"],[\"application/x-java-bean;version=1.3\",\"\"],[\"application/x-java-applet;version=1.2.2\",\"\"],[\"application/x-java-bean;version=1.2.2\",\"\"],[\"application/x-java-applet;version=1.2.1\",\"\"],[\"application/x-java-bean;version=1.2.1\",\"\"],[\"application/x-java-applet;version=1.3.1\",\"\"],[\"application/x-java-bean;version=1.3.1\",\"\"],[\"application/x-java-applet;version=1.4\",\"\"],[\"application/x-java-bean;version=1.4\",\"\"],[\"application/x-java-applet;version=1.4.1\",\"\"],[\"application/x-java-bean;version=1.4.1\",\"\"],[\"application/x-java-applet;version=1.4.2\",\"\"],[\"application/x-java-bean;version=1.4.2\",\"\"],[\"application/x-java-applet;version=1.5\",\"\"],[\"application/x-java-bean;version=1.5\",\"\"],[\"application/x-java-applet;version=1.6\",\"\"],[\"application/x-java-bean;version=1.6\",\"\"],[\"application/x-java-applet;version=1.7\",\"\"],[\"application/x-java-bean;version=1.7\",\"\"],[\"application/x-java-applet;version=1.8\",\"\"],[\"application/x-java-bean;version=1.8\",\"\"],[\"application/x-java-applet;jpi-version=1.8.0_291\",\"\"],[\"application/x-java-bean;jpi-version=1.8.0_291\",\"\"],[\"application/x-java-vm-npruntime\",\"\"],[\"application/x-java-applet;deploy=11.291.2\",\"\"],[\"application/x-java-applet;javafx=8.0.291\",\"\"]]],[\"Microsoft® Windows Media Player Firefox Plugin\",\"np-mswmp\",[[\"application/x-ms-wmp\",\"*\"],[\"application/asx\",\"*\"],[\"video/x-ms-asf-plugin\",\"*\"],[\"application/x-mplayer2\",\"*\"],[\"video/x-ms-asf\",\"asf,asx,*\"],[\"video/x-ms-wm\",\"wm,*\"],[\"audio/x-ms-wma\",\"wma,*\"],[\"audio/x-ms-wax\",\"wax,*\"],[\"video/x-ms-wmv\",\"wmv,*\"],[\"video/x-ms-wvx\",\"wvx,*\"]]],[\"QQÒôÀÖ²¥·Å¿Ø¼þ\",\"QQÒôÀÖ²¥·Å¿Ø¼þ\",[[\"application/tecent-qzonemusic-plugin\",\"rts\"]]],[\"Shockwave Flash\",\"Shockwave Flash 34.0 r0\",[[\"application/x-shockwave-flash\",\"swf\"],[\"application/futuresplash\",\"spl\"]]],[\"XunLei User Plugin\",\"Xunlei User scriptability Plugin,version= 2.0.2.3\",[[\"application/npxluser_plugin\",\"\"]]],[\"iTrusChina iTrusPTA,XEnroll,iEnroll,hwPTA,UKeyInstalls Firefox Plugin\",\"iTrusPTA&XEnroll hwPTA,IEnroll,UKeyInstalls for FireFox,version=1.0.0.2\",[[\"application/pta.itruspta.version.1\",\"*\"],[\"application/cenroll.cenroll.version.1\",\"\"],[\"application/itrusenroll.certenroll.version.1\",\"\"],[\"application/hwpta.itrushwpta\",\"\"],[\"application/hwwdkey.installwdkey\",\"\"],[\"application/hwepass2001.installepass2001\",\"\"]]],[\"npQQPhotoDrawEx\",\"npQQPhotoDrawEx Module\",[[\"application/tencent-qqphotodrawex2-plugin\",\"rts\"]]]],\"13ab\":\"hwAAAABJRU5ErkJggg==\",\"bfe9\":\"SAAskoALCSsZpEUcC+Av8DxpQVtSPLlMwAAAAASUVORK5CYII=\",\"a3c1\":[\"extensions:ANGLE_instanced_arrays;EXT_blend_minmax;EXT_clip_control;EXT_color_buffer_half_float;EXT_depth_clamp;EXT_disjoint_timer_query;EXT_float_blend;EXT_frag_depth;EXT_polygon_offset_clamp;EXT_shader_texture_lod;EXT_texture_compression_bptc;EXT_texture_compression_rgtc;EXT_texture_filter_anisotropic;EXT_sRGB;KHR_parallel_shader_compile;OES_element_index_uint;OES_fbo_render_mipmap;OES_standard_derivatives;OES_texture_float;OES_texture_float_linear;OES_texture_half_float;OES_texture_half_float_linear;OES_vertex_array_object;WEBGL_blend_func_extended;WEBGL_color_buffer_float;WEBGL_compressed_texture_s3tc;WEBGL_compressed_texture_s3tc_srgb;WEBGL_debug_renderer_info;WEBGL_debug_shaders;WEBGL_depth_texture;WEBGL_draw_buffers;WEBGL_lose_context;WEBGL_multi_draw;WEBGL_polygon_mode\",\"webgl aliased line width range:[1, 1]\",\"webgl aliased point size range:[1, 1024]\",\"webgl alpha bits:8\",\"webgl antialiasing:yes\",\"webgl blue bits:8\",\"webgl depth bits:24\",\"webgl green bits:8\",\"webgl max anisotropy:16\",\"webgl max combined texture image units:32\",\"webgl max cube map texture size:16384\",\"webgl max fragment uniform vectors:1024\",\"webgl max render buffer size:16384\",\"webgl max texture image units:16\",\"webgl max texture size:16384\",\"webgl max varying vectors:30\",\"webgl max vertex attribs:16\",\"webgl max vertex texture image units:16\",\"webgl max vertex uniform vectors:4096\",\"webgl max viewport dims:[32767, 32767]\",\"webgl red bits:8\",\"webgl renderer:WebKit WebGL\",\"webgl shading language version:WebGL GLSL ES 1.0 (OpenGL ES GLSL ES 1.0 Chromium)\",\"webgl stencil bits:0\",\"webgl vendor:WebKit\",\"webgl version:WebGL 1.0 (OpenGL ES 2.0 Chromium)\",\"webgl unmasked vendor:Google Inc. (Intel)\",\"webgl unmasked renderer:ANGLE (Intel, Intel(R) HD Graphics 4000 (0x00000166) Direct3D11 vs_5_0 ps_5_0, D3D11)\",\"webgl vertex shader high float precision:23\",\"webgl vertex shader high float precision rangeMin:127\",\"webgl vertex shader high float precision rangeMax:127\",\"webgl vertex shader medium float precision:23\",\"webgl vertex shader medium float precision rangeMin:127\",\"webgl vertex shader medium float precision rangeMax:127\",\"webgl vertex shader low float precision:23\",\"webgl vertex shader low float precision rangeMin:127\",\"webgl vertex shader low float precision rangeMax:127\",\"webgl fragment shader high float precision:23\",\"webgl fragment shader high float precision rangeMin:127\",\"webgl fragment shader high float precision rangeMax:127\",\"webgl fragment shader medium float precision:23\",\"webgl fragment shader medium float precision rangeMin:127\",\"webgl fragment shader medium float precision rangeMax:127\",\"webgl fragment shader low float precision:23\",\"webgl fragment shader low float precision rangeMin:127\",\"webgl fragment shader low float precision rangeMax:127\",\"webgl vertex shader high int precision:0\",\"webgl vertex shader high int precision rangeMin:31\",\"webgl vertex shader high int precision rangeMax:30\",\"webgl vertex shader medium int precision:0\",\"webgl vertex shader medium int precision rangeMin:31\",\"webgl vertex shader medium int precision rangeMax:30\",\"webgl vertex shader low int precision:0\",\"webgl vertex shader low int precision rangeMin:31\",\"webgl vertex shader low int precision rangeMax:30\",\"webgl fragment shader high int precision:0\",\"webgl fragment shader high int precision rangeMin:31\",\"webgl fragment shader high int precision rangeMax:30\",\"webgl fragment shader medium int precision:0\",\"webgl fragment shader medium int precision rangeMin:31\",\"webgl fragment shader medium int precision rangeMax:30\",\"webgl fragment shader low int precision:0\",\"webgl fragment shader low int precision rangeMin:31\",\"webgl fragment shader low int precision rangeMax:30\"],\"6bc5\":\"Google Inc. (Intel)~ANGLE (Intel, Intel(R) HD Graphics 4000 (0x00000166) Direct3D11 vs_5_0 ps_5_0, D3D11)\",\"ed31\":0,\"72bd\":0,\"097b\":0,\"52cd\":[0,0,0],\"a658\":[\"Arial\",\"Arial Black\",\"Arial Narrow\",\"Arial Unicode MS\",\"Book Antiqua\",\"Bookman Old Style\",\"Calibri\",\"Cambria\",\"Cambria Math\",\"Century\",\"Century Gothic\",\"Century Schoolbook\",\"Comic Sans MS\",\"Consolas\",\"Courier\",\"Courier New\",\"Georgia\",\"Helvetica\",\"Impact\",\"Lucida Bright\",\"Lucida Calligraphy\",\"Lucida Console\",\"Lucida Fax\",\"Lucida Handwriting\",\"Lucida Sans\",\"Lucida Sans Typewriter\",\"Lucida Sans Unicode\",\"Microsoft Sans Serif\",\"Monotype Corsiva\",\"MS Gothic\",\"MS PGothic\",\"MS Reference Sans Serif\",\"MS Sans Serif\",\"MS Serif\",\"MYRIAD PRO\",\"Palatino Linotype\",\"Segoe Print\",\"Segoe Script\",\"Segoe UI\",\"Segoe UI Light\",\"Segoe UI Semibold\",\"Segoe UI Symbol\",\"Tahoma\",\"Times\",\"Times New Roman\",\"Trebuchet MS\",\"Verdana\",\"Wingdings\",\"Wingdings 2\",\"Wingdings 3\"],\"d02f\":\"124.04347527516074\"}}");
        payload.put("5062", System.currentTimeMillis());
        Pair<Integer, Integer> resolution = gen_browser_resolution();
        payload.put("6e7c", resolution.first + "x" + resolution.second);
        payload.put("df35", NetWorkUtil.getCookies().getOrDefault("_uuid",""));
        payload.put("b8ce", NetWorkUtil.USER_AGENT_WEB);
        JSONArray resolutionArray = new JSONArray();
        resolutionArray.put(resolution.second);
        resolutionArray.put(resolution.first);
        payload.put("748e", resolutionArray);
        payload.put("d61f", resolutionArray);
        return new JSONObject().put("payload", payload.toString());
    }

    /**
     * 生成buvid3、buvid4
     *
     * @return buvid3、buvid4
     */
    public static Pair<String, String> getWebBuvids() throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/frontend/finger/spi";
        JSONObject data = NetWorkUtil.getJson(url, genWebHeaders()).getJSONObject("data");
        return new Pair<>(data.optString("b_3"), data.optString("b_4"));
    }

    /**
     * 生成bili_ticket
     *
     * @return bili_ticket and create time
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws JSONException
     */
    public static Pair<String, Integer> genBiliTicket() throws IOException, NoSuchAlgorithmException, InvalidKeyException, JSONException {
        long ts = System.currentTimeMillis() / 1000;
        String o = hmacSha256("XgwSnGZ1p", "ts" + ts);
        String url = "https://api.bilibili.com/bapis/bilibili.api.ticket.v1.Ticket/GenWebTicket";
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.postJson(url + new NetWorkUtil.FormData()
                        .setUrlParam(true)
                        .put("key_id", "ec02")
                        .put("hexsign", o)
                        .put("context[ts]", String.valueOf(ts))
                        .put("csrf", SharedPreferencesUtil.getString("csrf","")),
                "", genWebHeaders()).body()).string());
        if (result.has("data") && !result.isNull("data")) {
            JSONObject data = result.getJSONObject("data");
            return new Pair<>(data.optString("ticket"), data.optInt("created_at"));
        } else {
            return new Pair<>(null, -1);
        }
    }

    public static String hmacSha256(String key, String message)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hashBytes = sha256Hmac.doFinal(message.getBytes());
        StringBuilder hexHash = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexHash.append('0');
            hexHash.append(hex);
        }
        return hexHash.toString();
    }

    private static final Map<String, String> otherCookieMap = new HashMap<>() {{
        put("enable_web_push", "DISABLE");
        put("header_theme_version", "undefined");
        put("home_feed_column", "4");
        put("PVID", "1");
    }};

    public static void checkCookies() throws JSONException, IOException {
        Cookies cookies = NetWorkUtil.getCookies();

        // bili_ticket
        if (!cookies.containsKey("bili_ticket") || cookies.get("bili_ticket").equals("null") || !cookies.containsKey("bili_ticket_expires") || parseInt(cookies.get("bili_ticket_expires")) == null || parseInt(cookies.get("bili_ticket_expires")) < System.currentTimeMillis() / 1000) {
            try {
                Pair<String, Integer> bili_ticket = genBiliTicket();
                NetWorkUtil.putCookie("bili_ticket", bili_ticket.first);
                NetWorkUtil.putCookie("bili_ticket_expires", String.valueOf(bili_ticket.second + (3 * 24 * 60 * 60)));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }

        // _uuid
        if (!cookies.containsKey("_uuid")) {
            NetWorkUtil.putCookie("_uuid", gen_uuid_infoc());
        }

        // b_lsid
        if (!cookies.containsKey("b_lsid")) {
            NetWorkUtil.putCookie("b_lsid", gen_b_lsid());
        }

        // buvid_fp
        if (!cookies.containsKey("buvid_fp")) {
            NetWorkUtil.putCookie("buvid_fp", gen_buvid_fp(NetWorkUtil.USER_AGENT_WEB + System.currentTimeMillis(), 31));
        }

        // buvid3 & buvid4. Get from http API.
        if (!cookies.containsKey("buvid3") || !cookies.containsKey("buvid4")) {
            Pair<String, String> buvids = getWebBuvids();
            NetWorkUtil.putCookie("buvid3", buvids.first);
            NetWorkUtil.putCookie("buvid4", buvids.second);
        }

        // b_nut
        if (!cookies.containsKey("b_nut")) {
            NetWorkUtil.putCookie("b_nut", gen_b_nut());
        }

        // LIVE_BUVID
        if (!cookies.containsKey("LIVE_BUVID")) {
            long min = 1000000000000000L;
            long max = 9999999999999999L;
            NetWorkUtil.putCookie("LIVE_BUVID", "AUTO" + (min + (long) (new Random().nextDouble() * (max - min))));
        }

        // browser_resolution
        if (!cookies.containsKey("browser_resolution")) {
            Pair<Integer, Integer> resolution = gen_browser_resolution();
            NetWorkUtil.putCookie("browser_resolution", resolution.first + "-" + resolution.second);
        }

        // Others
        for (Map.Entry<String, String> entry : otherCookieMap.entrySet()) {
            if (!cookies.containsKey(entry.getKey())) {
                NetWorkUtil.putCookie(entry.getKey(), entry.getValue());
            }
        }

        NetWorkUtil.refreshHeaders();
    }

    private static Integer parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static final String[] MP = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "10"
    };
    private static final int[] PCK = {8, 4, 4, 4, 12};
    private static final String CHARSET = "0123456789ABCDEF";

    private static String gen_b_lsid() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        String randomString = sb.toString();
        long currentTimeMillis = System.currentTimeMillis();
        return randomString + "_" + Long.toHexString(currentTimeMillis).toUpperCase();
    }

    @SuppressLint("DefaultLocale")
    private static String gen_uuid_infoc() {
        long t = System.currentTimeMillis() % 100000;
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int len : PCK) {
            for (int i = 0; i < len; i++) {
                sb.append(MP[random.nextInt(16)]);
            }
            sb.append("-");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(String.format("%05d", t)).append("infoc");
        return sb.toString();
    }

    private static String gen_b_nut() {
        long timestampInSeconds = System.currentTimeMillis() / 1000;
        return String.valueOf(timestampInSeconds);
    }

    private static Pair<Integer, Integer> gen_browser_resolution(){
        WindowManager windowManager = (WindowManager) BiliTerminal.context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return new Pair<>(metrics.widthPixels, metrics.heightPixels);
    }

    private static final BigInteger MOD = BigInteger.ONE.shiftLeft(64);
    private static final BigInteger C1 = new BigInteger("87C37B91114253D5", 16);
    private static final BigInteger C2 = new BigInteger("4CF5AD432745937F", 16);
    private static final BigInteger C3 = BigInteger.valueOf(0x52DCE729L);
    private static final BigInteger C4 = BigInteger.valueOf(0x38495AB5L);
    private static final int R1 = 27;
    private static final int R2 = 31;
    private static final int R3 = 33;
    private static final int M = 5;

    public static String gen_buvid_fp(String key, long seed) throws IOException {
        InputStream source = new ByteArrayInputStream(key.getBytes("US-ASCII"));
        BigInteger m = murmur3_x64_128(source, BigInteger.valueOf(seed));
        return String.format("%016x%016x", m.mod(MOD), m.shiftRight(64).mod(MOD));
    }

    private static BigInteger rotateLeft(BigInteger x, int k) {
        return x.shiftLeft(k).or(x.shiftRight(64 - k)).mod(MOD);
    }

    private static BigInteger murmur3_x64_128(InputStream source, BigInteger seed) throws IOException {
        BigInteger h1 = seed;
        BigInteger h2 = seed;
        long processed = 0;
        byte[] buffer = new byte[16];
        while (true) {
            int bytesRead = source.read(buffer);
            processed += bytesRead;
            if (bytesRead == 16) {
                long k1 = ByteBuffer.wrap(buffer, 0, 8).getLong();
                long k2 = ByteBuffer.wrap(buffer, 8, 8).getLong();
                h1 = h1.xor(rotateLeft(BigInteger.valueOf(k1).multiply(C1).mod(MOD), R2).multiply(C2).mod(MOD));
                h1 = (rotateLeft(h1, R1).add(h2).multiply(BigInteger.valueOf(M)).add(C3)).mod(MOD);
                h2 = h2.xor(rotateLeft(BigInteger.valueOf(k2).multiply(C2).mod(MOD), R3).multiply(C1).mod(MOD));
                h2 = (rotateLeft(h2, R2).add(h1).multiply(BigInteger.valueOf(M)).add(C4)).mod(MOD);
            } else if (bytesRead == -1) {
                h1 = h1.xor(BigInteger.valueOf(processed));
                h2 = h2.xor(BigInteger.valueOf(processed));
                h1 = h1.add(h2).mod(MOD);
                h2 = h2.add(h1).mod(MOD);
                h1 = fmix64(h1);
                h2 = fmix64(h2);
                h1 = h1.add(h2).mod(MOD);
                h2 = h2.add(h1).mod(MOD);
                return h2.shiftLeft(64).or(h1);
            } else {
                long k1 = 0;
                long k2 = 0;
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                if (bytesRead >= 15) {
                    k2 ^= (long) byteBuffer.get(14) << 48;
                }
                if (bytesRead >= 14) {
                    k2 ^= (long) byteBuffer.get(13) << 40;
                }
                if (bytesRead >= 13) {
                    k2 ^= (long) byteBuffer.get(12) << 32;
                }
                if (bytesRead >= 12) {
                    k2 ^= (long) byteBuffer.get(11) << 24;
                }
                if (bytesRead >= 11) {
                    k2 ^= (long) byteBuffer.get(10) << 16;
                }
                if (bytesRead >= 10) {
                    k2 ^= (long) byteBuffer.get(9) << 8;
                }
                if (bytesRead >= 9) {
                    k2 ^= byteBuffer.get(8);
                    h2 = h2.xor(rotateLeft(BigInteger.valueOf(k2).multiply(C2).mod(MOD), R3).multiply(C1).mod(MOD));
                }
                if (bytesRead >= 8) {
                    k1 ^= (long) byteBuffer.get(7) << 56;
                }
                if (bytesRead >= 7) {
                    k1 ^= (long) byteBuffer.get(6) << 48;
                }
                if (bytesRead >= 6) {
                    k1 ^= (long) byteBuffer.get(5) << 40;
                }
                if (bytesRead >= 5) {
                    k1 ^= (long) byteBuffer.get(4) << 32;
                }
                if (bytesRead >= 4) {
                    k1 ^= (long) byteBuffer.get(3) << 24;
                }
                if (bytesRead >= 3) {
                    k1 ^= (long) byteBuffer.get(2) << 16;
                }
                if (bytesRead >= 2) {
                    k1 ^= (long) byteBuffer.get(1) << 8;
                }
                if (bytesRead >= 1) {
                    k1 ^= byteBuffer.get(0);
                    h1 = h1.xor(rotateLeft(BigInteger.valueOf(k1).multiply(C1).mod(MOD), R2));
                }
            }
        }
    }

    private static BigInteger fmix64(BigInteger k) {
        final BigInteger C1 = new BigInteger("FF51AFD7ED558CCD", 16);
        final BigInteger C2 = new BigInteger("C4CEB9FE1A85EC53", 16);
        final int R = 33;
        k = k.xor(k.shiftRight(R)).multiply(C1).mod(MOD);
        k = k.xor(k.shiftRight(R)).multiply(C2).mod(MOD);
        k = k.xor(k.shiftRight(R)).mod(MOD);
        return k;
    }
}
