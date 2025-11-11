package com.homi.service.pdf;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.URLUtil;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.exception.BizException;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Margin;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
public class PdfService {
    private static final String A4_FORMAT = "A4";
    private static final boolean PRINT_BACKGROUND = true;
    private static final String MARGIN_TOP = "20mm";
    private static final String MARGIN_BOTTOM = "20mm";
    private static final String MARGIN_LEFT = "15mm";
    private static final String MARGIN_RIGHT = "15mm";

    public byte[] generatePdf(String html) {
        // 清理和修复HTML
        String cleanedHtml = cleanHtml(html);
        Document doc = Jsoup.parse(cleanedHtml);

        // 处理外部资源
        processExternalCss(doc);
        processExternalImages(doc);

        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(java.util.Arrays.asList(
                    "--disable-web-security",
                    "--disable-features=IsolateOrigins,site-per-process"
                ));

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // 设置视口大小，确保内容完整显示
                page.setViewportSize(1200, 1600);

                String htmlWithStyle = doc.html();

                // 使用 LOAD 而不是 NETWORKIDLE，并添加额外等待时间
                page.setContent(htmlWithStyle, new Page.SetContentOptions()
                    .setWaitUntil(WaitUntilState.LOAD));

                // 额外等待，确保所有内容渲染完成
                page.waitForTimeout(1000);

                // 评估页面高度，确保内容完整
                Object height = page.evaluate("() => document.body.scrollHeight");
                log.info("页面高度: {}", height);

                return page.pdf(new Page.PdfOptions()
                    .setFormat(A4_FORMAT)
                    .setPrintBackground(PRINT_BACKGROUND)
                    .setMargin(createMargin())
                    .setPreferCSSPageSize(false)  // 使用PDF选项而不是CSS页面大小
                );
            }
        } catch (Exception e) {
            log.error("生成PDF失败：{}", e.getMessage(), e);
            throw new BizException(ResponseCodeEnum.PDF_GENERATE_ERROR);
        }
    }

    /**
     * 清理HTML中的格式问题
     */
    private String cleanHtml(String html) {
        // 移除多余的闭合标签和修复格式问题
        html = html.replaceAll("</p>\\s*</p>", "</p>");
        html = html.replaceAll("<p>\\s*<p>", "<p>");

        // 确保HTML结构完整
        if (!html.contains("<!DOCTYPE")) {
            html = "<!DOCTYPE html>\n" + html;
        }

        return html;
    }

    /**
     * 处理外部 CSS 链接，将其内联到 HTML 中
     */
    private void processExternalCss(Document doc) {
        Elements links = doc.select("link[rel=stylesheet]");
        for (Element link : links) {
            String href = link.attr("href");
            if (href.startsWith("http")) {
                inlineCss(doc, link, href);
            }
            link.remove();
        }

        // 添加额外的PDF打印样式
        addPrintStyles(doc);
    }

    /**
     * 添加针对PDF打印的额外样式
     */
    private void addPrintStyles(Document doc) {
        String printStyles = """
            @media print {
                body {
                    -webkit-print-color-adjust: exact;
                    print-color-adjust: exact;
                }
                .previewContent {
                    page-break-inside: avoid;
                }
                h2, h3 {
                    page-break-after: avoid;
                }
                p {
                    orphans: 3;
                    widows: 3;
                }
            }
            /* 确保所有内容可见 */
            html, body {
                overflow: visible !important;
                height: auto !important;
            }
            .previewContent {
                overflow: visible !important;
                height: auto !important;
            }
            """;

        doc.head().appendElement("style")
            .attr("type", "text/css")
            .text(printStyles);
    }

    /**
     * 下载并内联 CSS
     */
    private void inlineCss(Document doc, Element link, String href) {
        try (InputStream inputStream = URLUtil.url(href).openStream()) {
            String css = IoUtil.read(inputStream, StandardCharsets.UTF_8);
            doc.head().appendElement("style")
                .attr("type", "text/css")
                .text(css);
        } catch (Exception e) {
            log.error("CSS 下载失败: {}", href, e);
        }
    }

    /**
     * 处理外部图片，将其转换为 Base64 内嵌
     */
    private void processExternalImages(Document doc) {
        Elements imgs = doc.select("img[src]");
        for (Element img : imgs) {
            String src = img.attr("src");
            if (src.startsWith("http")) {
                convertImageToBase64(img, src);
            }
        }
    }

    /**
     * 将图片转换为 Base64 格式
     */
    private void convertImageToBase64(Element img, String src) {
        try {
            byte[] bytes = URI.create(src).toURL().openStream().readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String mimeType = determineImageMimeType(src);
            img.attr("src", "data:image/" + mimeType + ";base64," + base64);
        } catch (IOException e) {
            log.error("图片下载失败: {}", src, e);
        }
    }

    /**
     * 根据文件扩展名确定图片 MIME 类型
     */
    private String determineImageMimeType(String src) {
        String lowerSrc = src.toLowerCase();
        if (lowerSrc.endsWith(".png")) {
            return "png";
        } else if (lowerSrc.endsWith(".jpg") || lowerSrc.endsWith(".jpeg")) {
            return "jpeg";
        } else if (lowerSrc.endsWith(".gif")) {
            return "gif";
        } else if (lowerSrc.endsWith(".webp")) {
            return "webp";
        }
        return "png";
    }

    /**
     * 创建 PDF 页边距配置
     */
    private Margin createMargin() {
        return new Margin()
            .setTop(MARGIN_TOP)
            .setBottom(MARGIN_BOTTOM)
            .setLeft(MARGIN_LEFT)
            .setRight(MARGIN_RIGHT);
    }
}
