package com.homi.admin.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.homi.domain.base.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */

@RequiredArgsConstructor
@Slf4j
@RequestMapping("admin/file")
public class FileController {
    @Value("${spring.web.resources.static-locations}")
    private String uploadLocation;

    @PostMapping("/upload")
    public ResponseResult<String> uploadImage(HttpServletRequest request, @Valid @NonNull @RequestParam("file") MultipartFile file) throws IOException {
        String scheme = request.getScheme();             // http 或 https
        String serverName = request.getServerName();     // 域名或 IP
        int serverPort = request.getServerPort();        // 端口

        String domain = scheme + "://" + serverName + ":" + serverPort;

        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (CharSequenceUtil.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成新文件名
        String newFileName = UUID.randomUUID() + extension;

        // 保存文件
        Path targetPath = Paths.get(CharSequenceUtil.removePrefix(uploadLocation, "file:"), newFileName);
        file.transferTo(targetPath.toFile());

        // 返回可访问的 URL
        return ResponseResult.ok("上传成功", String.format("%s/%s", domain, newFileName));
    }
}
