package com.homi.saas.web.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.model.dao.entity.FileMeta;
import com.homi.model.dao.repo.FileMetaRepo;
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
    private final FileMetaRepo fileMetaRepo;

//    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点
    public void cleanUnusedFilesTask() {
        DateTime beforeYesterdayZero = DateUtil.beginOfDay(DateUtil.offsetDay(DateUtil.date(), -2));

        LambdaQueryWrapper<FileMeta> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(FileMeta::getCreateTime, beforeYesterdayZero);
        queryWrapper.eq(FileMeta::getIsUsed, StatusEnum.DISABLED.getValue());
        List<FileMeta> fileMetas = fileMetaRepo.list(queryWrapper);
        fileMetas.forEach(fileMeta -> {
            fileMetaRepo.getBaseMapper().deleteById(fileMeta.getId());
            log.info("删除未使用的图片: fileMeta={}", fileMeta);
        });
    }
}
