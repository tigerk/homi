package com.homi.service.service.lease.bill;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.finance.PaymentFlowStatusEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.repo.LeaseBillFeeRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.dao.repo.TenantRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import com.homi.service.service.finance.FinanceFlowService;
import com.homi.service.service.finance.PaymentFlowService;
import com.homi.service.service.lease.bill.component.LeaseBillCalculator;
import com.homi.service.service.lease.bill.component.LeaseBillPayerResolver;
import com.homi.service.service.lease.bill.component.LeaseBillUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 收款审批回调服务。
 *
 * <p>职责：处理审批流产生的两种结果——
 * <ul>
 *   <li>{@link #completePaymentFlowCollection}：审批通过，执行账单入账。</li>
 *   <li>{@link #closePaymentFlowCollection}：审批驳回/撤回，关闭支付流水。</li>
 * </ul>
 *
 * <p>依赖关系：
 * <ul>
 *   <li>纯计算逻辑（校验、状态推导）委托给 {@link LeaseBillCalculator}。</li>
 *   <li>账单金额重算（需要写库）委托给 {@link LeaseBillUpdater#recalculate(LeaseBill, Long, DateTime)}。</li>
 *   <li>本类不被 {@link LeaseBillService} 反向依赖，依赖关系为单向，无需 {@code @Lazy}。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PaymentApprovalService {
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;
    private final TenantRepo tenantRepo;
    private final UserRepo userRepo;
    private final FinanceFlowService financeFlowService;
    private final PaymentFlowService paymentFlowService;
    private final LeaseBillPayerResolver leaseBillPayerResolver;
    /**
     * 纯计算组件，与 LeaseBillService 共用，不产生循环依赖。
     */
    private final LeaseBillCalculator billCalculator;
    /**
     * 单向依赖：仅用于回写账单汇总金额，不会被 LeaseBillService 反向注入。
     */
    private final LeaseBillUpdater leaseBillUpdater;

    // -------------------------------------------------------------------------
    // 审批回调
    // -------------------------------------------------------------------------

    /**
     * 审批通过后完成账单收款入账。
     *
     * <p>流程：
     * <ol>
     *   <li>从 {@code payment_flow.ext_json} 中还原收款明细 DTO。</li>
     *   <li>重新校验分摊合法性（防止审批期间数据变更）。</li>
     *   <li>生成财务流水，回写费用项已收金额与账单汇总状态。</li>
     *   <li>更新支付流水为成功状态。</li>
     * </ol>
     *
     * @param paymentFlowId 待入账的支付流水 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void completePaymentFlowCollection(Long paymentFlowId) {
        PaymentFlow paymentFlow = paymentFlowService.getById(paymentFlowId);
        if (isPendingApproval(paymentFlow)) {
            return;
        }

        // 还原审批前暂存的收款请求
        LeaseBillCollectDTO dto = JSONUtil.toBean(paymentFlow.getExtJson(), LeaseBillCollectDTO.class);
        if (dto == null || dto.getId() == null
            || dto.getItems() == null || dto.getItems().isEmpty()) {
            markFlowFailed(paymentFlow.getId(), paymentFlow.getUpdateBy());
            return;
        }

        LeaseBill bill = leaseBillRepo.getByIdForUpdate(dto.getId());
        if (bill == null) {
            markFlowFailed(paymentFlow.getId(), dto.getUpdateBy());
            return;
        }

        // 加锁取出涉及的费用项
        Map<Long, LeaseBillFee> feeMap = loadFeeMapForUpdate(dto);

        // 重新校验：审批期间账单数据可能已被修改
        if (billCalculator.validateCollectItems(dto, bill, feeMap)) {
            markFlowFailed(paymentFlow.getId(), dto.getUpdateBy());
            return;
        }

        DateTime now = DateUtil.date();
        Tenant tenant = tenantRepo.getById(bill.getTenantId());
        LeaseBillPayerResolver.BillPayerInfo payerInfo = leaseBillPayerResolver.resolve(tenant);
        String operatorName = userRepo.getUserNicknameById(dto.getUpdateBy());
        Date payTime = ObjectUtil.defaultIfNull(dto.getPayTime(), paymentFlow.getPayTime());

        // 生成财务流水
        financeFlowService.createLeaseBillReceiveFlows(
            FinanceFlowService.CreateCommand.builder()
                .paymentFlow(paymentFlow)
                .feeMap(feeMap)
                .items(dto.getItems())
                .payTime(payTime)
                .operatorId(dto.getUpdateBy())
                .operatorName(operatorName)
                .payerName(payerInfo.payerName())
                .payerPhone(payerInfo.payerPhone())
                .remark(billCalculator.buildBillSummary(bill))
                .now(now)
                .build());

        // 回写费用项收款金额
        leaseBillUpdater.applyCollectToFees(feeMap, dto.getItems(), dto.getUpdateBy(), now);

        // 重算账单汇总（含写库，委托给 LeaseBillService）
        leaseBillUpdater.recalculate(bill, dto.getUpdateBy(), now);

        // 支付流水标记为成功
        paymentFlowService.updateApprovalAndStatus(
            paymentFlow.getId(),
            BizApprovalStatusEnum.APPROVED.getCode(),
            PaymentFlowStatusEnum.SUCCESS.getCode(),
            dto.getUpdateBy(),
            now);
    }

    /**
     * 审批驳回或撤回后关闭支付流水，不执行账单入账。
     *
     * @param paymentFlowId  支付流水 ID
     * @param approvalStatus 最终审批状态（驳回/撤回对应的 code）
     */
    @Transactional(rollbackFor = Exception.class)
    public void closePaymentFlowCollection(Long paymentFlowId, Integer approvalStatus) {
        PaymentFlow paymentFlow = paymentFlowService.getById(paymentFlowId);
        if (isPendingApproval(paymentFlow)) {
            return;
        }
        paymentFlowService.updateApprovalAndStatus(
            paymentFlow.getId(),
            approvalStatus,
            PaymentFlowStatusEnum.CLOSED.getCode(),
            paymentFlow.getUpdateBy(),
            DateUtil.date());
    }

    // -------------------------------------------------------------------------
    // 私有：入账辅助
    // -------------------------------------------------------------------------

    /**
     * 根据 DTO 中的费用项 ID 加行锁查询，并转为 Map。
     */
    private Map<Long, LeaseBillFee> loadFeeMapForUpdate(LeaseBillCollectDTO dto) {
        List<Long> feeIds = dto.getItems().stream()
            .map(LeaseBillCollectDTO.Item::getLeaseBillFeeId)
            .filter(Objects::nonNull)
            .toList();
        return leaseBillFeeRepo.getByIdsForUpdate(feeIds).stream()
            .collect(Collectors.toMap(LeaseBillFee::getId, f -> f));
    }

    /**
     * 判断支付流水是否处于待审批状态（避免重复入账）。
     */
    private boolean isPendingApproval(PaymentFlow paymentFlow) {
        return paymentFlow == null || !Objects.equals(paymentFlow.getStatus(), PaymentFlowStatusEnum.PENDING_APPROVAL.getCode());
    }

    /**
     * 将支付流水标记为失败状态。
     */
    private void markFlowFailed(Long paymentFlowId, Long operatorId) {
        paymentFlowService.updateStatus(
            paymentFlowId,
            PaymentFlowStatusEnum.FAILED.getCode(),
            operatorId,
            DateUtil.date());
    }
}
