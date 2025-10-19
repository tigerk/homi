package com.homi.admin.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.enums.common.BooleanEnum;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.model.entity.TempFileResource;
import com.homi.model.repo.TempFileResourceRepo;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
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

    private static final Tika tika = new Tika();
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            // 图片
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/svg+xml",
            // 视频
            "video/mp4", "video/x-msvideo", "video/quicktime", "video/x-ms-wmv",
            "video/x-flv", "video/x-matroska", "video/webm"
    );

    private final TempFileResourceRepo tempFileResourceRepo;

    /**
     * 上传文件接口
     *
     * @param request HTTP 请求
     * @param file    上传的文件
     * @return 返回文件访问 URL
     */
    @PostMapping("/upload")
    public ResponseResult<String> uploadImage(HttpServletRequest request, @Valid @NonNull @RequestParam("file") MultipartFile file) throws IOException {

        // 检测真实的 MIME 类型
        String detectedMimeType = tika.detect(file.getInputStream());

        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
            log.warn("不允许的文件类型: {}, 文件名: {}", detectedMimeType, file.getOriginalFilename());
            return ResponseResult.fail(ResponseCodeEnum.UPLOAD_FAIL.getCode(), "只允许上传图片或视频文件");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (CharSequenceUtil.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // 生成新文件名（只使用 UUID + 扩展名，不包含任何用户输入的路径）
        String newFileName = UUID.randomUUID() + extension;

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
        TempFileResource tempFileResource = new TempFileResource();
        tempFileResource.setFileUrl(fileUrl);
        tempFileResource.setCreateBy(LoginManager.getUserId());
        tempFileResource.setIsUsed(BooleanEnum.FALSE.getValue());
        tempFileResource.setUpdateBy(LoginManager.getUserId());
        tempFileResourceRepo.save(tempFileResource);

        return ResponseResult.ok("上传成功", fileUrl);
    }
}