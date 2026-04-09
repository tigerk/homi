package com.homi.saas.web.job;

import com.homi.service.service.owner.OwnerBillGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 业主账单定时任务
 * <p>
 * 处理轻托管模式下的业主账单自动生成。
 */
@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class OwnerBillJob {
    private final OwnerBillGenerateService ownerBillGenerateService;

    /**
     * 自动生成轻托管起租日业
     * 主账单
     * 每天凌晨 1:10 执行一次
     */
    @Scheduled(cron = "0 10 1 * * ?")
    @SchedulerLock(name = "ownerBillJob.generateLeaseStartOwnerBillTask", lockAtMostFor = "PT20M", lockAtLeastFor = "PT30S")
    public void generateLeaseStartOwnerBillTask() {
        try {
            Integer lightManagedCount = ownerBillGenerateService.generateLeaseStartOwnerBills();
            if (lightManagedCount > 0) {
                log.info("自动生成轻托管业主账单成功，数量={}", lightManagedCount);
            }
        } catch (Exception e) {
            log.error("自动生成轻托管业主账单失败", e);
        }
    }
}
