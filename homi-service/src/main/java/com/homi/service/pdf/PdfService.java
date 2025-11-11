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
        Document doc = Jsoup.parse(html);

        // 处理外部资源
        processExternalCss(doc);
        processExternalImages(doc);

        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            ); Page page = browser.newPage()) {
                String htmlWithStyle = createStyledHtml(doc.html());
                page.setContent(htmlWithStyle, new Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

                return page.pdf(new Page.PdfOptions()
                    .setFormat(A4_FORMAT)
                    .setPrintBackground(PRINT_BACKGROUND)
                    .setMargin(createMargin())
                );
            }
        } catch (Exception e) {
            log.error("生成PDF失败：{}", e.getMessage(), e);

            throw new BizException(ResponseCodeEnum.PDF_GENERATE_ERROR);
        }
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
            // 使用 URI.toURL() 替代废弃的 URL(String) 构造函数
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
        return "png"; // 默认值
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

    /**
     * 创建带样式的完整 HTML
     */
    private String createStyledHtml(String html) {
        return """
            <html>
              <head>
                <meta charset='UTF-8'>
                <style>
                  body { font-family: "Noto Sans SC", "SimSun", sans-serif; margin: 20mm; }
                  h1, h2, h3 { color: #333; }
                  table { width: 100%; border-collapse: collapse; }
                  td, th { border: 1px solid #aaa; padding: 6px; }
                  .sign { border: 1px dashed #888; height: 80px; width: 200px; margin-top: 10px; }
                </style>
              </head>
              <body>
            """ + html + """
                  </body>
                </html>
            """;
    }
}
