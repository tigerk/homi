package com.homi.service.service.owner;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.homi.common.lib.enums.biz.BizOperateBizTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateSourceTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateTypeEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowChannelEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillPaymentStatusEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillStatusEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.owner.dto.*;
import com.homi.model.owner.vo.*;
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
    private final OwnerPayableBillPaymentRepo ownerPayableBillPaymentRepo;
    private final OwnerRepo ownerRepo;
    private final OwnerContractRepo ownerContractRepo;
    private final OwnerContractSubjectRepo ownerContractSubjectRepo;
    private final FileAttachRepo fileAttachRepo;
    private final BizOperateLogRepo bizOperateLogRepo;

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
        vo.setCanceledCount(list.stream().filter(item -> Objects.equals(item.getBillStatus(), OwnerPayableBillStatusEnum.CANCELED.getCode())).count());
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
        Owner owner = ownerRepo.getById(bill.getOwnerId());
        OwnerContract contract = ownerContractRepo.getById(bill.getContractId());
        OwnerPayableBillDetailVO vo = new OwnerPayableBillDetailVO();
        vo.setBillId(bill.getId());
        vo.setBillNo(bill.getBillNo());
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
        vo.setCancelReason(bill.getCancelReason());
        vo.setCancelBy(bill.getCancelBy());
        vo.setCancelByName(bill.getCancelByName());
        vo.setCancelAt(bill.getCancelAt());
        vo.setGeneratedAt(bill.getGeneratedAt());
        vo.setRemark(bill.getRemark());
        vo.setCreateAt(bill.getCreateAt());
        vo.setUpdateAt(bill.getUpdateAt());
        vo.setFeeList(ownerPayableBillFeeRepo.lambdaQuery().eq(OwnerPayableBillFee::getBillId, bill.getId()).orderByAsc(OwnerPayableBillFee::getId).list()
            .stream().map(this::toFeeVO).toList());
        vo.setPaymentList(buildPaymentList(bill.getId()));
        vo.setOperateLogList(bizOperateLogRepo.listByBiz(BizOperateBizTypeEnum.OWNER_PAYABLE_BILL.getCode(), bill.getId()));
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
        remarkExpr = "#p0.cancelReason",
        extraDataExpr = "{'cancelReason': #p0.cancelReason}",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "ownerPayableBillSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long cancel(OwnerPayableBillCancelDTO dto, Long operatorId, String operatorName) {
        if (dto == null || dto.getBillId() == null || StrUtil.isBlank(dto.getCancelReason())) {
            throw new IllegalArgumentException("作废参数不正确");
        }
        OwnerPayableBill bill = mustGetBill(dto.getBillId());
        ensureCancelable(bill);
        Date now = new Date();
        bill.setBillStatus(OwnerPayableBillStatusEnum.CANCELED.getCode());
        bill.setCancelReason(dto.getCancelReason());
        bill.setCancelBy(operatorId);
        bill.setCancelByName(operatorName);
        bill.setCancelAt(now);
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
        extraDataExpr = "{'payAmount': #p0.payAmount, 'payChannel': #p0.payChannel != null ? #p0.payChannel.code : null}",
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
        if (Objects.equals(bill.getBillStatus(), OwnerPayableBillStatusEnum.CANCELED.getCode())) {
            throw new IllegalArgumentException("已作废账单不可登记付款");
        }
        if (dto.getPayAmount().compareTo(defaultZero(bill.getUnpaidAmount())) > 0) {
            throw new IllegalArgumentException("付款金额不能超过未付金额");
        }
        Date now = new Date();
        OwnerPayableBillPayment payment = new OwnerPayableBillPayment();
        payment.setCompanyId(bill.getCompanyId());
        payment.setBillId(bill.getId());
        payment.setPaymentNo(generatePaymentNo());
        payment.setPayAmount(dto.getPayAmount());
        payment.setPayAt(dto.getPayAt());
        payment.setPayChannel(dto.getPayChannel().getCode());
        payment.setThirdTradeNo(dto.getThirdTradeNo());
        payment.setRemark(dto.getRemark());
        payment.setCreateBy(operatorId);
        payment.setCreateAt(now);
        payment.setUpdateBy(operatorId);
        payment.setUpdateAt(now);
        ownerPayableBillPaymentRepo.save(payment);
        if (dto.getVoucherUrls() != null && !dto.getVoucherUrls().isEmpty()) {
            fileAttachRepo.recreateFileAttachList(payment.getId(), FileAttachBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT_VOUCHER.getBizType(), dto.getVoucherUrls());
        }
        bill.setPaidAmount(defaultZero(bill.getPaidAmount()).add(dto.getPayAmount()));
        bill.setUnpaidAmount(defaultZero(bill.getPayableAmount()).subtract(defaultZero(bill.getPaidAmount())).max(BigDecimal.ZERO));
        bill.setPaymentStatus(resolvePaymentStatus(bill));
        bill.setUpdateBy(operatorId);
        bill.setUpdateAt(now);
        ownerPayableBillRepo.updateById(bill);
        return payment.getId();
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
        vo.setCanceledCount(0L);
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
        if (ownerPayableBillPaymentRepo.lambdaQuery().eq(OwnerPayableBillPayment::getBillId, bill.getId()).count() > 0
            || defaultZero(bill.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0
            || !Objects.equals(bill.getPaymentStatus(), OwnerPayableBillPaymentStatusEnum.UNPAID.getCode())) {
            throw new IllegalArgumentException("已有付款记录的账单不可直接修改");
        }
    }

    private void ensureCancelable(OwnerPayableBill bill) {
        if (!Objects.equals(bill.getBillStatus(), OwnerPayableBillStatusEnum.NORMAL.getCode())) {
            throw new IllegalArgumentException("账单已作废");
        }
        if (ownerPayableBillPaymentRepo.lambdaQuery().eq(OwnerPayableBillPayment::getBillId, bill.getId()).count() > 0
            || defaultZero(bill.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0
            || !Objects.equals(bill.getPaymentStatus(), OwnerPayableBillPaymentStatusEnum.UNPAID.getCode())) {
            throw new IllegalArgumentException("仅未付款账单允许作废");
        }
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

    private Integer resolvePaymentStatus(OwnerPayableBill bill) {
        if (defaultZero(bill.getUnpaidAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            return OwnerPayableBillPaymentStatusEnum.PAID.getCode();
        }
        if (defaultZero(bill.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0) {
            return OwnerPayableBillPaymentStatusEnum.PART_PAID.getCode();
        }
        return OwnerPayableBillPaymentStatusEnum.UNPAID.getCode();
    }

    private List<OwnerPayableBillPaymentVO> buildPaymentList(Long billId) {
        List<OwnerPayableBillPayment> list = ownerPayableBillPaymentRepo.lambdaQuery()
            .eq(OwnerPayableBillPayment::getBillId, billId)
            .orderByDesc(OwnerPayableBillPayment::getPayAt)
            .orderByDesc(OwnerPayableBillPayment::getId)
            .list();
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = list.stream().map(OwnerPayableBillPayment::getId).toList();
        Map<Long, List<String>> voucherMap = fileAttachRepo.lambdaQuery()
            .in(FileAttach::getBizId, ids)
            .eq(FileAttach::getBizType, FileAttachBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT_VOUCHER.getBizType())
            .orderByAsc(FileAttach::getSortOrder)
            .list()
            .stream()
            .collect(Collectors.groupingBy(FileAttach::getBizId, Collectors.mapping(FileAttach::getFileUrl, Collectors.toList())));
        return list.stream().map(item -> toPaymentVO(item, voucherMap.get(item.getId()))).toList();
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
        vo.setCancelAt(item.getCancelAt());
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

    private OwnerPayableBillPaymentVO toPaymentVO(OwnerPayableBillPayment item, List<String> voucherUrls) {
        OwnerPayableBillPaymentVO vo = new OwnerPayableBillPaymentVO();
        vo.setPaymentId(item.getId());
        vo.setPaymentNo(item.getPaymentNo());
        vo.setPayAmount(item.getPayAmount());
        vo.setPayAt(item.getPayAt());
        vo.setPayChannel(item.getPayChannel() == null ? null : PaymentFlowChannelEnum.valueOf(item.getPayChannel()));
        vo.setThirdTradeNo(item.getThirdTradeNo());
        vo.setRemark(item.getRemark());
        vo.setVoucherUrls(voucherUrls == null ? Collections.emptyList() : voucherUrls);
        vo.setCreateAt(item.getCreateAt());
        return vo;
    }

    private String generateBillNo() {
        return "OPB" + IdUtil.getSnowflakeNextIdStr();
    }

    private String generatePaymentNo() {
        return "OPP" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
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
