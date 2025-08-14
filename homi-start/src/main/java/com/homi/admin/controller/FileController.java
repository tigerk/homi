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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
@RestController
@RequestMapping("admin/file")
public class FileController {

    // 使用系统临时目录或指定的绝对路径
    @Value("${file.upload.path:#{systemProperties['java.io.tmpdir']}/uploads/}")
    private String uploadPath;

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

        // 创建上传目录（如果不存在）
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean mkdirs = uploadDir.mkdirs();
            if (!mkdirs) {
                log.error("创建上传目录失败: {}", uploadDir.getAbsolutePath());
            }
            log.info("创建上传目录: {}", uploadDir.getAbsolutePath());
        }

        // 保存文件
        Path targetPath = Paths.get(uploadPath, newFileName);
        file.transferTo(targetPath.toFile());

        log.info("文件上传成功: {}", targetPath.toAbsolutePath());

        // 返回可访问的 URL
        return ResponseResult.ok("上传成功", String.format("%s/uploads/%s", domain, newFileName));
    }
}