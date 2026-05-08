package com.homi.saas.web.job;

import com.homi.service.service.owner.OwnerBillingGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 业主单据定时任务
 * <p>
 * 处理轻托管模式下的业主账单自动生成。
 */
@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class OwnerBillingJob {
    private final OwnerBillingGenerateService ownerBillingGenerateService;

    /**
     * 自动生成轻托管起租日业主结算单
     * 每天凌晨 1:10 执行一次
     */
    @Scheduled(cron = "0 10 1 * * ?")
    @SchedulerLock(name = "ownerBillingJob.generateLeaseStartSettlementBillTask", lockAtMostFor = "PT20M", lockAtLeastFor = "PT30S")
    public void generateLeaseStartSettlementBillTask() {
        try {
            Integer lightManagedCount = ownerBillingGenerateService.generateLeaseStartSettlementBills();
            if (lightManagedCount > 0) {
                log.info("自动生成轻托管业主结算单成功，数量={}", lightManagedCount);
            }
        } catch (Exception e) {
            log.error("自动生成轻托管业主结算单失败", e);
        }
    }

    /**
     * 补偿轻托管租客支付实时分账
     * 每天凌晨 1:30 执行一次
     */
    @Scheduled(cron = "0 30 1 * * ?")
    @SchedulerLock(name = "ownerBillingJob.compensateRealtimeSettlementBillTask", lockAtMostFor = "PT30M", lockAtLeastFor = "PT30S")
    public void compensateRealtimeSettlementBillTask() {
        try {
            Integer count = ownerBillingGenerateService.compensateRealtimeSettlementBills(null, null, null);
            if (count > 0) {
                log.info("补偿轻托管租客支付实时分账成功，数量={}", count);
            }
        } catch (Exception e) {
            log.error("补偿轻托管租客支付实时分账失败", e);
        }
    }
}
