package com.homi;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/11
 */

import com.microsoft.playwright.Playwright;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PlaywrightDownloader implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("检查 Playwright 浏览器是否安装...");

        try (Playwright playwright = Playwright.create()) {
            // 这里会自动下载 Chromium，如果已经下载就不会重复下载
            playwright.chromium().executablePath();
        }

        System.out.println("Playwright 浏览器检查/下载完成。");
    }
}
