package com.homi.service.service.approval;

import com.homi.model.approval.dto.ApprovalSubmitDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.function.LongConsumer;

/**
 * 审批流程模板
 * <p>
 * 使用示例：
 * <pre>{@code
 * approvalTemplate.submitIfNeed(
 *     ApprovalSubmitDTO.builder()
 *         .companyId(loginUser.getCurCompanyId())
 *         .bizType(ApprovalBizTypeEnum.TENANT_CHECKIN.getCode())
 *         .bizId(tenant.getId())
 *         .title("租客入住审批 - " + tenant.getName())
 *         .applicantId(loginUser.getId())
 *         .build(),
 *     // 需要审批时：更新为 PENDING
 *     bizId -> tenantRepo.updateApprovalStatus(bizId, BizApprovalStatusEnum.PENDING.getCode()),
 *     // 无需审批时：更新为 APPROVED 并设置业务状态为生效
 *     bizId -> {
 *         tenantRepo.updateApprovalStatus(bizId, BizApprovalStatusEnum.APPROVED.getCode());
 *         tenantRepo.updateStatusById(bizId, TenantStatusEnum.EFFECTIVE.getCode());
 *     }
 * );
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalTemplate {
    private final ApprovalService approvalService;

    /**
     * 提交审批（如果需要）
     *
     * @param dto              审批参数
     * @param onNeedApproval   需要审批时的回调（设置 approvalStatus = PENDING）
     * @param onNoNeedApproval 无需审批时的回调（设置 approvalStatus = APPROVED，并更新业务状态）
     * @return 审批结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ApprovalResult submitIfNeed(ApprovalSubmitDTO dto, LongConsumer onNeedApproval, LongConsumer onNoNeedApproval) {
        validateDTO(dto);

        boolean needApproval = needApproval(dto.getCompanyId(), dto.getBizType());

        if (needApproval) {
            // 1. 执行回调：更新审批状态为 PENDING
            if (onNeedApproval != null) {
                onNeedApproval.accept(dto.getBizId());
            }

            // 2. 提交审批
            Long instanceId = approvalService.submitApproval(dto);

            log.info("审批已提交: bizType={}, bizId={}, instanceId={}", dto.getBizType(), dto.getBizId(), instanceId);

            return ApprovalResult.pending(instanceId);
        } else {
            // 执行回调：更新审批状态为 APPROVED，并更新业务状态
            if (onNoNeedApproval != null) {
                onNoNeedApproval.accept(dto.getBizId());
            }

            log.info("无需审批，直接通过: bizType={}, bizId={}", dto.getBizType(), dto.getBizId());

            return ApprovalResult.approved();
        }
    }

    /**
     * 检查是否需要审批
     */
    public boolean needApproval(Long companyId, String bizType) {
        return approvalService.needApproval(companyId, bizType);
    }

    private void validateDTO(ApprovalSubmitDTO dto) {
        Assert.notNull(dto, "审批参数不能为空");
        Assert.notNull(dto.getCompanyId(), "公司ID不能为空");
        Assert.hasText(dto.getBizType(), "业务类型不能为空");
        Assert.notNull(dto.getBizId(), "业务ID不能为空");
        Assert.hasText(dto.getTitle(), "审批标题不能为空");
        Assert.notNull(dto.getApplicantId(), "申请人ID不能为空");
    }
}
