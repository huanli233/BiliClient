package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.Opus;
import com.RobinNotBad.BiliClient.model.OpusParagraph;
import com.RobinNotBad.BiliClient.model.Stats;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.JsonUtil;
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

import okhttp3.Response;
import okhttp3.ResponseBody;

public class OpusApi {

    public static Opus getOpus(long id) throws IOException, JSONException {
        Opus opus = new Opus();
        opus.id = id;
        
        // 首先尝试判断是专栏还是动态
        if (isArticleId(id)) {
            // 专栏ID，使用ArticleApi获取数据
            opus.type = Opus.TYPE_ARTICLE;
            try {
                ArticleInfo articleInfo = ArticleApi.getArticle(id);
                if (articleInfo != null) {
                    // 将ArticleInfo转换为Opus格式
                    convertArticleInfoToOpus(opus, articleInfo);
                    return opus;
                } else {
                    // ArticleApi.getArticle返回null，使用HTML解析作为备用
                    return getOpusByHtmlParsing(id, true);
                }
            } catch (Exception e) {
                Logu.e("通过ArticleApi获取专栏失败，尝试备用方法: " + e.getMessage());
                // 备用方法：使用HTML解析
                return getOpusByHtmlParsing(id, true);
            }
        } else {
            // 动态ID，使用HTML解析
            opus.type = Opus.TYPE_DYNAMIC;
            return getOpusByHtmlParsing(id, false);
        }
    }
    
    private static boolean isArticleId(long id) {
        // 专栏ID通常小于100000000，且不以0开头
        return id > 0 && id < 100000000;
    }
    
    private static Opus getOpusByHtmlParsing(long id, boolean isArticle) throws IOException, JSONException {
        Opus opus = new Opus();
        opus.id = id;
        opus.type = isArticle ? Opus.TYPE_ARTICLE : Opus.TYPE_DYNAMIC;
        
        String url;
        if (isArticle) {
            url = "https://www.bilibili.com/read/cv" + id;
        } else {
            url = "https://www.bilibili.com/opus/" + id;
        }
        
        // 添加重试机制
        int retryCount = 0;
        final int maxRetries = 3;
        
        while (retryCount < maxRetries) {
            try {
                Response response = NetWorkUtil.get(url);
                
                // 处理专栏的重定向
                if (isArticle) {
                    String location = response.headers().get("location");
                    if (location != null && !location.isEmpty()) {
                        // 确保重定向URL是完整的
                        if (location.startsWith("//")) {
                            location = "https:" + location;
                        } else if (location.startsWith("/")) {
                            location = "https://www.bilibili.com" + location;
                        }
                        response = NetWorkUtil.get(location);
                    }
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new IOException("响应体为空");
                }

                String html = responseBody.string();
                
                // 检查是否返回了HTML错误页面
                if (html.trim().startsWith("<!DOCTYPE") || html.trim().startsWith("<html")) {
                    Logu.e("收到HTML页面而不是JSON数据，重试 " + (retryCount + 1) + "/" + maxRetries);
                    retryCount++;
                    if (retryCount < maxRetries) {
                        Thread.sleep(1000); // 等待1秒后重试
                        continue;
                    } else {
                        throw new IOException("API返回HTML页面而不是JSON数据");
                    }
                }
                
                // 检查是否包含错误页面
                if (html.contains("页面不存在") || html.contains("404")) {
                    throw new IOException("页面不存在");
                }

                String detailJson = JsonUtil.search(html, "detail", "");
                if (detailJson == null || detailJson.isEmpty()) {
                    throw new JSONException("未找到detail数据");
                }
                
                // 验证detailJson是否是有效的JSON
                if (detailJson.trim().startsWith("<!DOCTYPE") || detailJson.trim().startsWith("<html")) {
                    Logu.e("detail数据是HTML而不是JSON，重试 " + (retryCount + 1) + "/" + maxRetries);
                    retryCount++;
                    if (retryCount < maxRetries) {
                        Thread.sleep(1000); // 等待1秒后重试
                        continue;
                    } else {
                        throw new JSONException("detail数据是HTML而不是JSON");
                    }
                }
                
                JSONObject detail = new JSONObject(detailJson);

                JSONObject basic = detail.getJSONObject("basic");
                opus.commentId = Long.parseLong(basic.optString("comment_id_str", "0"));
                opus.commentType = basic.optInt("comment_type");

                if (detail.isNull("modules")) {
                    throw new JSONException("modules数据为空");
                }
                
                JSONArray modules = detail.getJSONArray("modules");

                for (int i = 0; i < modules.length(); i++) {
                    JSONObject module = modules.getJSONObject(i);
                    switch (module.optString("module_type")) {
                        case "MODULE_TYPE_TITLE":
                            opus.title = module.getJSONObject("module_title").getString("text");
                            break;
                        case "MODULE_TYPE_TOP":
                            ArrayList<String> topImages = new ArrayList<>();
                            JSONObject module_top = module.getJSONObject("module_top");
                            JSONObject display = module_top.getJSONObject("display");
                            int displayType = display.optInt("type");
                            if (displayType == 1) {
                                JSONObject album = display.getJSONObject("album");
                                JSONArray pics = album.getJSONArray("pics");
                                for (int j = 0; j < pics.length(); j++) {
                                    topImages.add(pics.getJSONObject(j).getString("url"));
                                }
                            }
                            opus.topImages = topImages;
                            break;
                        case "MODULE_TYPE_AUTHOR":
                            JSONObject module_author = module.getJSONObject("module_author");
                            UserInfo author = new UserInfo();
                            author.mid = module_author.getLong("mid");
                            author.name = module_author.getString("name");
                            author.followed = module_author.optBoolean("following", false);
                            author.avatar = module_author.getString("face");
                            if (!module_author.isNull("vip"))
                                author.vip_nickname_color = module_author.getJSONObject("vip").optString("nickname_color", "");

                            opus.pubTime = module_author.getString("pub_time");
                            opus.upInfo = author;
                            break;
                        case "MODULE_TYPE_CONTENT":
                            JSONArray paragraphs = module.getJSONObject("module_content").getJSONArray("paragraphs");
                            opus.paragraphs = analyzeParagraphs(paragraphs);
                            break;
                        case "MODULE_TYPE_STAT":
                            opus.stats = Stats.fromOpus(module.optJSONObject("module_stat"));
                            break;
                    }
                }

                if (opus.upInfo == null) opus.upInfo = new UserInfo();
                if (opus.stats == null) opus.stats = new Stats();
                
                opus.cover = "";
                return opus; // 成功，返回结果
                
            } catch (IllegalArgumentException e) {
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.contains("URL")) {
                    opus.type = Opus.TYPE_DYNAMIC_OLD_STYLE;
                    opus.cover = "";
                    return opus;
                } else {
                    throw new IOException("URL格式错误: " + errMsg, e);
                }
            } catch (Exception e) {
                // 其他异常，检查是否需要重试
                if (retryCount < maxRetries - 1) {
                    Logu.e("解析失败，重试 " + (retryCount + 1) + "/" + maxRetries + ": " + e.getMessage());
                    retryCount++;
                    try {
                        Thread.sleep(1000); // 等待1秒后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("线程被中断", ie);
                    }
                    continue;
                } else {
                    throw new IOException("解析HTML失败: " + e.getMessage(), e);
                }
            }
        }
        
        // 如果所有重试都失败
        throw new IOException("获取动态数据失败，已达到最大重试次数");
    }
    
    private static void convertArticleInfoToOpus(Opus opus, ArticleInfo articleInfo) {
        opus.title = articleInfo.title;
        opus.cover = articleInfo.banner;
        opus.content = articleInfo.content;
        
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
        // 改进的HTML到段落转换，支持图片
        ArrayList<OpusParagraph> paragraphs = new ArrayList<>();
        
        // 使用更智能的HTML解析
        // 首先处理图片
        java.util.regex.Pattern imgPattern = java.util.regex.Pattern.compile("<img[^>]+src=\"([^\"]+)\"[^>]*>", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher imgMatcher = imgPattern.matcher(html);
        
        int lastIndex = 0;
        while (imgMatcher.find()) {
            // 添加图片前的文本
            String textBefore = html.substring(lastIndex, imgMatcher.start()).trim();
            if (!textBefore.isEmpty()) {
                addTextParagraphs(paragraphs, textBefore);
            }
            
            // 添加图片
            String imgUrl = imgMatcher.group(1);
            if (imgUrl != null && !imgUrl.isEmpty()) {
                // 修复图片URL格式
                imgUrl = fixImageUrl(imgUrl);
                
                OpusParagraph imgParagraph = new OpusParagraph();
                imgParagraph.type = OpusParagraph.TYPE_PIC;
                imgParagraph.content = new String[]{imgUrl};
                paragraphs.add(imgParagraph);
            }
            
            lastIndex = imgMatcher.end();
        }
        
        // 添加剩余的文本
        String remainingText = html.substring(lastIndex).trim();
        if (!remainingText.isEmpty()) {
            addTextParagraphs(paragraphs, remainingText);
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
    
    private static void addTextParagraphs(ArrayList<OpusParagraph> paragraphs, String htmlText) {
        if (htmlText == null || htmlText.isEmpty()) {
            return;
        }

        // 将常见的换行/段落标签转换为真实换行，避免整段文本挤在一行
        String normalized = htmlText
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p\\s*>", "\n")
                .replaceAll("(?i)<p\\s*[^>]*>", "");

        // 移除HTML标签，保留纯文本
        String cleanText = normalized.replaceAll("<[^>]+>", "");
        cleanText = StringUtil.htmlToString(cleanText)
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();
        if (cleanText.isEmpty()) {
            return;
        }

        // 按换行分割文本
        String[] lines = cleanText.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                OpusParagraph paragraph = new OpusParagraph();
                paragraph.type = OpusParagraph.TYPE_TEXT;
                paragraph.content = line;
                paragraphs.add(paragraph);
            }
        }
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

        String dynamicType = item.getString("type");

        if (item.isNull("modules")) return;
        JSONObject modules = item.getJSONObject("modules");

        //up主信息
        UserInfo author = new UserInfo();
        if (!modules.isNull("module_author")) {
            JSONObject module_author = modules.getJSONObject("module_author");
            author.mid = module_author.getLong("mid");
            author.name = module_author.getString("name");
            author.followed = module_author.optBoolean("following", false);
            author.avatar = module_author.getString("face");
            if (!module_author.isNull("vip"))
                author.vip_nickname_color = module_author.getJSONObject("vip").optString("nickname_color", "");
            opus.pubTime = module_author.getString("pub_time");
        }
        opus.upInfo = author;

        if (dynamicType.equals("DYNAMIC_TYPE_NONE")) {
            opus.content = "[动态不存在]";
            return;
        }

        //动态主体内容
        JSONObject module_dynamic = modules.getJSONObject("module_dynamic");

        ArrayList<OpusParagraph> paragraphList = new ArrayList<>();

        if (!module_dynamic.isNull("desc")) {
            JSONObject object = new JSONObject();
            object.put("para_type", OpusParagraph.TYPE_TEXT_OPUS);
            object.put("data", module_dynamic.getJSONObject("desc").getJSONArray("rich_text_nodes"));
            paragraphList.add(new OpusParagraph(object));
        }

        if (!module_dynamic.isNull("major")) {
            JSONObject major = module_dynamic.getJSONObject("major");

            if (!major.isNull("opus")) {
                JSONObject dynamic_opus = major.getJSONObject("opus");
                JSONArray opus_pics = dynamic_opus.getJSONArray("pics");

                // 为了排版正常，这里必须把列表完整传递给OpusParagraph，让OpusParagraph那边解析
                // 这么干主要是为了适配这神秘的代码结构，我研究OpusParagraph的使用方法就研究了半天
                // by Moye

                JSONObject object = new JSONObject();
                object.put("para_type", OpusParagraph.TYPE_TEXT_OPUS);
                object.put("data", dynamic_opus.getJSONObject("summary").getJSONArray("rich_text_nodes"));
                paragraphList.add(new OpusParagraph(object));

                object = new JSONObject();
                object.put("para_type", OpusParagraph.TYPE_PIC);
                object.put("pic", new JSONObject().put("pics", opus_pics));
                paragraphList.add(new OpusParagraph(object));
            }

            if (!major.isNull("archive")) {
                // 这里是视频卡片
            }

        }

        opus.paragraphs = paragraphList.toArray(new OpusParagraph[0]);

        JSONObject module_stat = modules.getJSONObject("module_stat");
        Stats stats = new Stats();
        stats.reply = module_stat.getJSONObject("comment").getInt("count");
        stats.like = module_stat.getJSONObject("like").getInt("count");

        opus.stats = stats;
    }
}
