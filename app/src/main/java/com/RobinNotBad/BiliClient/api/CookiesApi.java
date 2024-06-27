package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.util.Pair;

import com.RobinNotBad.BiliClient.util.Cookies;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

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

    private static final List<String> mWebHeaders = new ArrayList<>() {{
        addAll(NetWorkUtil.webHeaders);
        add("Sec-Ch-Ua");
        add("\"Chromium\";v=\"109\", \"Not_A Brand\";v=\"99\"");
        add("Sec-Ch-Ua-Platform");
        add("\"Windows\"");
        add("Sec-Ch-Ua-Mobile");
        add("?0");
        add("Sec-Fetch-Site");
        add("same-site");
        add("Sec-Fetch-Mode");
        add("cors");
        add("Sec-Fetch-Dest");
        add("empty");
    }};

    /**
     * 调用ExClimbWuzhi API激活Cookies，并检查如果有一些可本地生成的Cookie没有就顺带生成一下
     * 注：payload是我瞎jb弄得
     *
     * @return 返回码
     */
    public static int activeCookieInfo() throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/internal/gaia-gateway/ExClimbWuzhi";
        checkCookies();
        @SuppressLint("DefaultLocale") JSONObject payload = new JSONObject(
                String.format(
                        "{\"payload\":\"{\\\"3064\\\":1,\\\"5062\\\":\\\"%d\\\",\\\"03bf\\\":\\\"https%%3A%%2F%%2Fwww.bilibili.com%%2F\\\",\\\"39c8\\\":\\\"333.1193.fp.risk\\\",\\\"34f1\\\":\\\"\\\",\\\"d402\\\":\\\"\\\",\\\"654a\\\":\\\"\\\",\\\"6e7c\\\":\\\"0x0\\\",\\\"3c43\\\":{\\\"2673\\\":0,\\\"5766\\\":24,\\\"6527\\\":0,\\\"7003\\\":1,\\\"807e\\\":1,\\\"b8ce\\\":\\\"%s\\\",\\\"641c\\\":1,\\\"07a4\\\":\\\"zh-CN\\\",\\\"1c57\\\":8,\\\"0bd0\\\":12,\\\"748e\\\":[830,1475],\\\"d61f\\\":[783,1475],\\\"fc9d\\\":-480,\\\"6aa9\\\":\\\"Asia/Shanghai\\\",\\\"75b8\\\":1,\\\"3b21\\\":1,\\\"8a1c\\\":1,\\\"d52f\\\":\\\"not available\\\",\\\"adca\\\":\\\"Win32\\\",\\\"80c9\\\":[[\\\"Chromium PDF Plugin\\\",\\\"Portable Document Format\\\",[[\\\"application/x-google-chrome-pdf\\\",\\\"pdf\\\"]]],[\\\"Chromium PDF Viewer\\\",\\\"\\\",[[\\\"application/pdf\\\",\\\"pdf\\\"]]]],\\\"13ab\\\":\\\"mCaDAAAAAElFTkSuQmCC\\\",\\\"bfe9\\\":\\\"EKJKMJaErGahJFAfsK/A/GlBW1/fBxgwAAAABJRU5ErkJggg==\\\",\\\"a3c1\\\":[\\\"extensions:ANGLE_instanced_arrays;EXT_blend_minmax;EXT_color_buffer_half_float;EXT_disjoint_timer_query;EXT_float_blend;EXT_frag_depth;EXT_shader_texture_lod;EXT_texture_compression_bptc;EXT_texture_compression_rgtc;EXT_texture_filter_anisotropic;EXT_sRGB;KHR_parallel_shader_compile;OES_element_index_uint;OES_fbo_render_mipmap;OES_standard_derivatives;OES_texture_float;OES_texture_float_linear;OES_texture_half_float;OES_texture_half_float_linear;OES_vertex_array_object;WEBGL_color_buffer_float;WEBGL_compressed_texture_s3tc;WEBGL_compressed_texture_s3tc_srgb;WEBGL_debug_renderer_info;WEBGL_debug_shaders;WEBGL_depth_texture;WEBGL_draw_buffers;WEBGL_lose_context;WEBGL_multi_draw\\\",\\\"webgl aliased line width range:[1, 1]\\\",\\\"webgl aliased point size range:[1, 1024]\\\",\\\"webgl alpha bits:8\\\",\\\"webgl antialiasing:yes\\\",\\\"webgl blue bits:8\\\",\\\"webgl depth bits:24\\\",\\\"webgl green bits:8\\\",\\\"webgl max anisotropy:16\\\",\\\"webgl max combined texture image units:32\\\",\\\"webgl max cube map texture size:16384\\\",\\\"webgl max fragment uniform vectors:1024\\\",\\\"webgl max render buffer size:16384\\\",\\\"webgl max texture image units:16\\\",\\\"webgl max texture size:16384\\\",\\\"webgl max varying vectors:30\\\",\\\"webgl max vertex attribs:16\\\",\\\"webgl max vertex texture image units:16\\\",\\\"webgl max vertex uniform vectors:4096\\\",\\\"webgl max viewport dims:[32767, 32767]\\\",\\\"webgl red bits:8\\\",\\\"webgl renderer:WebKit WebGL\\\",\\\"webgl shading language version:WebGL GLSL ES 1.0 (OpenGL ES GLSL ES 1.0 Chromium)\\\",\\\"webgl stencil bits:0\\\",\\\"webgl vendor:WebKit\\\",\\\"webgl version:WebGL 1.0 (OpenGL ES 2.0 Chromium)\\\",\\\"webgl unmasked vendor:Google Inc. (Intel)\\\",\\\"webgl unmasked renderer:ANGLE (Intel, Intel(R) UHD Graphics 630 Direct3D11 vs_5_0 ps_5_0, D3D11)\\\",\\\"webgl vertex shader high float precision:23\\\",\\\"webgl vertex shader high float precision rangeMin:127\\\",\\\"webgl vertex shader high float precision rangeMax:127\\\",\\\"webgl vertex shader medium float precision:23\\\",\\\"webgl vertex shader medium float precision rangeMin:127\\\",\\\"webgl vertex shader medium float precision rangeMax:127\\\",\\\"webgl vertex shader low float precision:23\\\",\\\"webgl vertex shader low float precision rangeMin:127\\\",\\\"webgl vertex shader low float precision rangeMax:127\\\",\\\"webgl fragment shader high float precision:23\\\",\\\"webgl fragment shader high float precision rangeMin:127\\\",\\\"webgl fragment shader high float precision rangeMax:127\\\",\\\"webgl fragment shader medium float precision:23\\\",\\\"webgl fragment shader medium float precision rangeMin:127\\\",\\\"webgl fragment shader medium float precision rangeMax:127\\\",\\\"webgl fragment shader low float precision:23\\\",\\\"webgl fragment shader low float precision rangeMin:127\\\",\\\"webgl fragment shader low float precision rangeMax:127\\\",\\\"webgl vertex shader high int precision:0\\\",\\\"webgl vertex shader high int precision rangeMin:31\\\",\\\"webgl vertex shader high int precision rangeMax:30\\\",\\\"webgl vertex shader medium int precision:0\\\",\\\"webgl vertex shader medium int precision rangeMin:31\\\",\\\"webgl vertex shader medium int precision rangeMax:30\\\",\\\"webgl vertex shader low int precision:0\\\",\\\"webgl vertex shader low int precision rangeMin:31\\\",\\\"webgl vertex shader low int precision rangeMax:30\\\",\\\"webgl fragment shader high int precision:0\\\",\\\"webgl fragment shader high int precision rangeMin:31\\\",\\\"webgl fragment shader high int precision rangeMax:30\\\",\\\"webgl fragment shader medium int precision:0\\\",\\\"webgl fragment shader medium int precision rangeMin:31\\\",\\\"webgl fragment shader medium int precision rangeMax:30\\\",\\\"webgl fragment shader low int precision:0\\\",\\\"webgl fragment shader low int precision rangeMin:31\\\",\\\"webgl fragment shader low int precision rangeMax:30\\\"],\\\"6bc5\\\":\\\"Google Inc. (Intel)~ANGLE (Intel, Intel(R) UHD Graphics 630 Direct3D11 vs_5_0 ps_5_0, D3D11)\\\",\\\"ed31\\\":0,\\\"72bd\\\":0,\\\"097b\\\":0,\\\"52cd\\\":[0,0,0],\\\"a658\\\":[],\\\"d02f\\\":\\\"124.04347527516074\\\"},\\\"54ef\\\":\\\"{}\\\",\\\"8b94\\\":\\\"https%%3A%%2F%%2Fwww.bilibili.com%%2F\\\",\\\"df35\\\":\\\"%s\\\",\\\"07a4\\\":\\\"zh-CN\\\",\\\"5f45\\\":null,\\\"db46\\\":0}\"}",
                        System.currentTimeMillis(), NetWorkUtil.USER_AGENT_WEB, NetWorkUtil.getCookies().getOrDefault("_uuid", "")
                )
        );
        return new JSONObject(Objects.requireNonNull(NetWorkUtil.postJson(url, payload.toString(), mWebHeaders).body()).string()).getInt("code");
    }

    /**
     * 生成buvid3、buvid4
     *
     * @return buvid3、buvid4
     */
    public static Pair<String, String> getWebBuvids() throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/frontend/finger/spi";
        JSONObject data = NetWorkUtil.getJson(url).getJSONObject("data");
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
        int ts = (int) (System.currentTimeMillis() / 1000);
        String o = hmacSha256("XgwSnGZ1p", "ts" + ts);
        String url = "https://api.bilibili.com/bapis/bilibili.api.ticket.v1.Ticket/GenWebTicket";
        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.postJson(url + new NetWorkUtil.FormData()
                        .setUrlParam(true)
                        .put("key_id", "ec02")
                        .put("hexsign", o)
                        .put("context[ts]", String.valueOf(ts))
                        .put("csrf", ""),
                "", new ArrayList<>() {{
                    add("User-Agent");
                    add(NetWorkUtil.USER_AGENT_WEB);
                }}).body()).string());
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
        put("browser_resolution", "839-959");
    }};

    public static void checkCookies() throws JSONException, IOException {
        Cookies cookies = NetWorkUtil.getCookies();

        // bili_ticket
        if (!cookies.containsKey("bili_ticket") || cookies.get("bili_ticket").equals("null") || !cookies.containsKey("bili_ticket_expires") || parseInt(cookies.get("bili_ticket_expires")) == null || parseInt(cookies.get("bili_ticket_expires")) < (int) (System.currentTimeMillis() / 1000)) {
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

        // buvid_fp. Hardcoded.
        if (!cookies.containsKey("buvid_fp")) {
            NetWorkUtil.putCookie("buvid_fp", /* gen_buvid_fp(NetWorkUtil.USER_AGENT_WEB + System.currentTimeMillis(), 31) */ "30c3020be6cee8345ddc4c3c6b77f60f");
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
        InputStream source = new ByteArrayInputStream(key.getBytes("ascii"));
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
