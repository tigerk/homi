package com.homi.admin.controller;

import com.homi.domain.base.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */


@RequiredArgsConstructor
@Slf4j
public class TestController {
    @PostMapping("/test")
    public ResponseResult<String> test() {
        return ResponseResult.ok("test");
    }
}
