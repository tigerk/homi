package com.homi.service.service.approval;

import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.common.lib.enums.approval.ApprovalStatusEnum;
import com.homi.common.lib.enums.tenant.TenantStatusEnum;
import com.homi.model.dao.repo.TenantCheckoutRepo;
import com.homi.model.dao.repo.TenantRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审批状态变更事件监听器
 * 负责在审批状态变更时更新对应业务表的状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalEventListener {

    private final TenantRepo tenantRepo;
    private final TenantCheckoutRepo tenantCheckoutRepo;
    // 可注入其他需要的 Repo

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalStatusChange(ApprovalStatusChangeEvent event) {
        String bizType = event.getBizType();
        Long bizId = event.getBizId();
        Integer approvalStatus = event.getApprovalStatus();

        log.info("收到审批状态变更事件: bizType={}, bizId={}, status={}", bizType, bizId, approvalStatus);

        ApprovalBizTypeEnum bizTypeEnum = ApprovalBizTypeEnum.getByCode(bizType);
        if (bizTypeEnum == null) {
            log.warn("未知的业务类型: {}", bizType);
            return;
        }

        switch (bizTypeEnum) {
            case TENANT_CHECKIN -> handleTenantCheckinApproval(bizId, approvalStatus);
            case TENANT_CHECKOUT -> handleTenantCheckoutApproval(bizId, approvalStatus);
            case HOUSE_CREATE -> handleHouseCreateApproval(bizId, approvalStatus);
            case CONTRACT_SIGN -> handleContractSignApproval(bizId, approvalStatus);
            default -> log.info("业务类型 {} 暂无特殊处理", bizType);
        }
    }

    /**
     * 处理租客入住审批
     */
    private void handleTenantCheckinApproval(Long tenantId, Integer approvalStatus) {
        if (ApprovalStatusEnum.APPROVED.getCode().equals(approvalStatus)) {
            // 审批通过 -> 更新租客状态为生效
            tenantRepo.updateStatusById(tenantId, TenantStatusEnum.EFFECTIVE.getCode());
            log.info("租客入住审批通过，租客ID: {}", tenantId);
        } else if (ApprovalStatusEnum.REJECTED.getCode().equals(approvalStatus)) {
            // 审批驳回 -> 可选择保持待签约状态或其他处理
            log.info("租客入住审批驳回，租客ID: {}", tenantId);
        } else if (ApprovalStatusEnum.WITHDRAWN.getCode().equals(approvalStatus)) {
            // 撤回 -> 恢复待签约状态
            tenantRepo.updateStatusById(tenantId, TenantStatusEnum.TO_SIGN.getCode());
            log.info("租客入住审批撤回，租客ID: {}", tenantId);
        }
    }

    /**
     * 处理退租审批
     */
    private void handleTenantCheckoutApproval(Long checkoutId, Integer approvalStatus) {
        if (ApprovalStatusEnum.APPROVED.getCode().equals(approvalStatus)) {
            // 审批通过 -> 更新退租单状态为已完成
            tenantCheckoutRepo.updateStatus(checkoutId, 2); // 2=已完成

            // TODO: 执行退租后续操作
            // 1. 更新租客状态为已退租
            // 2. 更新房间状态为空置
            // 3. 作废未付账单
            log.info("退租审批通过，退租单ID: {}", checkoutId);
        } else if (ApprovalStatusEnum.REJECTED.getCode().equals(approvalStatus)) {
            // 审批驳回 -> 更新退租单状态为草稿，允许重新编辑
            tenantCheckoutRepo.updateStatus(checkoutId, 0); // 0=草稿
            log.info("退租审批驳回，退租单ID: {}", checkoutId);
        } else if (ApprovalStatusEnum.WITHDRAWN.getCode().equals(approvalStatus)) {
            // 撤回 -> 更新退租单状态为草稿
            tenantCheckoutRepo.updateStatus(checkoutId, 0);
            log.info("退租审批撤回，退租单ID: {}", checkoutId);
        }
    }

    /**
     * 处理房源录入审批
     */
    private void handleHouseCreateApproval(Long houseId, Integer approvalStatus) {
        if (ApprovalStatusEnum.APPROVED.getCode().equals(approvalStatus)) {
            // TODO: 更新房源状态为已发布
            log.info("房源录入审批通过，房源ID: {}", houseId);
        } else if (ApprovalStatusEnum.REJECTED.getCode().equals(approvalStatus)) {
            // TODO: 更新房源状态为驳回
            log.info("房源录入审批驳回，房源ID: {}", houseId);
        }
    }

    /**
     * 处理合同签署审批
     */
    private void handleContractSignApproval(Long contractId, Integer approvalStatus) {
        if (ApprovalStatusEnum.APPROVED.getCode().equals(approvalStatus)) {
            // TODO: 更新合同状态
            log.info("合同签署审批通过，合同ID: {}", contractId);
        }
    }
}
