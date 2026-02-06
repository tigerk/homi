package com.homi.service.service.approval.event;

import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.common.lib.enums.approval.ApprovalInstanceStatusEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.enums.tenant.TenantCheckOutStatusEnum;
import com.homi.common.lib.enums.tenant.TenantStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.model.dao.entity.Lease;
import com.homi.model.dao.repo.HouseRepo;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.model.dao.repo.LeaseCheckoutRepo;
import com.homi.model.dao.repo.LeaseRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审批状态变更事件监听器
 * <p>
 * 负责在审批状态变更时更新对应业务表的状态。
 * <p>
 * 工作流程：
 * 1. 审批人处理审批（通过/驳回）或申请人撤回
 * 2. ApprovalService 发布 ApprovalStatusChangeEvent 事件
 * 3. 本监听器接收事件，更新业务表的 approval_status 和业务状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalEventListener {

    private final LeaseRepo leaseRepo;
    private final RoomRepo roomRepo;
    private final LeaseCheckoutRepo leaseCheckoutRepo;
    private final HouseRepo houseRepo;

    /**
     * 监听审批状态变更事件
     *
     * @ApplicationModuleListener 的工作流程：
     * 1. 发布事件时，Spring Modulith 将事件序列化并保存到 event_publication 表
     * 2. 等待发布事件的事务提交
     * 3. 在新事务中调用此方法
     * 4. 处理成功后，更新 event_publication 表的 completion_date
     * 5. 如果处理失败（抛出异常），Spring Modulith 会自动重试
     */
    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void handleApprovalStatusChange(ApprovalStatusChangeEvent event) {
        String bizType = event.getBizType();
        Long bizId = event.getBizId();
        Integer approvalStatus = event.getApprovalStatus();

        log.info("处理审批状态变更事件: bizType={}, bizId={}, approvalStatus={}, occurredAt={}", bizType, bizId, approvalStatus, event.getOccurredAt());

        ApprovalBizTypeEnum bizTypeEnum = ApprovalBizTypeEnum.getByCode(bizType);
        if (bizTypeEnum == null) {
            log.warn("未知的业务类型: {}, 跳过处理", bizType);
            // 不抛异常，标记为已处理
            return;
        }

        Integer bizApprovalStatus = convertToBizApprovalStatus(approvalStatus);

        try {
            switch (bizTypeEnum) {
                case TENANT_CHECKIN -> handleTenantCheckin(bizId, approvalStatus, bizApprovalStatus);
                case TENANT_CHECKOUT -> handleLeaseCheckout(bizId, approvalStatus, bizApprovalStatus);
                case HOUSE_CREATE -> handleHouseCreate(bizId, approvalStatus, bizApprovalStatus);
                default -> handleDefaultBiz(bizType, bizId, approvalStatus, bizApprovalStatus);
            }

            log.info("审批状态变更事件处理成功: bizType={}, bizId={}", bizType, bizId);
        } catch (Exception e) {
            log.error("审批状态变更事件处理失败，将自动重试: bizType={}, bizId={}", bizType, bizId, e);
            // 抛出异常，Spring Modulith 会自动重试
            throw new BizException("事件处理失败: " + e.getMessage());
        }
    }

    /**
     * 将审批实例状态转换为业务审批状态
     */
    private Integer convertToBizApprovalStatus(Integer approvalStatus) {
        if (ApprovalInstanceStatusEnum.PENDING.getCode().equals(approvalStatus)) {
            return BizApprovalStatusEnum.PENDING.getCode();
        } else if (ApprovalInstanceStatusEnum.APPROVED.getCode().equals(approvalStatus)) {
            return BizApprovalStatusEnum.APPROVED.getCode();
        } else if (ApprovalInstanceStatusEnum.REJECTED.getCode().equals(approvalStatus)) {
            return BizApprovalStatusEnum.REJECTED.getCode();
        } else if (ApprovalInstanceStatusEnum.WITHDRAWN.getCode().equals(approvalStatus)) {
            return BizApprovalStatusEnum.WITHDRAWN.getCode();
        }

        return BizApprovalStatusEnum.APPROVED.getCode();
    }

    // ==================== 各业务类型的处理方法 ====================

    /**
     * 处理租客入住审批
     */
    private void handleTenantCheckin(Long leaseId, Integer approvalStatus, Integer bizApprovalStatus) {
        // 1. 更新业务表的审批状态
        leaseRepo.updateApprovalStatus(leaseId, bizApprovalStatus);

        // 2. 根据审批结果更新业务状态
        if (ApprovalInstanceStatusEnum.APPROVED.getCode().equals(approvalStatus)) {
            // 审批通过 -> 租客状态改为生效
            leaseRepo.updateStatusById(leaseId, TenantStatusEnum.TO_SIGN.getCode());
            log.info("租客入住审批通过，已更新为待签约状态，允许租客签约合同: leaseId={}", leaseId);

            // TODO: 可以在这里执行审批通过后的业务逻辑，做一个通用的消息通知功能。
            // 发送通知给业务人员，审核已通过，可以让租客进行合同签字了。

        } else if (ApprovalInstanceStatusEnum.REJECTED.getCode().equals(approvalStatus)) {
            log.info("租客入住审批驳回: leaseId={}", leaseId);

            // 1. 审批驳回 -> 租客状态改为已取消，可以再次提交。
            leaseRepo.updateStatusById(leaseId, TenantStatusEnum.CANCELLED.getCode());

            Lease lease = leaseRepo.getById(leaseId);
            // 2. 更新房间状态为空置
            if (lease != null && lease.getRoomIds() != null) {
                roomRepo.updateRoomStatusByRoomIds(JSONUtil.toList(lease.getRoomIds(), Long.class), RoomStatusEnum.AVAILABLE.getCode());
            }

            // TODO: 可以发送驳回通知，发送给提交人，告诉他审批被驳回了。

        } else if (ApprovalInstanceStatusEnum.WITHDRAWN.getCode().equals(approvalStatus)) {
            // 撤回 -> 租客状态保持待签约，可重新提交
            log.info("租客入住审批撤回: leaseId={}", leaseId);
        }
    }

    /**
     * 处理退租审批
     */
    private void handleLeaseCheckout(Long checkoutId, Integer approvalStatus, Integer bizApprovalStatus) {
        // 1. 更新退租单的审批状态
        leaseCheckoutRepo.updateApprovalStatus(checkoutId, bizApprovalStatus);

        // 2. 根据审批结果处理
        if (ApprovalInstanceStatusEnum.APPROVED.getCode().equals(approvalStatus)) {
            // 审批通过 -> 执行退租流程
            leaseCheckoutRepo.updateStatus(checkoutId, TenantCheckOutStatusEnum.NORMAL_CHECK_OUT.getCode());
            log.info("退租审批通过: checkoutId={}", checkoutId);
            // TODO: 执行退租后续操作
            // 1. 更新租客状态为已退租
            // 2. 更新房间状态为空置
            // 3. 作废未付账单
        } else if (ApprovalInstanceStatusEnum.REJECTED.getCode().equals(approvalStatus)) {
            // 审批驳回 -> 退租单状态改为草稿，可重新编辑
            leaseCheckoutRepo.updateStatus(checkoutId, TenantCheckOutStatusEnum.UN_CHECK_OUT.getCode()); // 0=草稿
            log.info("退租审批驳回: checkoutId={}", checkoutId);

        } else if (ApprovalInstanceStatusEnum.WITHDRAWN.getCode().equals(approvalStatus)) {
            // 撤回 -> 退租单状态改为草稿
            leaseCheckoutRepo.updateStatus(checkoutId, TenantCheckOutStatusEnum.UN_CHECK_OUT.getCode()); // 0=草稿
            log.info("退租审批撤回: checkoutId={}", checkoutId);
        }
    }

    /**
     * 处理房源录入审批
     */
    private void handleHouseCreate(Long houseId, Integer approvalStatus, Integer bizApprovalStatus) {
        // TODO: 注入 HouseRepo 并实现
        if (ApprovalInstanceStatusEnum.APPROVED.getCode().equals(approvalStatus)) {
            // 审批通过 -> 更新房源状态为已发布
            log.info("房源录入审批通过: houseId={}", houseId);
        } else if (ApprovalInstanceStatusEnum.REJECTED.getCode().equals(approvalStatus)) {
            // 审批驳回
            log.info("房源录入审批驳回: houseId={}", houseId);
        }

        houseRepo.updateApprovalStatus(houseId, bizApprovalStatus);
    }

    /**
     * 默认处理（仅更新审批状态，不处理业务状态）
     */
    private void handleDefaultBiz(String bizType, Long bizId, Integer approvalStatus, Integer bizApprovalStatus) {
        // 如果业务表有 approval_status 字段，可以在这里通用处理
        // 但由于不知道具体是哪张表，这里只打印日志
        log.info("业务类型 {} 使用默认处理: bizId={}, approvalStatus={}, bizApprovalStatus={}", bizType, bizId, approvalStatus, bizApprovalStatus);

    }
}
