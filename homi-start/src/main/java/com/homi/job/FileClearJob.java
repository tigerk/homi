package com.homi.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.model.entity.UploadedFile;
import com.homi.model.repo.UploadedFileRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/20
 */

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class FileClearJob {
    private final UploadedFileRepo uploadedFileRepo;

    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点
    public void cleanUnusedFilesTask() {
        DateTime beforeYesterdayZero = DateUtil.beginOfDay(DateUtil.offsetDay(DateUtil.date(), -2));

        LambdaQueryWrapper<UploadedFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(UploadedFile::getCreateTime, beforeYesterdayZero);
        queryWrapper.eq(UploadedFile::getIsUsed, StatusEnum.DISABLED.getValue());
        List<UploadedFile> uploadedFiles = uploadedFileRepo.list(queryWrapper);
        uploadedFiles.forEach(uploadedFile -> {
            uploadedFileRepo.getBaseMapper().deleteById(uploadedFile.getId());
            log.info("删除未使用的图片: uploadedFile={}", uploadedFile);
        });
    }
}
