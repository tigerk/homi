package com.homi.saas.web.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.homi.common.lib.response.ResponseResult;

import com.homi.model.dao.entity.FileMeta;
import com.homi.model.dao.repo.FileMetaRepo;
import com.homi.saas.web.config.LoginManager;
import com.homi.common.lib.enums.BooleanEnum;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.utils.ImageUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

/**
 * 应用于 homi-boot
 *
 * @author tigerk
 * @version v1.0
 * {@code @date} 2025/4/26
 */

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/saas/file")
public class FileController {

    // 使用系统临时目录或指定的绝对路径
    @Value("${file.upload.path:#{systemProperties['java.io.tmpdir']}/uploads/}")
    private String uploadPath;

    private static final Tika tika = new Tika();
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            // 图片
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/svg+xml",
            // 视频
            "video/mp4", "video/x-msvideo", "video/quicktime", "video/x-ms-wmv",
            "video/x-flv", "video/x-matroska", "video/webm",
            // 文档
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain", "text/csv", "application/csv"
    );
    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg",
            ".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm",
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".csv"
    );

    private final FileMetaRepo fileMetaRepo;

    /**
     * 上传文件接口
     *
     * @param request HTTP 请求
     * @param file    上传的文件
     * @return 返回文件访问 URL
     */
    @PostMapping("/upload")
    public ResponseResult<String> uploadFile(HttpServletRequest request, @Valid @NonNull @RequestParam("file") MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String extension = getSafeExtension(originalFilename);

        // 同时结合内容和文件名检测 MIME，避免 Office/PDF 等附件被误判为通用二进制。
        String detectedMimeType = tika.detect(file.getInputStream(), originalFilename);

        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType) || !ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            log.warn("不允许的文件类型: {}, 文件名: {}", detectedMimeType, file.getOriginalFilename());
            return ResponseResult.fail(ResponseCodeEnum.UPLOAD_FAIL.getCode(), "仅支持图片、视频、PDF、Word、Excel、PPT、TXT 或 CSV 文件");
        }

        String fileMD5 = ImageUtils.getFileMD5(file.getInputStream());
        // 查看是否上传过文件
        FileMeta fileByHash = fileMetaRepo.searchFileByHash(fileMD5);
        if (Objects.nonNull(fileByHash)) {
            return ResponseResult.ok("上传成功", fileByHash.getFileUrl());
        }

        // 生成新文件名（只使用 UUID + 扩展名，不包含任何用户输入的路径）
        String uuid = IdUtil.simpleUUID();
        String newFileName = uuid + extension;

        // 创建上传目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean mkdirs = uploadDir.mkdirs();
            if (!mkdirs) {
                log.error("创建上传目录失败: {}", uploadDir.getAbsolutePath());
                return ResponseResult.fail(ResponseCodeEnum.UPLOAD_FAIL.getCode(), "文件上传失败");
            }
            log.info("创建上传目录: {}", uploadDir.getAbsolutePath());
        }

        // 安全构建目标路径
        Path uploadDirPath = uploadDir.toPath().toRealPath(); // 获取规范化的绝对路径
        Path targetPath = uploadDirPath.resolve(newFileName).normalize(); // 解析并规范化

        // 关键安全检查：确保目标路径在上传目录内
        if (!targetPath.startsWith(uploadDirPath)) {
            log.error("检测到路径遍历攻击尝试: {}", targetPath);
            return ResponseResult.fail(ResponseCodeEnum.UPLOAD_FAIL.getCode(), "非法的文件路径");
        }

        // 检查文件是否已存在（虽然 UUID 冲突概率极低）
        if (Files.exists(targetPath)) {
            log.warn("文件已存在: {}", targetPath);
            return ResponseResult.fail(ResponseCodeEnum.UPLOAD_FAIL.getCode(), "文件已存在，请重试");
        }

        // 保存文件
        file.transferTo(targetPath.toFile());

        log.info("文件上传成功: {}", targetPath.toAbsolutePath());

        // 返回可访问的 URL
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String domain = scheme + "://" + serverName + ":" + serverPort;

        String fileUrl = String.format("%s/uploads/%s", domain, newFileName);

        // 保存上传的存储文件到表中，后期定期清理。
        FileMeta fileMeta = new FileMeta();
        fileMeta.setFileUrl(fileUrl);
        fileMeta.setFileName(newFileName);
        fileMeta.setFileHash(fileMD5);
        fileMeta.setFileType(detectedMimeType);
        fileMeta.setFileSize(file.getSize());
        fileMeta.setCreateBy(LoginManager.getUserId());
        fileMeta.setIsUsed(BooleanEnum.FALSE.getValue());
        fileMeta.setUpdateBy(LoginManager.getUserId());
        fileMetaRepo.save(fileMeta);

        return ResponseResult.ok("上传成功", fileUrl);
    }

    private String getSafeExtension(String originalFilename) {
        if (CharSequenceUtil.isBlank(originalFilename) || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
    }
}
