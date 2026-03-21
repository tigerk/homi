package com.homi.service.service.tenant;

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
import com.homi.common.lib.enums.pay.PayStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.approval.dto.ApprovalSubmitDTO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import com.homi.model.tenant.dto.LeaseBillFeeDTO;
import com.homi.model.tenant.dto.LeaseBillUpdateDTO;
import com.homi.model.tenant.vo.bill.FinanceFlowVO;
import com.homi.model.tenant.vo.bill.LeaseBillFeeVO;
import com.homi.model.tenant.vo.bill.LeaseBillListVO;
import com.homi.model.tenant.vo.bill.PaymentFlowVO;
import com.homi.service.service.approval.ApprovalTemplate;
import com.homi.service.service.finance.FinanceFlowService;
import com.homi.service.service.finance.PaymentFlowService;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租客账单服务。
 * <p>
 * 负责账单查询、账单编辑、发起收款以及账单上下文信息组装。
 */
@Service
@RequiredArgsConstructor
public class LeaseBillService {
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;
    private final LeaseRepo leaseRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final TenantRepo tenantRepo;
    private final UserRepo userRepo;
    private final FinanceFlowService financeFlowService;
    private final PaymentFlowService paymentFlowService;
    private final ApprovalTemplate approvalTemplate;
    private final RoomService roomService;
    private final PaymentApprovalService paymentApprovalService;
    private final LeaseBillPayerResolver leaseBillPayerResolver;

    /**
     * 根据租约查询账单列表，并挂载账单费用明细。
     */
    public List<LeaseBillListVO> getBillListByLeaseId(Long leaseId, Boolean valid) {
        List<LeaseBill> bills = leaseBillRepo.getBillListByLeaseId(leaseId, valid);
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

    /**
     * 编辑账单及费用项。
     * <p>
     * 已支付账单不允许编辑；已收款或已有财务流水的费用项不允许删除。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBill(LeaseBillUpdateDTO dto, Long operatorId) {
        LeaseBill bill = getEditableBill(dto);
        if (bill == null) {
            return false;
        }

        DateTime now = DateUtil.date();
        BeanUtil.copyProperties(dto, bill, CopyOptions.create().setIgnoreNullValue(true));
        if (dto.getValid() != null) {
            bill.setValid(dto.getValid());
        }

        if (dto.getFeeList() == null) {
            updateBillBaseInfo(bill, operatorId, now);
            return true;
        }

        FeeSyncCommand syncCommand = buildFeeSyncCommand(bill, dto.getFeeList(), operatorId, now);
        if (syncCommand == null) {
            return false;
        }

        applyFeeChanges(syncCommand);
        recalculateBillAmounts(bill, operatorId, now);
        return true;
    }

    /**
     * 发起账单收款。
     * <p>
     * 先创建 payment_flow，再根据是否存在审批配置决定立即入账或进入审批。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean collectBill(LeaseBillCollectDTO dto) {
        if (isInvalidCollectRequest(dto)) {
            return false;
        }

        LeaseBill bill = leaseBillRepo.getByIdForUpdate(dto.getId());
        Map<Long, LeaseBillFee> feeMap = getCollectFeeMap(dto);
        if (bill == null || feeMap.isEmpty() || !validateCollectItems(dto, bill, feeMap)) {
            return false;
        }

        DateTime now = DateUtil.date();
        Tenant tenant = getTenant(bill.getTenantId());
        LeaseBillPayerResolver.BillPayerInfo payerInfo = leaseBillPayerResolver.resolve(tenant);
        String operatorName = userRepo.getUserNicknameById(dto.getUpdateBy());
        Date payTime = ObjectUtil.defaultIfNull(dto.getPayTime(), now);
        String billSummary = buildBillSummary(bill);

        PaymentFlow paymentFlow = createPendingApprovalPaymentFlow(dto, bill, payerInfo, operatorName, payTime, billSummary, now);
        submitPaymentApproval(dto, bill, payerInfo, paymentFlow, billSummary, now);
        return true;
    }

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
        Tenant tenant = getTenant(bill.getTenantId());
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

    /**
     * 校验即将删除的费用项是否允许删除。
     */
    private void validateRemovableFees(List<Long> removedIds, Map<Long, LeaseBillFee> existFeeMap) {
        if (removedIds.isEmpty()) {
            return;
        }

        boolean hasPaidFee = removedIds.stream()
            .map(existFeeMap::get)
            .filter(Objects::nonNull)
            .anyMatch(item -> ObjectUtil.defaultIfNull(item.getPaidAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0);
        if (hasPaidFee) {
            throw new BizException("已收款的费用项不允许删除");
        }
        if (financeFlowService.existsByBizIds(FinanceBizTypeEnum.LEASE_BILL_FEE.getCode(), removedIds)) {
            throw new BizException("已有财务流水的费用项不允许删除");
        }
    }

    /**
     * 校验本次收款分摊是否与账单及费用项匹配，且不能超收。
     */
    private boolean validateCollectItems(LeaseBillCollectDTO dto, LeaseBill bill, Map<Long, LeaseBillFee> feeMap) {
        BigDecimal totalAmount = ObjectUtil.defaultIfNull(dto.getTotalAmount(), BigDecimal.ZERO);
        BigDecimal allocatedAmount = BigDecimal.ZERO;
        Set<Long> duplicateGuard = new HashSet<>();

        for (LeaseBillCollectDTO.Item item : dto.getItems()) {
            if (item == null || item.getLeaseBillFeeId() == null || !duplicateGuard.add(item.getLeaseBillFeeId())) {
                return false;
            }

            LeaseBillFee fee = feeMap.get(item.getLeaseBillFeeId());
            BigDecimal amount = ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO);
            if (!isCollectableItem(bill, fee, amount)) {
                return false;
            }
            allocatedAmount = allocatedAmount.add(amount);
        }

        return allocatedAmount.compareTo(totalAmount) == 0;
    }

    /**
     * 基于账单下全部费用项重算账单汇总金额与支付状态。
     */
    private void recalculateBillAmounts(LeaseBill bill, Long operatorId, DateTime now) {
        List<LeaseBillFee> allFees = leaseBillFeeRepo.getFeesByBillIdForUpdate(bill.getId());
        BigDecimal totalAmount = sumFeeAmount(allFees);
        BigDecimal paidAmount = sumFeePaidAmount(allFees);

        bill.setTotalAmount(totalAmount);
        bill.setPaidAmount(paidAmount);
        bill.setUnpaidAmount(totalAmount.subtract(paidAmount));
        bill.setPayStatus(resolvePayStatus(paidAmount, totalAmount));
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        leaseBillRepo.updateById(bill);
    }

    private LeaseBill getEditableBill(LeaseBillUpdateDTO dto) {
        if (dto == null || dto.getId() == null) {
            return null;
        }

        LeaseBill bill = leaseBillRepo.getByIdForUpdate(dto.getId());
        if (bill == null || Objects.equals(bill.getPayStatus(), PayStatusEnum.PAID.getCode())) {
            return null;
        }
        return bill;
    }

    private void updateBillBaseInfo(LeaseBill bill, Long operatorId, DateTime now) {
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        leaseBillRepo.updateById(bill);
    }

    private FeeSyncCommand buildFeeSyncCommand(LeaseBill bill, List<LeaseBillFeeDTO> feeList, Long operatorId, DateTime now) {
        List<LeaseBillFee> existFees = leaseBillFeeRepo.getFeesByBillIdForUpdate(bill.getId());
        Map<Long, LeaseBillFee> existFeeMap = existFees.stream()
            .filter(item -> item.getId() != null)
            .collect(Collectors.toMap(LeaseBillFee::getId, item -> item));
        Map<Long, LeaseBillFeeDTO> incomingFeeMap = feeList.stream()
            .filter(item -> item.getId() != null)
            .collect(Collectors.toMap(LeaseBillFeeDTO::getId, item -> item, (left, right) -> right));

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
        return new FeeSyncCommand(removedIds, toCreate, toUpdate);
    }

    private LeaseBillFee buildFeeEntity(Long billId, LeaseBillFeeDTO fee, LeaseBillFee existing, Long operatorId, DateTime now) {
        LeaseBillFee entity = fee.getId() == null ? new LeaseBillFee() : existing;
        if (entity == null) {
            return null;
        }

        BigDecimal amount = ObjectUtil.defaultIfNull(fee.getAmount(), BigDecimal.ZERO);
        BigDecimal currentPaidAmount = entity.getId() == null ? BigDecimal.ZERO : ObjectUtil.defaultIfNull(entity.getPaidAmount(), BigDecimal.ZERO);
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
        entity.setPayStatus(resolvePayStatus(currentPaidAmount, amount));
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

    private void applyFeeChanges(FeeSyncCommand command) {
        if (!command.removedIds().isEmpty()) {
            leaseBillFeeRepo.removeByIds(command.removedIds());
        }
        if (!command.toUpdate().isEmpty()) {
            leaseBillFeeRepo.updateBatchById(command.toUpdate());
        }
        if (!command.toCreate().isEmpty()) {
            leaseBillFeeRepo.saveBatch(command.toCreate());
        }
    }

    private boolean isInvalidCollectRequest(LeaseBillCollectDTO dto) {
        return dto == null || dto.getId() == null || dto.getItems() == null || dto.getItems().isEmpty();
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
            .collect(Collectors.toMap(LeaseBillFee::getId, item -> item));
    }

    private PaymentFlow createPendingApprovalPaymentFlow(
        LeaseBillCollectDTO dto,
        LeaseBill bill,
        LeaseBillPayerResolver.BillPayerInfo payerInfo,
        String operatorName,
        java.util.Date payTime,
        String billSummary,
        DateTime now
    ) {
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
                .build()
        );
    }

    private void submitPaymentApproval(
        LeaseBillCollectDTO dto,
        LeaseBill bill,
        LeaseBillPayerResolver.BillPayerInfo payerInfo,
        PaymentFlow paymentFlow,
        String billSummary,
        DateTime now
    ) {
        approvalTemplate.submitIfNeed(
            ApprovalSubmitDTO.builder()
                .companyId(bill.getCompanyId())
                .bizType(ApprovalBizTypeEnum.PAYMENT_FLOW.getCode())
                .bizId(paymentFlow.getId())
                .title(buildPaymentApprovalTitle(payerInfo.payerName(), dto.getTotalAmount()))
                .applicantId(dto.getUpdateBy())
                .remark(CharSequenceUtil.blankToDefault(dto.getPayRemark(), billSummary))
                .build(),
            bizId -> paymentFlowService.updateApprovalStatus(bizId, BizApprovalStatusEnum.PENDING.getCode(), dto.getUpdateBy(), now),
            bizId -> {
                paymentFlowService.updateApprovalStatus(bizId, BizApprovalStatusEnum.APPROVED.getCode(), dto.getUpdateBy(), now);
                paymentApprovalService.completePaymentFlowCollection(bizId);
            }
        );
    }

    private List<LeaseBillFeeVO> toLeaseBillFeeVos(List<LeaseBillFee> fees) {
        return fees.stream()
            .map(item -> BeanCopyUtils.copyBean(item, LeaseBillFeeVO.class))
            .toList();
    }

    private Map<Long, List<LeaseBillFeeVO>> buildBillFeeVoMap(List<LeaseBillFee> fees) {
        return fees.stream().collect(Collectors.groupingBy(
            LeaseBillFee::getBillId,
            Collectors.mapping(item -> BeanCopyUtils.copyBean(item, LeaseBillFeeVO.class), Collectors.toList())
        ));
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
        if (leaseRoomIds.isEmpty()) {
            return null;
        }
        return roomService.getRoomAddressByIds(leaseRoomIds);
    }

    private Tenant getTenant(Long tenantId) {
        return tenantId == null ? null : tenantRepo.getById(tenantId);
    }

    private BigDecimal sumFeeAmount(List<LeaseBillFee> fees) {
        return fees.stream()
            .map(item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumFeePaidAmount(List<LeaseBillFee> fees) {
        return fees.stream()
            .map(item -> ObjectUtil.defaultIfNull(item.getPaidAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isCollectableItem(LeaseBill bill, LeaseBillFee fee, BigDecimal amount) {
        return fee != null
            && Objects.equals(fee.getBillId(), bill.getId())
            && amount.compareTo(BigDecimal.ZERO) > 0
            && amount.compareTo(ObjectUtil.defaultIfNull(fee.getUnpaidAmount(), BigDecimal.ZERO)) <= 0;
    }

    private Integer resolvePayStatus(BigDecimal paidAmount, BigDecimal totalAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return PayStatusEnum.UNPAID.getCode();
        }
        if (totalAmount != null && paidAmount.compareTo(totalAmount) >= 0) {
            return PayStatusEnum.PAID.getCode();
        }
        return PayStatusEnum.PARTIALLY_PAID.getCode();
    }

    private String buildBillSummary(LeaseBill bill) {
        if (bill == null) {
            return "租客账单收款";
        }
        return "租客账单#" + bill.getId();
    }

    private String buildPaymentApprovalTitle(String payerName, BigDecimal totalAmount) {
        return String.format("【账单收款审批】-付款人：%s 金额：%s",
            CharSequenceUtil.blankToDefault(payerName, "未知"),
            ObjectUtil.defaultIfNull(totalAmount, BigDecimal.ZERO).stripTrailingZeros().toPlainString());
    }

    private record FeeSyncCommand(List<Long> removedIds, List<LeaseBillFee> toCreate, List<LeaseBillFee> toUpdate) {
    }
}
