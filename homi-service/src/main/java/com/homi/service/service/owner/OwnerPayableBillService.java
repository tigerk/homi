package com.homi.service.service.owner;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.biz.BizOperateBizTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateSourceTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateTypeEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowStatusEnum;
import com.homi.common.lib.enums.owner.OwnerBillSceneEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillPaymentStatusEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillStatusEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.approval.dto.ApprovalSubmitDTO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.owner.dto.*;
import com.homi.model.owner.vo.*;
import com.homi.service.service.approval.ApprovalTemplate;
import com.homi.service.service.finance.FinanceFlowService;
import com.homi.service.service.finance.PaymentFlowService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerPayableBillService {
    private final OwnerPayableBillRepo ownerPayableBillRepo;
    private final OwnerPayableBillFeeRepo ownerPayableBillFeeRepo;
    private final PaymentFlowRepo paymentFlowRepo;
    private final OwnerRepo ownerRepo;
    private final OwnerContractRepo ownerContractRepo;
    private final OwnerContractSubjectRepo ownerContractSubjectRepo;
    private final FileAttachRepo fileAttachRepo;
    private final BizOperateLogRepo bizOperateLogRepo;
    private final ApprovalTemplate approvalTemplate;
    private final OwnerPayableBillPaymentApprovalService ownerPayableBillPaymentApprovalService;
    private final FinanceFlowService financeFlowService;
    private final PaymentFlowService paymentFlowService;

    public PageVO<OwnerPayableBillListVO> page(OwnerPayableBillQueryDTO query) {
        Page<OwnerPayableBill> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        List<Long> ownerIds = ownerRepo.getOwnerIdsByOwnerName(query.getOwnerName());
        if (ownerIds != null && ownerIds.isEmpty()) {
            return emptyPage(query);
        }
        LambdaQueryWrapper<OwnerPayableBill> wrapper = buildWrapper(query, ownerIds);
        wrapper
            .orderByDesc(OwnerPayableBill::getPaymentStatus)
            .orderByAsc(OwnerPayableBill::getId);
        Page<OwnerPayableBill> result = ownerPayableBillRepo.page(page, wrapper);

        List<OwnerPayableBillListVO> list = Lists.newArrayList();

        if (CollectionUtils.isNotEmpty(result.getRecords())) {
            // 获取业主 ID 列表
            List<Long> OwnerIds = result.getRecords().stream().map(OwnerPayableBill::getOwnerId).filter(Objects::nonNull).distinct().toList();
            Map<Long, Owner> ownerMap = ownerRepo.listByIds(OwnerIds).stream().collect(Collectors.toMap(Owner::getId, Function.identity()));

            // 获取合同 ID 列表
            List<Long> contractIds = result.getRecords().stream().map(OwnerPayableBill::getContractId).filter(Objects::nonNull).distinct().toList();
            Map<Long, OwnerContract> contractMap = ownerContractRepo.listByIds(contractIds).stream().collect(Collectors.toMap(OwnerContract::getId, Function.identity()));

            list = result.getRecords().stream().map(item -> toListVO(item, ownerMap.get(item.getOwnerId()), contractMap.get(item.getContractId()))).toList();
        }

        return PageVO.<OwnerPayableBillListVO>builder()
            .currentPage(result.getCurrent())
            .pageSize(result.getSize())
            .total(result.getTotal())
            .pages(result.getPages())
            .list(list)
            .build();
    }

    public OwnerPayableBillSummaryVO summary(OwnerPayableBillQueryDTO query) {
        OwnerPayableBillSummaryVO vo = new OwnerPayableBillSummaryVO();
        List<Long> ownerIds = ownerRepo.getOwnerIdsByOwnerName(query.getOwnerName());
        if (ownerIds != null && ownerIds.isEmpty()) {
            fillEmptySummary(vo);
            return vo;
        }
        List<OwnerPayableBill> list = ownerPayableBillRepo.list(buildWrapper(query, ownerIds));
        vo.setBillCount((long) list.size());
        vo.setTotalPayableAmount(sum(list, OwnerPayableBill::getPayableAmount));
        vo.setTotalPaidAmount(sum(list, OwnerPayableBill::getPaidAmount));
        vo.setTotalUnpaidAmount(sum(list, OwnerPayableBill::getUnpaidAmount));
        vo.setVoidedCount(list.stream().filter(item -> Objects.equals(item.getBillStatus(), OwnerPayableBillStatusEnum.VOIDED.getCode())).count());
        return vo;
    }

    public OwnerPayableBillDetailVO detail(OwnerPayableBillIdDTO dto) {
        if (dto == null || dto.getBillId() == null) {
            throw new IllegalArgumentException("应付单ID不能为空");
        }
        OwnerPayableBill bill = ownerPayableBillRepo.getById(dto.getBillId());
        if (bill == null) {
            throw new IllegalArgumentException("包租业主应付单不存在");
        }
        return toDetailVOList(Collections.singletonList(bill), true).get(0);
    }

    public List<OwnerPayableBillDetailVO> detailListByContract(OwnerPayableBillQueryDTO query) {
        if (query == null || query.getContractId() == null) {
            throw new IllegalArgumentException("业主合同ID不能为空");
        }
        List<Long> ownerIds = ownerRepo.getOwnerIdsByOwnerName(query.getOwnerName());
        if (ownerIds != null && ownerIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<OwnerPayableBill> wrapper = buildWrapper(query, ownerIds);
        wrapper
            .orderByDesc(OwnerPayableBill::getPaymentStatus)
            .orderByAsc(OwnerPayableBill::getId);
        return toDetailVOList(ownerPayableBillRepo.list(wrapper), false);
    }

    private List<OwnerPayableBillDetailVO> toDetailVOList(List<OwnerPayableBill> billList, boolean includeOperateLog) {
        if (CollectionUtils.isEmpty(billList)) {
            return Collections.emptyList();
        }

        List<Long> billIds = billList.stream().map(OwnerPayableBill::getId).filter(Objects::nonNull).toList();
        List<Long> ownerIds = billList.stream().map(OwnerPayableBill::getOwnerId).filter(Objects::nonNull).distinct().toList();
        List<Long> contractIds = billList.stream().map(OwnerPayableBill::getContractId).filter(Objects::nonNull).distinct().toList();

        Map<Long, Owner> ownerMap = ownerIds.isEmpty()
            ? Collections.emptyMap()
            : ownerRepo.listByIds(ownerIds).stream().collect(Collectors.toMap(Owner::getId, Function.identity()));
        Map<Long, OwnerContract> contractMap = contractIds.isEmpty()
            ? Collections.emptyMap()
            : ownerContractRepo.listByIds(contractIds).stream().collect(Collectors.toMap(OwnerContract::getId, Function.identity()));
        Map<Long, List<OwnerPayableBillFeeVO>> feeMap = buildFeeListMap(billIds);
        Map<Long, List<OwnerPayableBillPaymentVO>> paymentMap = buildPaymentListMap(billIds);

        return billList.stream()
            .map(bill -> toDetailVO(
                bill,
                ownerMap.get(bill.getOwnerId()),
                contractMap.get(bill.getContractId()),
                feeMap.getOrDefault(bill.getId(), Collections.emptyList()),
                paymentMap.getOrDefault(bill.getId(), Collections.emptyList()),
                includeOperateLog
            ))
            .toList();
    }

    private OwnerPayableBillDetailVO toDetailVO(
        OwnerPayableBill bill,
        Owner owner,
        OwnerContract contract,
        List<OwnerPayableBillFeeVO> feeList,
        List<OwnerPayableBillPaymentVO> paymentList,
        boolean includeOperateLog
    ) {
        OwnerPayableBillDetailVO vo = new OwnerPayableBillDetailVO();
        vo.setBillId(bill.getId());
        vo.setBillNo(bill.getBillNo());
        vo.setBillScene(bill.getBillScene());
        vo.setOwnerId(bill.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getOwnerName() : null);
        vo.setOwnerPhone(owner != null ? owner.getOwnerPhone() : null);
        vo.setContractId(bill.getContractId());
        vo.setContractNo(contract != null ? contract.getContractNo() : null);
        vo.setSubjectName(bill.getSubjectNameSnapshot());
        vo.setBillStartDate(bill.getBillStartDate());
        vo.setBillEndDate(bill.getBillEndDate());
        vo.setDueDate(bill.getDueDate());
        vo.setPayableAmount(bill.getPayableAmount());
        vo.setPaidAmount(bill.getPaidAmount());
        vo.setUnpaidAmount(bill.getUnpaidAmount());
        vo.setAdjustAmount(bill.getAdjustAmount());
        vo.setPaymentStatus(bill.getPaymentStatus());
        vo.setBillStatus(bill.getBillStatus());
        vo.setVoidReason(bill.getVoidReason());
        vo.setVoidBy(bill.getVoidBy());
        vo.setVoidByName(bill.getVoidByName());
        vo.setVoidAt(bill.getVoidAt());
        vo.setGeneratedAt(bill.getGeneratedAt());
        vo.setRemark(bill.getRemark());
        vo.setCreateAt(bill.getCreateAt());
        vo.setUpdateAt(bill.getUpdateAt());
        vo.setFeeList(feeList);
        vo.setPaymentList(paymentList);
        vo.setOperateLogList(includeOperateLog
            ? bizOperateLogRepo.listByBiz(BizOperateBizTypeEnum.OWNER_PAYABLE_BILL.getCode(), bill.getId())
            : Collections.emptyList());
        return vo;
    }

    @com.homi.common.lib.annotation.BizOperateLog(
        bizType = BizOperateBizTypeEnum.OWNER_PAYABLE_BILL,
        operateType = BizOperateTypeEnum.CREATE,
        operateDesc = "新增包租应付单",
        bizIdExpr = "#result",
        remarkExpr = "#p0.remark",
        saveAfterSnapshot = true,
        snapshotProvider = "ownerPayableBillSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long create(OwnerPayableBillCreateDTO dto, Long operatorId, String operatorName) {
        validateSaveDto(dto);
        OwnerContract contract = mustGetContract(dto.getContractId());
        OwnerPayableBill bill = buildBill(dto, contract, operatorId, null);
        ownerPayableBillRepo.save(bill);
        saveBillFees(bill, dto.getFeeList());
        return bill.getId();
    }

    @com.homi.common.lib.annotation.BizOperateLog(
        bizType = BizOperateBizTypeEnum.OWNER_PAYABLE_BILL,
        operateType = BizOperateTypeEnum.UPDATE,
        operateDesc = "修改包租应付单",
        bizIdExpr = "#p0.billId",
        remarkExpr = "#p0.remark",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "ownerPayableBillSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long update(OwnerPayableBillUpdateDTO dto, Long operatorId, String operatorName) {
        if (dto == null || dto.getBillId() == null) {
            throw new IllegalArgumentException("应付单ID不能为空");
        }
        validateSaveDto(dto);
        OwnerPayableBill existed = mustGetBill(dto.getBillId());
        ensureEditable(existed);
        OwnerContract contract = mustGetContract(dto.getContractId());
        existed.setOwnerId(contract.getOwnerId());
        existed.setContractId(dto.getContractId());
        existed.setSubjectNameSnapshot(buildContractSubjectSummary(contract.getId()));
        existed.setBillStartDate(dto.getBillStartDate());
        existed.setBillEndDate(dto.getBillEndDate());
        existed.setDueDate(dto.getDueDate());
        existed.setRemark(dto.getRemark());
        BigDecimal payableAmount = calcFeeTotal(dto.getFeeList());
        existed.setPayableAmount(payableAmount);
        existed.setAdjustAmount(BigDecimal.ZERO);
        existed.setPaidAmount(BigDecimal.ZERO);
        existed.setUnpaidAmount(payableAmount);
        existed.setUpdateBy(operatorId);
        existed.setUpdateAt(new Date());
        ownerPayableBillRepo.updateById(existed);
        ownerPayableBillFeeRepo.remove(new LambdaQueryWrapper<OwnerPayableBillFee>().eq(OwnerPayableBillFee::getBillId, existed.getId()));
        saveBillFees(existed, dto.getFeeList());
        return existed.getId();
    }

    @com.homi.common.lib.annotation.BizOperateLog(
        bizType = BizOperateBizTypeEnum.OWNER_PAYABLE_BILL,
        operateType = BizOperateTypeEnum.CANCEL,
        operateDesc = "作废包租应付单",
        bizIdExpr = "#p0.billId",
        remarkExpr = "#p0.voidReason",
        extraDataExpr = "{'voidReason': #p0.voidReason}",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "ownerPayableBillSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long voidBill(OwnerPayableBillVoidDTO dto, Long operatorId, String operatorName) {
        if (dto == null || dto.getBillId() == null || StrUtil.isBlank(dto.getVoidReason())) {
            throw new IllegalArgumentException("作废参数不正确");
        }
        OwnerPayableBill bill = mustGetBill(dto.getBillId());
        ensureCancelable(bill);
        Date now = new Date();
        bill.setBillStatus(OwnerPayableBillStatusEnum.VOIDED.getCode());
        bill.setVoidReason(dto.getVoidReason());
        bill.setVoidBy(operatorId);
        bill.setVoidByName(operatorName);
        bill.setVoidAt(now);
        bill.setUpdateBy(operatorId);
        bill.setUpdateAt(now);
        ownerPayableBillRepo.updateById(bill);
        return bill.getId();
    }

    @com.homi.common.lib.annotation.BizOperateLog(
        bizType = BizOperateBizTypeEnum.OWNER_PAYABLE_BILL,
        operateType = BizOperateTypeEnum.PAY,
        operateDesc = "登记付款",
        bizIdExpr = "#p0.billId",
        remarkExpr = "#p0.remark",
        extraDataExpr = "{'payAmount': #p0.payAmount, 'payChannel': #p0.payChannel}",
        sourceType = BizOperateSourceTypeEnum.OWNER_PAYABLE_BILL_PAYMENT,
        sourceIdExpr = "#result",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "ownerPayableBillSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long createPayment(OwnerPayableBillPaymentCreateDTO dto, Long operatorId, String operatorName) {
        if (dto == null || dto.getBillId() == null) {
            throw new IllegalArgumentException("应付单ID不能为空");
        }
        if (dto.getPayAmount() == null || dto.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("付款金额必须大于0");
        }
        if (dto.getPayAt() == null) {
            throw new IllegalArgumentException("付款时间不能为空");
        }
        if (dto.getPayChannel() == null) {
            throw new IllegalArgumentException("付款渠道不能为空");
        }
        OwnerPayableBill bill = mustGetBill(dto.getBillId());
        if (Objects.equals(bill.getBillStatus(), OwnerPayableBillStatusEnum.VOIDED.getCode())) {
            throw new IllegalArgumentException("已作废账单不可登记付款");
        }
        if (hasPendingPaymentRecord(bill.getId())) {
            throw new IllegalArgumentException("存在待审核付款记录，请先处理后再登记付款");
        }
        if (dto.getPayAmount().compareTo(defaultZero(bill.getUnpaidAmount())) > 0) {
            throw new IllegalArgumentException("付款金额不能超过未付金额");
        }
        cn.hutool.core.date.DateTime now = DateUtil.date();
        PaymentFlow paymentFlow = paymentFlowService.createOwnerPayableBillPaymentFlow(
            PaymentFlowService.CreateOwnerPayableBillPaymentCommand.builder()
                .bill(bill)
                .totalAmount(dto.getPayAmount())
                .payChannel(dto.getPayChannel())
                .thirdTradeNo(dto.getThirdTradeNo())
                .paymentVoucherUrl(firstVoucherUrl(dto.getVoucherUrls()))
                .payAt(dto.getPayAt())
                .ownerName(resolveOwnerName(bill.getOwnerId()))
                .operatorId(operatorId)
                .operatorName(operatorName)
                .remark(dto.getRemark())
                .status(PaymentFlowStatusEnum.PENDING_APPROVAL.getCode())
                .approvalStatus(BizApprovalStatusEnum.PENDING.getCode())
                .extJson(buildOwnerPayableBillPaymentExtJson(bill))
                .now(now)
                .build()
        );
        if (dto.getVoucherUrls() != null && !dto.getVoucherUrls().isEmpty()) {
            fileAttachRepo.recreateFileAttachList(paymentFlow.getId(), FileAttachBizTypeEnum.PAYMENT_FLOW_VOUCHER.getBizType(), dto.getVoucherUrls());
        }
        submitPaymentApproval(paymentFlow, bill, dto, operatorId);
        return paymentFlow.getId();
    }

    private void validateSaveDto(OwnerPayableBillCreateDTO dto) {
        if (dto == null || dto.getOwnerId() == null || dto.getContractId() == null) {
            throw new IllegalArgumentException("应付单基础信息不能为空");
        }
        if (dto.getBillStartDate() == null || dto.getBillEndDate() == null || dto.getDueDate() == null) {
            throw new IllegalArgumentException("账期和应付日期不能为空");
        }
        if (dto.getFeeList() == null || dto.getFeeList().isEmpty()) {
            throw new IllegalArgumentException("应付单费用不能为空");
        }
    }

    private LambdaQueryWrapper<OwnerPayableBill> buildWrapper(OwnerPayableBillQueryDTO query, List<Long> ownerIds) {
        return new LambdaQueryWrapper<OwnerPayableBill>()
            .eq(query.getOwnerId() != null, OwnerPayableBill::getOwnerId, query.getOwnerId())
            .eq(query.getContractId() != null, OwnerPayableBill::getContractId, query.getContractId())
            .like(StrUtil.isNotBlank(query.getBillNo()), OwnerPayableBill::getBillNo, query.getBillNo())
            .eq(query.getPaymentStatus() != null, OwnerPayableBill::getPaymentStatus, query.getPaymentStatus())
            .eq(query.getBillStatus() != null, OwnerPayableBill::getBillStatus, query.getBillStatus())
            .in(ownerIds != null, OwnerPayableBill::getOwnerId, ownerIds);
    }

    private PageVO<OwnerPayableBillListVO> emptyPage(OwnerPayableBillQueryDTO query) {
        return PageVO.<OwnerPayableBillListVO>builder()
            .currentPage(query.getCurrentPage())
            .pageSize(query.getPageSize())
            .total(0L)
            .pages(0L)
            .list(Collections.emptyList())
            .build();
    }

    private void fillEmptySummary(OwnerPayableBillSummaryVO vo) {
        vo.setBillCount(0L);
        vo.setTotalPayableAmount(BigDecimal.ZERO);
        vo.setTotalPaidAmount(BigDecimal.ZERO);
        vo.setTotalUnpaidAmount(BigDecimal.ZERO);
        vo.setVoidedCount(0L);
    }

    private BigDecimal sum(List<OwnerPayableBill> list, Function<OwnerPayableBill, BigDecimal> getter) {
        return list.stream().map(getter).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OwnerPayableBill mustGetBill(Long billId) {
        OwnerPayableBill bill = ownerPayableBillRepo.getById(billId);
        if (bill == null) {
            throw new IllegalArgumentException("包租业主应付单不存在");
        }
        return bill;
    }

    private OwnerContract mustGetContract(Long contractId) {
        OwnerContract contract = ownerContractRepo.getById(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("业主合同不存在");
        }
        return contract;
    }

    private void ensureEditable(OwnerPayableBill bill) {
        if (!Objects.equals(bill.getBillStatus(), OwnerPayableBillStatusEnum.NORMAL.getCode())) {
            throw new IllegalArgumentException("已作废账单不可修改");
        }
        if (hasActivePaymentRecord(bill.getId())
            || defaultZero(bill.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0
            || !Objects.equals(bill.getPaymentStatus(), OwnerPayableBillPaymentStatusEnum.UNPAID.getCode())) {
            throw new IllegalArgumentException("已有付款记录的账单不可直接修改");
        }
    }

    private void ensureCancelable(OwnerPayableBill bill) {
        if (!Objects.equals(bill.getBillStatus(), OwnerPayableBillStatusEnum.NORMAL.getCode())) {
            throw new IllegalArgumentException("账单已作废");
        }
        if (hasActivePaymentRecord(bill.getId())
            || defaultZero(bill.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0
            || !Objects.equals(bill.getPaymentStatus(), OwnerPayableBillPaymentStatusEnum.UNPAID.getCode())) {
            throw new IllegalArgumentException("仅未付款账单允许作废");
        }
    }

    private void submitPaymentApproval(PaymentFlow paymentFlow, OwnerPayableBill bill, OwnerPayableBillPaymentCreateDTO dto, Long operatorId) {
        approvalTemplate.submitIfNeed(
            ApprovalSubmitDTO.builder()
                .companyId(bill.getCompanyId())
                .bizType(ApprovalBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode())
                .bizId(paymentFlow.getId())
                .title("包租应付付款审批 - " + bill.getBillNo())
                .applicantId(operatorId)
                .remark(dto.getRemark())
                .build(),
            bizId -> updatePaymentApprovalStatus(
                bizId,
                BizApprovalStatusEnum.PENDING.getCode(),
                PaymentFlowStatusEnum.PENDING_APPROVAL.getCode(),
                operatorId
            ),
            bizId -> {
                ownerPayableBillPaymentApprovalService.completePayment(bizId);
            }
        );
    }

    private void updatePaymentApprovalStatus(Long paymentId, Integer approvalStatus, Integer paymentStatus, Long operatorId) {
        PaymentFlow paymentFlow = new PaymentFlow();
        paymentFlow.setId(paymentId);
        paymentFlow.setApprovalStatus(approvalStatus);
        paymentFlow.setStatus(paymentStatus);
        paymentFlow.setUpdateBy(operatorId);
        paymentFlow.setUpdateAt(new Date());
        paymentFlowRepo.updateById(paymentFlow);
    }

    private boolean hasPendingPaymentRecord(Long billId) {
        return paymentFlowRepo.lambdaQuery()
            .eq(PaymentFlow::getBizType, PaymentFlowBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode())
            .eq(PaymentFlow::getBizId, billId)
            .eq(PaymentFlow::getStatus, PaymentFlowStatusEnum.PENDING_APPROVAL.getCode())
            .count() > 0;
    }

    private boolean hasActivePaymentRecord(Long billId) {
        return paymentFlowRepo.lambdaQuery()
            .eq(PaymentFlow::getBizType, PaymentFlowBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode())
            .eq(PaymentFlow::getBizId, billId)
            .in(PaymentFlow::getStatus,
                PaymentFlowStatusEnum.PENDING_APPROVAL.getCode(),
                PaymentFlowStatusEnum.SUCCESS.getCode())
            .count() > 0;
    }

    private OwnerPayableBill buildBill(OwnerPayableBillCreateDTO dto, OwnerContract contract, Long operatorId, Long billId) {
        Date now = new Date();
        OwnerPayableBill bill = new OwnerPayableBill();
        bill.setId(billId);
        bill.setCompanyId(contract.getCompanyId());
        bill.setOwnerId(contract.getOwnerId());
        bill.setContractId(dto.getContractId());
        bill.setSubjectNameSnapshot(buildContractSubjectSummary(contract.getId()));
        bill.setBillNo(generateBillNo());
        bill.setBillScene(OwnerBillSceneEnum.REGULAR.getCode());
        bill.setBillStartDate(dto.getBillStartDate());
        bill.setBillEndDate(dto.getBillEndDate());
        bill.setDueDate(dto.getDueDate());
        BigDecimal payableAmount = calcFeeTotal(dto.getFeeList());
        bill.setPayableAmount(payableAmount);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setUnpaidAmount(payableAmount);
        bill.setAdjustAmount(BigDecimal.ZERO);
        bill.setPaymentStatus(OwnerPayableBillPaymentStatusEnum.UNPAID.getCode());
        bill.setBillStatus(OwnerPayableBillStatusEnum.NORMAL.getCode());
        bill.setGeneratedAt(now);
        bill.setRemark(dto.getRemark());
        bill.setCreateBy(operatorId);
        bill.setCreateAt(now);
        bill.setUpdateBy(operatorId);
        bill.setUpdateAt(now);
        return bill;
    }

    private void saveBillFees(OwnerPayableBill bill, List<OwnerPayableBillFeeDTO> feeList) {
        Date now = new Date();
        List<OwnerPayableBillFee> entities = feeList.stream().map(item -> {
            OwnerPayableBillFee fee = new OwnerPayableBillFee();
            fee.setCompanyId(bill.getCompanyId());
            fee.setBillId(bill.getId());
            fee.setSourceType(item.getSourceType());
            fee.setSourceId(item.getSourceId());
            fee.setSubjectNameSnapshot(bill.getSubjectNameSnapshot());
            fee.setFeeType(item.getFeeType());
            fee.setDictDataId(item.getDictDataId());
            fee.setFeeName(item.getFeeName());
            fee.setDirection(item.getDirection());
            fee.setAmount(defaultZero(item.getAmount()));
            fee.setBizDate(item.getBizDate());
            fee.setRemark(item.getRemark());
            fee.setFormulaSnapshot(item.getFormulaSnapshot());
            fee.setCreateAt(now);
            return fee;
        }).toList();
        ownerPayableBillFeeRepo.saveBatch(entities);
    }

    private BigDecimal calcFeeTotal(List<OwnerPayableBillFeeDTO> feeList) {
        return feeList.stream().map(OwnerPayableBillFeeDTO::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Long, List<OwnerPayableBillFeeVO>> buildFeeListMap(Collection<Long> billIds) {
        if (CollectionUtils.isEmpty(billIds)) {
            return Collections.emptyMap();
        }
        return ownerPayableBillFeeRepo.lambdaQuery()
            .in(OwnerPayableBillFee::getBillId, billIds)
            .orderByAsc(OwnerPayableBillFee::getId)
            .list()
            .stream()
            .collect(Collectors.groupingBy(
                OwnerPayableBillFee::getBillId,
                Collectors.mapping(this::toFeeVO, Collectors.toList())
            ));
    }

    private Map<Long, List<OwnerPayableBillPaymentVO>> buildPaymentListMap(Collection<Long> billIds) {
        if (CollectionUtils.isEmpty(billIds)) {
            return Collections.emptyMap();
        }
        List<PaymentFlow> list = paymentFlowRepo.lambdaQuery()
            .eq(PaymentFlow::getBizType, PaymentFlowBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode())
            .in(PaymentFlow::getBizId, billIds)
            .orderByDesc(PaymentFlow::getPayAt)
            .orderByDesc(PaymentFlow::getId)
            .list();
        if (list.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = list.stream().map(PaymentFlow::getId).toList();
        Map<Long, List<String>> voucherMap = fileAttachRepo.lambdaQuery()
            .in(FileAttach::getBizId, ids)
            .eq(FileAttach::getBizType, FileAttachBizTypeEnum.PAYMENT_FLOW_VOUCHER.getBizType())
            .orderByAsc(FileAttach::getSortOrder)
            .list()
            .stream()
            .collect(Collectors.groupingBy(FileAttach::getBizId, Collectors.mapping(FileAttach::getFileUrl, Collectors.toList())));
        Map<Long, Long> financeFlowIdMap = financeFlowService.getListByPaymentFlowIds(ids).stream()
            .filter(item -> item.getPaymentFlowId() != null)
            .collect(Collectors.toMap(FinanceFlow::getPaymentFlowId, FinanceFlow::getId, (left, right) -> left));
        return list.stream()
            .collect(Collectors.groupingBy(
                PaymentFlow::getBizId,
                Collectors.mapping(item -> toPaymentVO(item, voucherMap.get(item.getId()), financeFlowIdMap.get(item.getId())), Collectors.toList())
            ));
    }

    /**
     * 业主应付账单列表VO
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/4/16 16:45
     *
     * @param item     应付账单
     * @param owner    业主
     * @param contract 合同
     * @return com.homi.model.owner.vo.OwnerPayableBillListVO
     */
    private OwnerPayableBillListVO toListVO(OwnerPayableBill item, Owner owner, OwnerContract contract) {
        OwnerPayableBillListVO vo = new OwnerPayableBillListVO();
        vo.setBillId(item.getId());
        vo.setBillNo(item.getBillNo());
        vo.setBillScene(item.getBillScene());
        vo.setOwnerId(item.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getOwnerName() : null);
        vo.setOwnerPhone(owner != null ? owner.getOwnerPhone() : null);
        vo.setContractId(item.getContractId());
        vo.setContractNo(contract != null ? contract.getContractNo() : null);
        vo.setSubjectName(item.getSubjectNameSnapshot());
        vo.setBillStartDate(item.getBillStartDate());
        vo.setBillEndDate(item.getBillEndDate());
        vo.setDueDate(item.getDueDate());
        vo.setPayableAmount(item.getPayableAmount());
        vo.setPaidAmount(item.getPaidAmount());
        vo.setUnpaidAmount(item.getUnpaidAmount());
        vo.setAdjustAmount(item.getAdjustAmount());
        vo.setPaymentStatus(item.getPaymentStatus());
        vo.setBillStatus(item.getBillStatus());
        vo.setGeneratedAt(item.getGeneratedAt());
        vo.setVoidAt(item.getVoidAt());
        return vo;
    }

    private OwnerPayableBillFeeVO toFeeVO(OwnerPayableBillFee item) {
        OwnerPayableBillFeeVO vo = new OwnerPayableBillFeeVO();
        vo.setId(item.getId());
        vo.setSourceType(item.getSourceType());
        vo.setSourceId(item.getSourceId());
        vo.setFeeType(item.getFeeType());
        vo.setDictDataId(item.getDictDataId());
        vo.setFeeName(item.getFeeName());
        vo.setDirection(item.getDirection());
        vo.setAmount(item.getAmount());
        vo.setBizDate(item.getBizDate());
        vo.setRemark(item.getRemark());
        vo.setFormulaSnapshot(item.getFormulaSnapshot());
        return vo;
    }

    private OwnerPayableBillPaymentVO toPaymentVO(PaymentFlow item, List<String> voucherUrls, Long financeFlowId) {
        OwnerPayableBillPaymentVO vo = new OwnerPayableBillPaymentVO();
        vo.setPaymentId(item.getId());
        vo.setPaymentNo(item.getPaymentNo());
        vo.setPayAmount(item.getAmount());
        vo.setPayAt(item.getPayAt());
        vo.setPayChannel(item.getChannel());
        vo.setThirdTradeNo(item.getThirdTradeNo());
        vo.setRemark(item.getRemark());
        vo.setPaymentStatus(item.getStatus());
        vo.setApprovalStatus(item.getApprovalStatus());
        vo.setFinanceFlowId(financeFlowId);
        vo.setVoucherUrls(voucherUrls == null ? Collections.emptyList() : voucherUrls);
        vo.setCreateAt(item.getCreateAt());
        return vo;
    }

    private String generateBillNo() {
        return "OPB" + IdUtil.getSnowflakeNextIdStr();
    }

    private String firstVoucherUrl(List<String> voucherUrls) {
        return voucherUrls == null || voucherUrls.isEmpty() ? null : voucherUrls.get(0);
    }

    private String resolveOwnerName(Long ownerId) {
        if (ownerId == null) {
            return null;
        }
        Owner owner = ownerRepo.getById(ownerId);
        return owner == null ? null : owner.getOwnerName();
    }

    private String buildOwnerPayableBillPaymentExtJson(OwnerPayableBill bill) {
        return JSONUtil.createObj()
            .set("billId", bill.getId())
            .set("billNo", bill.getBillNo())
            .set("ownerId", bill.getOwnerId())
            .set("contractId", bill.getContractId())
            .toString();
    }

    private String buildContractSubjectSummary(Long contractId) {
        List<OwnerContractSubject> subjectList = ownerContractSubjectRepo.listByContractId(contractId);
        if (CollectionUtils.isEmpty(subjectList)) {
            return "包租合同房源";
        }
        if (subjectList.size() == 1) {
            return StrUtil.blankToDefault(subjectList.get(0).getSubjectNameSnapshot(), "包租合同房源");
        }
        String joined = subjectList.stream()
            .map(OwnerContractSubject::getSubjectNameSnapshot)
            .filter(StrUtil::isNotBlank)
            .limit(2)
            .collect(Collectors.joining("、"));
        return StrUtil.isBlank(joined) ? "包租合同房源" : joined + " 等" + subjectList.size() + "项";
    }

    private BigDecimal defaultZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
