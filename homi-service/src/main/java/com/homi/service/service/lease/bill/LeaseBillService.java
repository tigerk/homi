package com.homi.service.service.lease.bill;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowStatusEnum;
import com.homi.common.lib.enums.lease.LeaseBillStatusEnum;
import com.homi.common.lib.enums.pay.PayStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.approval.dto.ApprovalSubmitDTO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import com.homi.model.tenant.dto.LeaseBillFeeDTO;
import com.homi.model.tenant.dto.LeaseBillVoidDTO;
import com.homi.model.tenant.dto.LeaseBillUpdateDTO;
import com.homi.model.tenant.vo.bill.FinanceFlowVO;
import com.homi.model.tenant.vo.bill.LeaseBillFeeVO;
import com.homi.model.tenant.vo.bill.LeaseBillListVO;
import com.homi.model.tenant.vo.bill.PaymentFlowVO;
import com.homi.service.service.approval.ApprovalTemplate;
import com.homi.service.service.finance.FinanceFlowService;
import com.homi.service.service.finance.PaymentFlowService;
import com.homi.service.service.lease.bill.component.LeaseBillCalculator;
import com.homi.service.service.lease.bill.component.LeaseBillPayerResolver;
import com.homi.service.service.lease.bill.component.LeaseBillUpdater;
import com.homi.service.service.room.RoomService;
import com.homi.service.service.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租客账单核心服务。
 *
 * <p>职责：
 * <ul>
 *   <li>账单及费用明细的查询与组装</li>
 *   <li>账单编辑（金额重算、费用增删改）</li>
 *   <li>发起收款（创建 PaymentFlow 并提交审批）</li>
 * </ul>
 *
 * <p>纯计算逻辑（支付状态推导、分摊校验等）由 {@link LeaseBillCalculator} 承担，
 * 使本类与 {@link PaymentApprovalService} 之间保持单向依赖，无需 {@code @Lazy}。
 */
@Service
@RequiredArgsConstructor
public class LeaseBillService {

    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;
    private final LeaseRepo leaseRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final TenantService tenantService;
    private final UserRepo userRepo;
    private final FinanceFlowService financeFlowService;
    private final PaymentFlowService paymentFlowService;
    private final ApprovalTemplate approvalTemplate;
    private final RoomService roomService;
    /**
     * 单向依赖：本类调用审批回调，PaymentApprovalService 不反向注入本类。
     */
    private final PaymentApprovalService paymentApprovalService;
    private final LeaseBillPayerResolver leaseBillPayerResolver;
    /**
     * 纯计算组件，两个 Service 共同依赖，不产生循环。
     */
    private final LeaseBillCalculator billCalculator;
    private final LeaseBillUpdater leaseBillUpdater;

    // -------------------------------------------------------------------------
    // 查询
    // -------------------------------------------------------------------------

    /**
     * 根据租约查询账单列表，并挂载账单费用明细。
     */
    public List<LeaseBillListVO> getBillListByLeaseId(Long leaseId, Boolean historical) {
        List<LeaseBill> bills = leaseBillRepo.getBillListByLeaseId(leaseId, historical);
        if (bills.isEmpty()) {
            return List.of();
        }
        List<Long> billIds = bills.stream().map(LeaseBill::getId).toList();
        Map<Long, List<LeaseBillFeeVO>> feeMap = buildBillFeeVoMap(leaseBillFeeRepo.getFeesByBillIds(billIds));
        return bills.stream().map(bill -> toBillVo(bill, feeMap)).toList();
    }

    /**
     * 查询单个账单详情，组装账单上下文、财务流水与支付流水。
     */
    public LeaseBillListVO getBillDetailById(Long billId) {
        if (billId == null) {
            return null;
        }
        LeaseBill bill = leaseBillRepo.getById(billId);
        if (bill == null) {
            return null;
        }
        LeaseBillListVO vo = BeanCopyUtils.copyBean(bill, LeaseBillListVO.class);
        if (vo == null) {
            return null;
        }
        vo.setFeeList(toLeaseBillFeeVos(leaseBillFeeRepo.getFeesByBillId(billId)));
        attachBillContext(vo, bill);
        attachFinanceFlow(vo);
        return vo;
    }

    // -------------------------------------------------------------------------
    // 编辑
    // -------------------------------------------------------------------------

    /**
     * 编辑账单及费用项。
     *
     * <p>规则：
     * <ul>
     *   <li>已支付账单不允许编辑。</li>
     *   <li>已收款或已有财务流水的费用项不允许删除。</li>
     * </ul>
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBill(LeaseBillUpdateDTO dto, Long operatorId) {
        LeaseBill bill = getEditableBill(dto);
        if (bill == null) {
            return false;
        }

        DateTime now = DateUtil.date();
        BeanUtil.copyProperties(dto, bill, CopyOptions.create().setIgnoreNullValue(true));
        if (dto.getHistorical() != null) {
            bill.setHistorical(dto.getHistorical());
        }

        // 未传费用列表时，仅更新账单基础信息
        if (dto.getFeeList() == null) {
            updateBillBaseInfo(bill, operatorId, now);
            return true;
        }

        LeaseBillUpdater.FeeSyncCommand syncCommand = buildFeeSyncCommand(bill, dto.getFeeList(), operatorId, now);
        if (syncCommand == null) {
            return false;
        }

        // 应用费用项变更, 包括删除、更新、创建费用项。
        leaseBillUpdater.applyFeeChanges(syncCommand);
        // 重新计算账单金额
        leaseBillUpdater.recalculate(bill, operatorId, now);

        return true;
    }

    // -------------------------------------------------------------------------
    // 收款
    // -------------------------------------------------------------------------

    /**
     * 发起账单收款。
     *
     * <p>流程：创建待审批状态的 PaymentFlow → 根据审批配置决定立即入账或进入审批流。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean collectBill(LeaseBillCollectDTO dto) {
        if (isInvalidCollectRequest(dto)) {
            return false;
        }
        LeaseBill bill = leaseBillRepo.getByIdForUpdate(dto.getId());
        Map<Long, LeaseBillFee> feeMap = getCollectFeeMap(dto);
        if (bill == null
            || !Objects.equals(bill.getStatus(), LeaseBillStatusEnum.NORMAL.getCode())
            || feeMap.isEmpty()
            || billCalculator.validateCollectItems(dto, bill, feeMap)) {
            return false;
        }

        DateTime now = DateUtil.date();
        Tenant tenant = tenantService.getTenant(bill.getTenantId());
        LeaseBillPayerResolver.BillPayerInfo payerInfo = leaseBillPayerResolver.resolve(tenant);
        String operatorName = userRepo.getUserNicknameById(dto.getUpdateBy());
        Date payTime = ObjectUtil.defaultIfNull(dto.getPayTime(), now);
        String billSummary = billCalculator.buildBillSummary(bill);

        PaymentFlow paymentFlow = createPendingApprovalPaymentFlow(
            dto, bill, payerInfo, operatorName, payTime, billSummary, now);
        submitPaymentApproval(dto, bill, payerInfo, paymentFlow, billSummary, now);
        return true;
    }

    /**
     * 作废账单。
     *
     * <p>仅允许未支付且处于正常状态的账单作废。作废后账单不再允许编辑、收款或参与正常账单列表。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean voidBill(LeaseBillVoidDTO dto) {
        if (dto == null || dto.getBillId() == null || CharSequenceUtil.isBlank(dto.getVoidReason())) {
            throw new BizException("作废原因不能为空");
        }
        LeaseBill bill = leaseBillRepo.getByIdForUpdate(dto.getBillId());
        if (bill == null) {
            return false;
        }
        if (!Objects.equals(bill.getStatus(), LeaseBillStatusEnum.NORMAL.getCode())) {
            throw new BizException("账单已作废");
        }
        if (!Objects.equals(bill.getPayStatus(), PayStatusEnum.UNPAID.getCode())) {
            throw new BizException("仅未支付账单允许作废");
        }

        DateTime now = DateUtil.date();
        bill.setStatus(LeaseBillStatusEnum.VOIDED.getCode());
        bill.setVoidReason(dto.getVoidReason().trim());
        bill.setVoidTime(now);
        bill.setVoidBy(dto.getUpdateBy());
        bill.setUpdateBy(dto.getUpdateBy());
        bill.setUpdateTime(now);
        leaseBillRepo.updateById(bill);
        return true;
    }

    // -------------------------------------------------------------------------
    // 私有：账单上下文组装
    // -------------------------------------------------------------------------

    /**
     * 组装账单详情中的财务流水与支付流水。
     */
    private void attachFinanceFlow(LeaseBillListVO vo) {
        if (vo == null) {
            return;
        }
        if (vo.getFeeList() == null || vo.getFeeList().isEmpty()) {
            vo.setFinanceFlowList(List.of());
            vo.setPaymentFlowList(List.of());
            return;
        }
        List<Long> feeIds = vo.getFeeList().stream()
            .map(LeaseBillFeeVO::getId)
            .filter(Objects::nonNull)
            .toList();
        vo.setFinanceFlowList(toFinanceFlowVos(financeFlowService.getListByBizIds(FinanceBizTypeEnum.LEASE_BILL_FEE.getCode(), feeIds)));
        vo.setPaymentFlowList(toPaymentFlowVos(paymentFlowService.listByBiz(PaymentFlowBizTypeEnum.LEASE_BILL.getCode(), vo.getId())));
    }

    /**
     * 组装账单详情中的房源、付款人和证件信息。
     */
    private void attachBillContext(LeaseBillListVO vo, LeaseBill bill) {
        if (vo == null || bill == null) {
            return;
        }
        vo.setRoomAddress(resolveRoomAddress(bill.getLeaseId()));

        Tenant tenant = tenantService.getTenant(bill.getTenantId());
        if (tenant == null) {
            return;
        }
        LeaseBillPayerResolver.BillPayerInfo payerInfo = leaseBillPayerResolver.resolve(tenant);
        vo.setPayerName(payerInfo.payerName());
        vo.setPayerPhone(payerInfo.payerPhone());
        vo.setPayerIdType(payerInfo.payerIdType());
        vo.setPayerIdTypeName(payerInfo.payerIdTypeName());
        vo.setPayerIdNo(payerInfo.payerIdNo());
    }

    // -------------------------------------------------------------------------
    // 私有：账单编辑辅助
    // -------------------------------------------------------------------------

    private LeaseBill getEditableBill(LeaseBillUpdateDTO dto) {
        if (dto == null || dto.getId() == null) {
            return null;
        }
        LeaseBill bill = leaseBillRepo.getByIdForUpdate(dto.getId());
        if (bill == null
            || Objects.equals(bill.getStatus(), LeaseBillStatusEnum.VOIDED.getCode())
            || Objects.equals(bill.getPayStatus(), PayStatusEnum.PAID.getCode())) {
            return null;
        }
        return bill;
    }

    private void updateBillBaseInfo(LeaseBill bill, Long operatorId, DateTime now) {
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        leaseBillRepo.updateById(bill);
    }

    /**
     * 构建费用同步命令：区分新增、更新、删除三类操作。
     *
     * @return 同步命令，若存在不可修改的费用项则返回 {@code null}
     */
    private LeaseBillUpdater.FeeSyncCommand buildFeeSyncCommand(LeaseBill bill, List<LeaseBillFeeDTO> feeList, Long operatorId, DateTime now) {
        List<LeaseBillFee> existFees = leaseBillFeeRepo.getFeesByBillIdForUpdate(bill.getId());
        Map<Long, LeaseBillFee> existFeeMap = existFees.stream()
            .filter(f -> f.getId() != null)
            .collect(Collectors.toMap(LeaseBillFee::getId, f -> f));
        Map<Long, LeaseBillFeeDTO> incomingFeeMap = feeList.stream()
            .filter(f -> f.getId() != null)
            .collect(Collectors.toMap(LeaseBillFeeDTO::getId, f -> f, (l, r) -> r));

        // 计算需要删除的费用项（存在于旧列表但不在新列表中）
        List<Long> removedIds = existFees.stream()
            .map(LeaseBillFee::getId)
            .filter(Objects::nonNull)
            .filter(id -> !incomingFeeMap.containsKey(id))
            .toList();
        validateRemovableFees(removedIds, existFeeMap);

        List<LeaseBillFee> toCreate = new ArrayList<>();
        List<LeaseBillFee> toUpdate = new ArrayList<>();
        for (LeaseBillFeeDTO fee : feeList) {
            LeaseBillFee entity = buildFeeEntity(bill.getId(), fee, existFeeMap.get(fee.getId()), operatorId, now);
            if (entity == null) {
                return null;
            }
            if (entity.getId() == null) {
                toCreate.add(entity);
            } else {
                toUpdate.add(entity);
            }
        }
        return new LeaseBillUpdater.FeeSyncCommand(removedIds, toCreate, toUpdate);
    }

    /**
     * 校验即将删除的费用项是否允许删除（已收款或已有财务流水则拒绝）。
     */
    private void validateRemovableFees(List<Long> removedIds, Map<Long, LeaseBillFee> existFeeMap) {
        if (removedIds.isEmpty()) {
            return;
        }
        boolean hasPaidFee = removedIds.stream()
            .map(existFeeMap::get)
            .filter(Objects::nonNull)
            .anyMatch(f -> ObjectUtil.defaultIfNull(f.getPaidAmount(), BigDecimal.ZERO)
                .compareTo(BigDecimal.ZERO) > 0);
        if (hasPaidFee) {
            throw new BizException("已收款的费用项不允许删除");
        }
        if (financeFlowService.existsByBizIds(FinanceBizTypeEnum.LEASE_BILL_FEE.getCode(), removedIds)) {
            throw new BizException("已有财务流水的费用项不允许删除");
        }
    }

    private LeaseBillFee buildFeeEntity(Long billId,
                                        LeaseBillFeeDTO fee,
                                        LeaseBillFee existing,
                                        Long operatorId,
                                        DateTime now) {
        // 新记录使用空实体，更新记录复用已有实体
        LeaseBillFee entity = (fee.getId() == null) ? new LeaseBillFee() : existing;
        if (entity == null) {
            return null;
        }
        BigDecimal amount = ObjectUtil.defaultIfNull(fee.getAmount(), BigDecimal.ZERO);
        BigDecimal currentPaidAmount = (entity.getId() == null)
            ? BigDecimal.ZERO
            : ObjectUtil.defaultIfNull(entity.getPaidAmount(), BigDecimal.ZERO);
        // 修改后的金额不能低于已收款金额
        if (currentPaidAmount.compareTo(amount) > 0) {
            return null;
        }
        entity.setBillId(billId);
        entity.setFeeType(fee.getFeeType());
        entity.setDictDataId(fee.getDictDataId());
        entity.setFeeName(fee.getFeeName());
        entity.setAmount(amount);
        entity.setPaidAmount(currentPaidAmount);
        entity.setUnpaidAmount(amount.subtract(currentPaidAmount));
        entity.setPayStatus(billCalculator.resolvePayStatus(currentPaidAmount, amount));
        entity.setFeeStart(fee.getFeeStart());
        entity.setFeeEnd(fee.getFeeEnd());
        entity.setRemark(fee.getRemark());
        entity.setUpdateBy(operatorId);
        entity.setUpdateTime(now);
        if (entity.getId() == null) {
            entity.setCreateBy(operatorId);
            entity.setCreateTime(now);
        }
        return entity;
    }

    // -------------------------------------------------------------------------
    // 私有：收款辅助
    // -------------------------------------------------------------------------

    private boolean isInvalidCollectRequest(LeaseBillCollectDTO dto) {
        return dto == null || dto.getId() == null
            || dto.getItems() == null || dto.getItems().isEmpty();
    }

    private Map<Long, LeaseBillFee> getCollectFeeMap(LeaseBillCollectDTO dto) {
        List<Long> feeIds = dto.getItems().stream()
            .map(LeaseBillCollectDTO.Item::getLeaseBillFeeId)
            .filter(Objects::nonNull)
            .toList();
        if (feeIds.isEmpty()) {
            return Map.of();
        }
        return leaseBillFeeRepo.getByIdsForUpdate(feeIds).stream()
            .collect(Collectors.toMap(LeaseBillFee::getId, f -> f));
    }

    /**
     * 创建待审批状态的支付流水。
     */
    private PaymentFlow createPendingApprovalPaymentFlow(LeaseBillCollectDTO dto,
                                                         LeaseBill bill,
                                                         LeaseBillPayerResolver.BillPayerInfo payerInfo,
                                                         String operatorName,
                                                         Date payTime,
                                                         String billSummary,
                                                         DateTime now) {
        return paymentFlowService.createLeaseBillPaymentFlow(
            PaymentFlowService.CreateCommand.builder()
                .bill(bill)
                .totalAmount(dto.getTotalAmount())
                .payChannel(dto.getPayChannel())
                .thirdTradeNo(dto.getThirdTradeNo())
                .paymentVoucherUrl(dto.getPaymentVoucherUrl())
                .payTime(payTime)
                .operatorId(dto.getUpdateBy())
                .operatorName(operatorName)
                .payerName(payerInfo.payerName())
                .payerPhone(payerInfo.payerPhone())
                .remark(CharSequenceUtil.blankToDefault(dto.getPayRemark(), billSummary))
                .status(PaymentFlowStatusEnum.PENDING_APPROVAL.getCode())
                .approvalStatus(BizApprovalStatusEnum.PENDING.getCode())
                .extJson(JSONUtil.toJsonStr(dto))
                .now(now)
                .build());
    }

    /**
     * 提交收款审批，无审批配置时直接回调入账逻辑。
     */
    private void submitPaymentApproval(LeaseBillCollectDTO dto,
                                       LeaseBill bill,
                                       LeaseBillPayerResolver.BillPayerInfo payerInfo,
                                       PaymentFlow paymentFlow,
                                       String billSummary,
                                       DateTime now) {
        approvalTemplate.submitIfNeed(
            ApprovalSubmitDTO.builder()
                .companyId(bill.getCompanyId())
                .bizType(ApprovalBizTypeEnum.PAYMENT_FLOW.getCode())
                .bizId(paymentFlow.getId())
                .title(buildPaymentApprovalTitle(payerInfo.payerName(), dto.getTotalAmount()))
                .applicantId(dto.getUpdateBy())
                .remark(CharSequenceUtil.blankToDefault(dto.getPayRemark(), billSummary))
                .build(),
            bizId -> paymentFlowService.updateApprovalStatus(
                bizId, BizApprovalStatusEnum.PENDING.getCode(), dto.getUpdateBy(), now),
            bizId -> {
                paymentFlowService.updateApprovalStatus(
                    bizId, BizApprovalStatusEnum.APPROVED.getCode(), dto.getUpdateBy(), now);
                // 审批通过：触发入账回调
                paymentApprovalService.completePaymentFlowCollection(bizId);
            });
    }

    // -------------------------------------------------------------------------
    // 私有：VO 转换
    // -------------------------------------------------------------------------

    private List<LeaseBillFeeVO> toLeaseBillFeeVos(List<LeaseBillFee> fees) {
        return fees.stream()
            .map(f -> BeanCopyUtils.copyBean(f, LeaseBillFeeVO.class))
            .toList();
    }

    private Map<Long, List<LeaseBillFeeVO>> buildBillFeeVoMap(List<LeaseBillFee> fees) {
        return fees.stream().collect(Collectors.groupingBy(
            LeaseBillFee::getBillId,
            Collectors.mapping(f -> BeanCopyUtils.copyBean(f, LeaseBillFeeVO.class), Collectors.toList())));
    }

    private LeaseBillListVO toBillVo(LeaseBill bill, Map<Long, List<LeaseBillFeeVO>> feeMap) {
        LeaseBillListVO vo = BeanCopyUtils.copyBean(bill, LeaseBillListVO.class);
        assert vo != null;
        vo.setFeeList(feeMap.getOrDefault(bill.getId(), List.of()));
        return vo;
    }

    private List<FinanceFlowVO> toFinanceFlowVos(List<FinanceFlow> flows) {
        return flows.stream().map(flow -> {
            FinanceFlowVO vo = new FinanceFlowVO();
            BeanUtils.copyProperties(flow, vo);
            return vo;
        }).toList();
    }

    private List<PaymentFlowVO> toPaymentFlowVos(List<PaymentFlow> paymentFlows) {
        return paymentFlows.stream().map(item -> {
            PaymentFlowVO vo = new PaymentFlowVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList();
    }

    // -------------------------------------------------------------------------
    // 私有：其他辅助
    // -------------------------------------------------------------------------

    private String resolveRoomAddress(Long leaseId) {
        if (leaseId == null) {
            return null;
        }
        Lease lease = leaseRepo.getById(leaseId);
        if (lease == null) {
            return null;
        }
        List<Long> roomIds = leaseRoomRepo.getListByLeaseId(lease.getId()).stream()
            .map(LeaseRoom::getRoomId)
            .filter(Objects::nonNull)
            .toList();
        if (!roomIds.isEmpty()) {
            return roomService.getRoomAddressByIds(roomIds);
        }
        if (lease.getRoomIds() == null) {
            return null;
        }
        List<Long> leaseRoomIds = JSONUtil.toList(lease.getRoomIds(), Long.class);
        return leaseRoomIds.isEmpty() ? null : roomService.getRoomAddressByIds(leaseRoomIds);
    }

    private String buildPaymentApprovalTitle(String payerName, BigDecimal totalAmount) {
        return String.format("【账单收款审批】-付款人：%s 金额：%s",
            CharSequenceUtil.blankToDefault(payerName, "未知"),
            ObjectUtil.defaultIfNull(totalAmount, BigDecimal.ZERO).stripTrailingZeros().toPlainString());
    }
}
