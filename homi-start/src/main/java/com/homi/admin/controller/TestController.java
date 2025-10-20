package com.homi.admin.controller;

import com.homi.job.FileClearJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */


@Slf4j
@RequestMapping("/admin/test")
@RestController
@RequiredArgsConstructor
public class TestController {
    private final FileClearJob fileClearJob;

    /**
     * 测试图片清理任务
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/20 11:17
     */
    @GetMapping("/clear")
    public void list() {
        fileClearJob.cleanUnusedFilesTask();
    }

}
