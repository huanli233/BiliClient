package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.Opus;
import com.RobinNotBad.BiliClient.model.OpusParagraph;
import com.RobinNotBad.BiliClient.model.Stats;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.Logu;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OpusApi {

    public static Opus getOpus(long id) throws IOException, JSONException {
        Opus opus = new Opus();
        opus.id = id;
        
        // 首先尝试判断是专栏还是动态
        if (isArticleId(id)) {
            // 专栏ID，优先尝试 opus detail API 获取富文本数据
            opus.type = Opus.TYPE_ARTICLE;
            try {
                Logu.i("尝试通过 opus detail API 获取专栏富文本数据: cv" + id);
                return getArticleOpusDetail(id);
            } catch (Exception e) {
                // opus detail API 失败，回退到传统 ArticleApi（HTML）
                Logu.e("opus detail API 失败，回退到 HTML 方式: " + e.getMessage());
                try {
                    ArticleInfo articleInfo = ArticleApi.getArticle(id);
                    if (articleInfo != null) {
                        convertArticleInfoToOpus(opus, articleInfo);
                        return opus;
                    } else {
                        throw new IOException("ArticleApi 返回 null");
                    }
                } catch (Exception e2) {
                    Logu.e("HTML 方式也失败: " + e2.getMessage());
                    throw new IOException("无法获取专栏数据: " + e2.getMessage());
                }
            }
        } else {
            // 动态ID，使用API获取
            opus.type = Opus.TYPE_DYNAMIC;
            return getOpusByApi(id);
        }
    }
    
    private static boolean isArticleId(long id) {
        // 专栏ID通常小于100000000，且不以0开头
        return id > 0 && id < 100000000;
    }
    
    /**
     * 通过 opus detail API 获取专栏富文本数据
     * API: /x/polymer/web-dynamic/v1/opus/detail
     * 这个 API 返回的是结构化的 paragraphs 数据，包含富文本样式信息
     */
    private static Opus getArticleOpusDetail(long cvid) throws IOException, JSONException {
        Opus opus = new Opus();
        opus.id = cvid;
        opus.type = Opus.TYPE_ARTICLE;
        
        // 首先需要将 cvid 转换为 opus_id
        // 使用 ArticleApi 的转换方法
        Opus opusIdInfo = ArticleApi.opusId2cvid(cvid);
        if (opusIdInfo == null || opusIdInfo.id == 0) {
            throw new IOException("无法将 cv" + cvid + " 转换为 opus_id");
        }
        
        long opusId = opusIdInfo.id;
        Logu.i("cv" + cvid + " 对应的 opus_id: " + opusId);
        
        // 使用 opus detail API 获取富文本数据
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/opus/detail?id=" + opusId + "&timezone_offset=-480";
        JSONObject result = NetWorkUtil.getJson(url);
        
        if (result.optBoolean("retry_failed", false)) {
            throw new IOException(result.optString("message", "网络请求失败"));
        }
        
        int code = result.optInt("code", -1);
        if (code == 0 && result.has("data") && !result.isNull("data")) {
            JSONObject data = result.getJSONObject("data");
            
            // 检查是否有 item 字段（opus detail 格式）
            if (data.has("item") && !data.isNull("item")) {
                JSONObject item = data.getJSONObject("item");
                
                // 解析 basic 信息
                if (item.has("basic")) {
                    JSONObject basic = item.getJSONObject("basic");
                    opus.commentId = Long.parseLong(basic.optString("comment_id_str", String.valueOf(cvid)));
                    opus.commentType = basic.optInt("comment_type", 12);
                    opus.listId = extractListIdFromBasic(basic);
                }
                
                // 解析 modules（数组格式）
                if (item.has("modules") && !item.isNull("modules")) {
                    Object modulesObj = item.get("modules");
                    if (modulesObj instanceof JSONArray) {
                        parseModulesArray(opus, (JSONArray) modulesObj);
                    }
                }

                if (opus.listId <= 0) {
                    opus.listId = extractListIdFromItem(item);
                }

                if (opus.listId <= 0) {
                    try {
                        ArticleInfo listInfo = ArticleApi.getArticle(cvid);
                        if (listInfo != null) {
                            opus.listId = listInfo.listId;
                        }
                    } catch (Exception ignored) {
                    }
                }
                
                // 确保必要字段不为null
                if (opus.upInfo == null) opus.upInfo = new UserInfo();
                if (opus.stats == null) opus.stats = new Stats();
                if (opus.cover == null) opus.cover = "";
                
                Logu.i("成功通过 opus detail API 获取专栏富文本数据");
                return opus;
            }
        }
        
        String message = result.optString("message", "未知错误");
        throw new IOException("opus detail API 错误 (code=" + code + "): " + message);
    }
    
    /**
     * 通过B站API获取动态/图文详情
     * 使用 dynamic detail API: /x/polymer/web-dynamic/v1/detail
     * 参考PiliPlus项目的获取方法
     */
    private static Opus getOpusByApi(long id) throws IOException, JSONException {
        Opus opus = new Opus();
        opus.id = id;
        opus.type = isArticleId(id) ? Opus.TYPE_ARTICLE : Opus.TYPE_DYNAMIC;
        
        // 使用 dynamic detail API（带 itemOpusStyle 特性，支持图文/文字动态）
        try {
            String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/detail?timezone_offset=-480&id=" + id + "&features=itemOpusStyle,listOnlyfans";
            JSONObject result = NetWorkUtil.getJson(url);
            
            if (result.optBoolean("retry_failed", false)) {
                throw new IOException(result.optString("message", "网络请求失败"));
            }
            
            int code = result.optInt("code", -1);
            if (code == 0 && result.has("data") && !result.isNull("data")) {
                JSONObject data = result.getJSONObject("data");
                JSONObject item = data.getJSONObject("item");
                
                // 使用analyzeOldStyleDynamic方法解析动态数据为Opus格式
                opus.type = Opus.TYPE_DYNAMIC_OLD_STYLE;
                analyzeOldStyleDynamic(opus, item);
                return opus;
            }
            
            String message = result.optString("message", "未知错误");
            throw new IOException("API错误 (code=" + code + "): " + message);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("获取动态详情失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析opus detail API返回的数据
     * API: /x/polymer/web-dynamic/v1/opus/detail
     */
    private static Opus parseOpusDetailData(Opus opus, JSONObject data) throws JSONException {
        // opus detail API返回的数据结构包含 basic 和 modules 数组
        if (data.has("basic")) {
            JSONObject basic = data.getJSONObject("basic");
            opus.commentId = Long.parseLong(basic.optString("comment_id_str", "0"));
            opus.commentType = basic.optInt("comment_type");
        }
        
        if (data.has("modules") && !data.isNull("modules")) {
            // modules可能是数组格式（opus detail API）
            Object modulesObj = data.get("modules");
            if (modulesObj instanceof JSONArray) {
                JSONArray modules = (JSONArray) modulesObj;
                parseModulesArray(opus, modules);
            } else if (modulesObj instanceof JSONObject) {
                // modules是对象格式（dynamic detail API的格式）
                JSONObject modules = (JSONObject) modulesObj;
                parseModulesObject(opus, modules);
            }
        }
        
        // 确保必要字段不为null
        if (opus.upInfo == null) opus.upInfo = new UserInfo();
        if (opus.stats == null) opus.stats = new Stats();
        if (opus.cover == null) opus.cover = "";
        
        return opus;
    }
    
    /**
     * 解析modules数组格式（opus detail API返回的格式）
     */
    private static void parseModulesArray(Opus opus, JSONArray modules) throws JSONException {
        for (int i = 0; i < modules.length(); i++) {
            JSONObject module = modules.getJSONObject(i);
            String moduleType = module.optString("module_type");
            switch (moduleType) {
                case "MODULE_TYPE_TITLE":
                    if (module.has("module_title"))
                        opus.title = module.getJSONObject("module_title").optString("text");
                    break;
                case "MODULE_TYPE_TOP":
                    ArrayList<String> topImages = new ArrayList<>();
                    if (module.has("module_top")) {
                        JSONObject module_top = module.getJSONObject("module_top");
                        JSONObject display = module_top.optJSONObject("display");
                        if (display != null) {
                            int displayType = display.optInt("type");
                            if (displayType == 1) {
                                JSONObject album = display.optJSONObject("album");
                                if (album != null) {
                                    JSONArray pics = album.optJSONArray("pics");
                                    if (pics != null) {
                                        for (int j = 0; j < pics.length(); j++) {
                                            topImages.add(pics.getJSONObject(j).optString("url"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    opus.topImages = topImages;
                    break;
                case "MODULE_TYPE_AUTHOR":
                    if (module.has("module_author")) {
                        JSONObject module_author = module.getJSONObject("module_author");
                        UserInfo author = new UserInfo();
                        author.mid = module_author.optLong("mid");
                        author.name = module_author.optString("name");
                        author.followed = module_author.optBoolean("following", false);
                        author.avatar = module_author.optString("face");
                        if (!module_author.isNull("vip"))
                            author.vip_nickname_color = module_author.getJSONObject("vip").optString("nickname_color", "");
                        opus.pubTime = module_author.optString("pub_time");
                        opus.upInfo = author;
                    }
                    break;
                case "MODULE_TYPE_CONTENT":
                    if (module.has("module_content")) {
                        JSONObject moduleContent = module.getJSONObject("module_content");
                        if (moduleContent.has("paragraphs")) {
                            JSONArray paragraphs = moduleContent.getJSONArray("paragraphs");
                            opus.paragraphs = analyzeParagraphs(paragraphs);
                        }
                    }
                    break;
                case "MODULE_TYPE_STAT":
                    opus.stats = Stats.fromOpus(module.optJSONObject("module_stat"));
                    break;
            }
        }
    }
    
    /**
     * 解析modules对象格式（dynamic detail API返回的格式）
     * 将其转换为Opus的段落格式
     */
    private static void parseModulesObject(Opus opus, JSONObject modules) throws JSONException {
        // 解析作者信息
        if (!modules.isNull("module_author")) {
            JSONObject module_author = modules.getJSONObject("module_author");
            UserInfo author = new UserInfo();
            author.mid = module_author.optLong("mid");
            author.name = module_author.optString("name");
            author.followed = module_author.optBoolean("following", false);
            author.avatar = module_author.optString("face");
            if (!module_author.isNull("vip"))
                author.vip_nickname_color = module_author.getJSONObject("vip").optString("nickname_color", "");
            opus.pubTime = module_author.optString("pub_time");
            opus.upInfo = author;
        }
        
        // 解析动态内容
        ArrayList<OpusParagraph> paragraphList = new ArrayList<>();
        
        if (!modules.isNull("module_dynamic")) {
            JSONObject module_dynamic = modules.getJSONObject("module_dynamic");
            
            // 解析文字描述
            if (!module_dynamic.isNull("desc")) {
                JSONObject desc = module_dynamic.getJSONObject("desc");
                JSONArray richTextNodes = desc.optJSONArray("rich_text_nodes");
                if (richTextNodes != null) {
                    JSONObject textPara = new JSONObject();
                    textPara.put("para_type", OpusParagraph.TYPE_TEXT_OPUS);
                    textPara.put("data", richTextNodes);
                    paragraphList.add(new OpusParagraph(textPara));
                }
            }
            
            // 解析major内容（图片、视频等）
            if (!module_dynamic.isNull("major")) {
                JSONObject major = module_dynamic.getJSONObject("major");
                String majorType = major.optString("type");
                
                switch (majorType) {
                    case "MAJOR_TYPE_OPUS":
                        if (!major.isNull("opus")) {
                            JSONObject opusObj = major.getJSONObject("opus");
                            
                            // 标题
                            String title = opusObj.optString("title");
                            if (title != null && !title.isEmpty() && !"null".equals(title)) {
                                opus.title = title;
                            }
                            
                            // 文字内容
                            JSONObject summary = opusObj.optJSONObject("summary");
                            if (summary != null) {
                                JSONArray richTextNodes = summary.optJSONArray("rich_text_nodes");
                                if (richTextNodes != null) {
                                    JSONObject textPara = new JSONObject();
                                    textPara.put("para_type", OpusParagraph.TYPE_TEXT_OPUS);
                                    textPara.put("data", richTextNodes);
                                    paragraphList.add(new OpusParagraph(textPara));
                                }
                            }
                            
                            // 图片
                            JSONArray pics = opusObj.optJSONArray("pics");
                            if (pics != null && pics.length() > 0) {
                                JSONObject picPara = new JSONObject();
                                picPara.put("para_type", OpusParagraph.TYPE_PIC);
                                picPara.put("pic", new JSONObject().put("pics", pics));
                                paragraphList.add(new OpusParagraph(picPara));
                            }
                        }
                        break;
                    case "MAJOR_TYPE_DRAW":
                        if (!major.isNull("draw")) {
                            JSONObject draw = major.getJSONObject("draw");
                            JSONArray items = draw.optJSONArray("items");
                            if (items != null && items.length() > 0) {
                                // 将draw items转换为pics格式
                                JSONArray pics = new JSONArray();
                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject item = items.getJSONObject(i);
                                    JSONObject pic = new JSONObject();
                                    pic.put("url", item.optString("src"));
                                    pics.put(pic);
                                }
                                JSONObject picPara = new JSONObject();
                                picPara.put("para_type", OpusParagraph.TYPE_PIC);
                                picPara.put("pic", new JSONObject().put("pics", pics));
                                paragraphList.add(new OpusParagraph(picPara));
                            }
                        }
                        break;
                }
            }
        }
        
        // 解析统计信息
        if (!modules.isNull("module_stat")) {
            JSONObject module_stat = modules.getJSONObject("module_stat");
            Stats stats = new Stats();
            if (module_stat.has("comment"))
                stats.reply = module_stat.getJSONObject("comment").optInt("count");
            if (module_stat.has("like"))
                stats.like = module_stat.getJSONObject("like").optInt("count");
            opus.stats = stats;
        }
        
        opus.paragraphs = paragraphList.toArray(new OpusParagraph[0]);
    }
    
    private static void convertArticleInfoToOpus(Opus opus, ArticleInfo articleInfo) {
        opus.title = articleInfo.title;
        opus.cover = articleInfo.banner;
        opus.content = articleInfo.content;
        opus.listId = articleInfo.listId;
        
        // 修复时间格式 - 将时间戳转换为可读格式
        if (articleInfo.ctime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
            opus.pubTime = sdf.format(articleInfo.ctime * 1000);
        } else {
            opus.pubTime = "";
        }
        
        opus.upInfo = articleInfo.upInfo;
        
        // 确保stats对象存在
        if (articleInfo.stats != null) {
            opus.stats = articleInfo.stats;
        } else {
            opus.stats = new Stats();
        }
        
        // 将HTML内容转换为段落（包含图片）
        if (articleInfo.content != null && !articleInfo.content.isEmpty()) {
            opus.paragraphs = convertHtmlToParagraphs(articleInfo.content);
        }
        
        // 设置评论信息
        opus.commentId = articleInfo.id;
        opus.commentType = 12; // 专栏的评论类型通常是12
    }
    
    private static OpusParagraph[] convertHtmlToParagraphs(String content) {
        // 首先尝试解析为JSON格式（新版动态/专栏）
        try {
            return parseJsonContent(content);
        } catch (Exception e) {
            // JSON解析失败，回退到HTML解析
            Logu.e("JSON解析失败，尝试HTML解析: " + e.getMessage());
            return parseHtmlContent(content);
        }
    }
    
    private static OpusParagraph[] parseJsonContent(String jsonContent) {
        ArrayList<OpusParagraph> paragraphs = new ArrayList<>();
        
        try {
            JSONObject json = new JSONObject(jsonContent);
            
            // 检查是否是ops格式（你提供的示例格式）
            if (json.has("ops")) {
                JSONArray ops = json.getJSONArray("ops");
                for (int i = 0; i < ops.length(); i++) {
                    JSONObject op = ops.getJSONObject(i);
                    
                    // 处理文本
                    if (op.has("insert") && op.get("insert") instanceof String) {
                        String text = op.getString("insert");
                        if (!text.trim().isEmpty() && !text.equals("\n")) {
                            OpusParagraph textParagraph = new OpusParagraph();
                            textParagraph.type = OpusParagraph.TYPE_TEXT;
                            textParagraph.content = text.trim();
                            paragraphs.add(textParagraph);
                        }
                    }
                    
                    // 处理图片
                    if (op.has("insert") && op.get("insert") instanceof JSONObject) {
                        JSONObject insertObj = op.getJSONObject("insert");
                        if (insertObj.has("native-image")) {
                            JSONObject nativeImage = insertObj.getJSONObject("native-image");
                            if (nativeImage.has("url")) {
                                String imgUrl = fixImageUrl(nativeImage.getString("url"));
                                if (imgUrl != null && !imgUrl.isEmpty()) {
                                    OpusParagraph imgParagraph = new OpusParagraph();
                                    imgParagraph.type = OpusParagraph.TYPE_PIC;
                                    imgParagraph.content = new String[]{imgUrl};
                                    paragraphs.add(imgParagraph);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException("JSON解析异常", e);
        }
        
        return paragraphs.toArray(new OpusParagraph[0]);
    }
    
    private static OpusParagraph[] parseHtmlContent(String html) {
        ArrayList<OpusParagraph> paragraphs = new ArrayList<>();

        java.util.regex.Pattern imgPattern =
                java.util.regex.Pattern.compile("<figure[^>]*>(.*?)</figure>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher figureMatcher = imgPattern.matcher(html);

        int lastIndex = 0;
        while (figureMatcher.find()) {
            String before = html.substring(lastIndex, figureMatcher.start()).trim();
            if (!before.isEmpty()) {
                addTextParagraphs(paragraphs, before, false);
            }

            String figureHtml = figureMatcher.group(1);

            java.util.regex.Matcher imgMatcher =
                    java.util.regex.Pattern.compile("<img[^>]+src=\"([^\"]+)\"", java.util.regex.Pattern.CASE_INSENSITIVE)
                            .matcher(figureHtml);

            if (imgMatcher.find()) {
                String imgUrl = fixImageUrl(imgMatcher.group(1));
                OpusParagraph imgParagraph = new OpusParagraph();
                imgParagraph.type = OpusParagraph.TYPE_PIC;
                imgParagraph.content = new String[]{imgUrl};
                paragraphs.add(imgParagraph);
            }

            java.util.regex.Matcher captionMatcher =
                    java.util.regex.Pattern.compile("<figcaption[^>]*>(.*?)</figcaption>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL)
                            .matcher(figureHtml);

            if (captionMatcher.find()) {
                String captionHtml = captionMatcher.group(1).trim();
                if (!captionHtml.isEmpty()) {
                    addTextParagraphs(paragraphs, captionHtml, true);
                }
            }

            lastIndex = figureMatcher.end();
        }

        String remaining = html.substring(lastIndex).trim();
        if (!remaining.isEmpty()) {
            addTextParagraphs(paragraphs, remaining, false);
        }

        return paragraphs.toArray(new OpusParagraph[0]);
    }
    
    private static String fixImageUrl(String imgUrl) {
        if (imgUrl == null || imgUrl.isEmpty()) {
            return imgUrl;
        }
        
        // 如果URL以//开头，添加https:
        if (imgUrl.startsWith("//")) {
            return "https:" + imgUrl;
        }
        
        // 如果URL以/开头，添加B站域名
        if (imgUrl.startsWith("/")) {
            return "https://www.bilibili.com" + imgUrl;
        }
        
        // 如果URL没有协议，添加https://
        if (!imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
            return "https://" + imgUrl;
        }
        
        return imgUrl;
    }
    
    private static void addTextParagraphs(ArrayList<OpusParagraph> paragraphs, String htmlText, boolean isCaption) {
        if (htmlText == null || htmlText.isEmpty()) {
            return;
        }

        htmlText = htmlText
                .replaceAll("class=\"color-green-02\"", "style=\"color:#60d837\"")
                .replaceAll("class=\"color-blue-01\"", "style=\"color:#23ade5\"")
                .replaceAll("class=\"color-pink-01\"", "style=\"color:#fb7299\"")
                .replaceAll("class=\"color-gray-01\"", "style=\"color:#999999\"");

        CharSequence spanned;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = android.text.Html.fromHtml(htmlText, android.text.Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = android.text.Html.fromHtml(htmlText);
        }

        if (!(spanned instanceof android.text.Spanned) || spanned.length() == 0) {
            return;
        }

        android.text.SpannableStringBuilder ssb = new android.text.SpannableStringBuilder(spanned);

        android.text.style.ForegroundColorSpan[] colorSpans =
                ssb.getSpans(0, ssb.length(), android.text.style.ForegroundColorSpan.class);

        for (android.text.style.ForegroundColorSpan span : colorSpans) {
            int color = span.getForegroundColor();
            if (android.graphics.Color.luminance(color) < 0.35) {
                ssb.removeSpan(span);
            }
        }

        if (isCaption) {
            ssb.setSpan(
                    new android.text.style.AlignmentSpan.Standard(android.text.Layout.Alignment.ALIGN_CENTER),
                    0,
                    ssb.length(),
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        OpusParagraph paragraph = new OpusParagraph();
        paragraph.type = OpusParagraph.TYPE_TEXT;
        paragraph.content = ssb;
        paragraphs.add(paragraph);
    }

    public static OpusParagraph[] analyzeParagraphs(JSONArray jsonArray) throws JSONException {
        OpusParagraph[] paragraphs = new OpusParagraph[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject paragraphJson = jsonArray.getJSONObject(i);
            OpusParagraph paragraph = new OpusParagraph(paragraphJson);
            paragraphs[i] = paragraph;
        }
        return paragraphs;
    }

    public static void analyzeOldStyleDynamic(Opus opus, JSONObject item) throws JSONException {
        JSONObject basic = item.getJSONObject("basic");
        opus.commentId = Long.parseLong(basic.optString("comment_id_str", "0"));
        opus.commentType = basic.optInt("comment_type");

        String dynamicType = item.optString("type", "");

        if (item.isNull("modules")) return;
        JSONObject modules = item.getJSONObject("modules");

        //up主信息
        UserInfo author = new UserInfo();
        if (!modules.isNull("module_author")) {
            JSONObject module_author = modules.getJSONObject("module_author");
            author.mid = module_author.optLong("mid");
            author.name = module_author.optString("name", "");
            author.followed = module_author.optBoolean("following", false);
            author.avatar = module_author.optString("face", "");
            if (!module_author.isNull("vip"))
                author.vip_nickname_color = module_author.getJSONObject("vip").optString("nickname_color", "");
            opus.pubTime = module_author.optString("pub_time", "");
        }
        opus.upInfo = author;

        if (dynamicType.equals("DYNAMIC_TYPE_NONE")) {
            opus.content = "[动态不存在]";
            return;
        }

        //动态主体内容
        if (modules.isNull("module_dynamic")) return;
        JSONObject module_dynamic = modules.getJSONObject("module_dynamic");

        ArrayList<OpusParagraph> paragraphList = new ArrayList<>();

        if (!module_dynamic.isNull("desc")) {
            JSONObject desc = module_dynamic.getJSONObject("desc");
            JSONArray richTextNodes = desc.optJSONArray("rich_text_nodes");
            if (richTextNodes != null) {
                JSONObject object = new JSONObject();
                object.put("para_type", OpusParagraph.TYPE_TEXT_OPUS);
                object.put("data", richTextNodes);
                paragraphList.add(new OpusParagraph(object));
            }
        }

        if (!module_dynamic.isNull("major")) {
            JSONObject major = module_dynamic.getJSONObject("major");

            if (!major.isNull("opus")) {
                JSONObject dynamic_opus = major.getJSONObject("opus");

                // 为了排版正常，这里必须把列表完整传递给OpusParagraph，让OpusParagraph那边解析
                // 这么干主要是为了适配这神秘的代码结构，我研究OpusParagraph的使用方法就研究了半天
                // by Moye

                // 标题
                String title = dynamic_opus.optString("title", "");
                if (!title.isEmpty() && !"null".equals(title)) {
                    opus.title = title;
                }

                // 文字内容
                JSONObject summary = dynamic_opus.optJSONObject("summary");
                if (summary != null) {
                    JSONArray richTextNodes = summary.optJSONArray("rich_text_nodes");
                    if (richTextNodes != null) {
                        JSONObject object = new JSONObject();
                        object.put("para_type", OpusParagraph.TYPE_TEXT_OPUS);
                        object.put("data", richTextNodes);
                        paragraphList.add(new OpusParagraph(object));
                    }
                }

                // 图片（可能不存在，纯文字动态没有图片）
                JSONArray opus_pics = dynamic_opus.optJSONArray("pics");
                if (opus_pics != null && opus_pics.length() > 0) {
                    JSONObject object = new JSONObject();
                    object.put("para_type", OpusParagraph.TYPE_PIC);
                    object.put("pic", new JSONObject().put("pics", opus_pics));
                    paragraphList.add(new OpusParagraph(object));
                }
            }

            // MAJOR_TYPE_DRAW 类型（旧版图片动态格式）
            if (!major.isNull("draw")) {
                JSONObject draw = major.getJSONObject("draw");
                JSONArray items = draw.optJSONArray("items");
                if (items != null && items.length() > 0) {
                    JSONArray pics = new JSONArray();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject drawItem = items.getJSONObject(i);
                        JSONObject pic = new JSONObject();
                        pic.put("url", drawItem.optString("src"));
                        pics.put(pic);
                    }
                    JSONObject object = new JSONObject();
                    object.put("para_type", OpusParagraph.TYPE_PIC);
                    object.put("pic", new JSONObject().put("pics", pics));
                    paragraphList.add(new OpusParagraph(object));
                }
            }

            if (!major.isNull("archive")) {
                // 这里是视频卡片
            }

        }

        opus.paragraphs = paragraphList.toArray(new OpusParagraph[0]);

        // 解析统计信息（使用安全方式）
        Stats stats = new Stats();
        if (!modules.isNull("module_stat")) {
            JSONObject module_stat = modules.getJSONObject("module_stat");
            if (module_stat.has("comment"))
                stats.reply = module_stat.getJSONObject("comment").optInt("count", 0);
            if (module_stat.has("like"))
                stats.like = module_stat.getJSONObject("like").optInt("count", 0);
        }
        opus.stats = stats;
    }

    private static long extractListIdFromBasic(JSONObject basic) {
        if (basic == null) {
            return 0;
        }
        String[] keys = new String[]{
                "list_id",
                "list_id_str",
                "collection_id",
                "collection_id_str",
                "article_list_id",
                "article_list_id_str",
                "listId",
                "collectionId"
        };
        return findIdInJson(basic, keys);
    }

    private static long extractListIdFromItem(JSONObject item) {
        if (item == null) {
            return 0;
        }
        JSONObject list = item.optJSONObject("list");
        if (list != null) {
            long id = list.optLong("id", 0);
            if (id > 0) {
                return id;
            }
        }
        Object modulesObj = item.opt("modules");
        if (modulesObj instanceof JSONArray) {
            JSONArray modules = (JSONArray) modulesObj;
            for (int i = 0; i < modules.length(); i++) {
                JSONObject module = modules.optJSONObject(i);
                if (module == null) {
                    continue;
                }
                long moduleId = extractListIdFromModule(module);
                if (moduleId > 0) {
                    return moduleId;
                }
            }
        }
        String[] keys = new String[]{
                "list_id",
                "list_id_str",
                "collection_id",
                "collection_id_str",
                "article_list_id",
                "article_list_id_str",
                "listId",
                "collectionId"
        };
        return findIdInJson(item, keys);
    }

    private static long extractListIdFromModule(JSONObject module) {
        String moduleType = module.optString("module_type", "");
        if (!moduleType.isEmpty()) {
            String lower = moduleType.toLowerCase(Locale.ROOT);
            if (lower.contains("list") || lower.contains("collect")) {
                long id = findIdInJson(module, new String[]{
                        "list_id",
                        "list_id_str",
                        "collection_id",
                        "collection_id_str",
                        "article_list_id",
                        "article_list_id_str",
                        "listId",
                        "collectionId"
                });
                if (id > 0) {
                    return id;
                }
            }
        }
        return 0;
    }

    private static long findIdInJson(Object node, String[] keys) {
        java.util.HashSet<String> keySet = new java.util.HashSet<>();
        for (String key : keys) {
            keySet.add(key);
        }
        return findIdInJson(node, keySet);
    }

    private static long findIdInJson(Object node, java.util.Set<String> keySet) {
        if (node == null) {
            return 0;
        }
        if (node instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) node;
            java.util.Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object value = jsonObject.opt(key);
                if (keySet.contains(key)) {
                    long parsed = parseIdValue(value);
                    if (parsed > 0) {
                        return parsed;
                    }
                }
                long nested = findIdInJson(value, keySet);
                if (nested > 0) {
                    return nested;
                }
            }
        } else if (node instanceof JSONArray) {
            JSONArray array = (JSONArray) node;
            for (int i = 0; i < array.length(); i++) {
                long nested = findIdInJson(array.opt(i), keySet);
                if (nested > 0) {
                    return nested;
                }
            }
        }
        return 0;
    }

    private static long parseIdValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        String raw = value.toString().trim();
        if (raw.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
        }
        String digits = raw.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
