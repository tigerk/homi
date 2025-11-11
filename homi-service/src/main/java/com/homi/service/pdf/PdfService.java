package com.homi.service.pdf;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Margin;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.stereotype.Service;

@Service
public class PdfService {
    private static final String A4_FORMAT = "A4";
    private static final boolean PRINT_BACKGROUND = true;
    private static final String MARGIN_TOP = "20mm";
    private static final String MARGIN_BOTTOM = "20mm";
    private static final String MARGIN_LEFT = "15mm";
    private static final String MARGIN_RIGHT = "15mm";

    public byte[] generatePdf(String html) {
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            ); Page page = browser.newPage()) {
                String htmlWithStyle = createStyledHtml(html);
                page.setContent(htmlWithStyle, new Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

                return page.pdf(new Page.PdfOptions()
                    .setFormat(A4_FORMAT)
                    .setPrintBackground(PRINT_BACKGROUND)
                    .setMargin(new Margin()
                        .setTop(MARGIN_TOP)
                        .setBottom(MARGIN_BOTTOM)
                        .setLeft(MARGIN_LEFT)
                        .setRight(MARGIN_RIGHT)
                    )
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("生成PDF失败：" + e.getMessage(), e);
        }
    }

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
