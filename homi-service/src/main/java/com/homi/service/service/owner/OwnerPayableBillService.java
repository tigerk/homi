package com.homi.service.service.owner;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.biz.BizOperateBizTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateTypeEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowChannelEnum;
import com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillPaymentStatusEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillStatusEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.BizOperateLog;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.Owner;
import com.homi.model.dao.entity.OwnerContract;
import com.homi.model.dao.entity.OwnerPayableBill;
import com.homi.model.dao.entity.OwnerPayableBillLine;
import com.homi.model.dao.entity.OwnerPayableBillPayment;
import com.homi.model.dao.repo.BizOperateLogRepo;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.OwnerContractRepo;
import com.homi.model.dao.repo.OwnerPayableBillLineRepo;
import com.homi.model.dao.repo.OwnerPayableBillPaymentRepo;
import com.homi.model.dao.repo.OwnerPayableBillRepo;
import com.homi.model.dao.repo.OwnerRepo;
import com.homi.model.owner.dto.OwnerPayableBillCancelDTO;
import com.homi.model.owner.dto.OwnerPayableBillCreateDTO;
import com.homi.model.owner.dto.OwnerPayableBillIdDTO;
import com.homi.model.owner.dto.OwnerPayableBillLineDTO;
import com.homi.model.owner.dto.OwnerPayableBillPaymentCreateDTO;
import com.homi.model.owner.dto.OwnerPayableBillQueryDTO;
import com.homi.model.owner.dto.OwnerPayableBillUpdateDTO;
import com.homi.model.owner.vo.BizOperateLogVO;
import com.homi.model.owner.vo.OwnerPayableBillDetailVO;
import com.homi.model.owner.vo.OwnerPayableBillLineVO;
import com.homi.model.owner.vo.OwnerPayableBillListVO;
import com.homi.model.owner.vo.OwnerPayableBillPaymentVO;
import com.homi.model.owner.vo.OwnerPayableBillSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerPayableBillService {
    private final OwnerPayableBillRepo ownerPayableBillRepo;
    private final OwnerPayableBillLineRepo ownerPayableBillLineRepo;
    private final OwnerPayableBillPaymentRepo ownerPayableBillPaymentRepo;
    private final OwnerRepo ownerRepo;
    private final OwnerContractRepo ownerContractRepo;
    private final FileAttachRepo fileAttachRepo;
    private final BizOperateLogRepo bizOperateLogRepo;
    private final BizOperateLogService bizOperateLogService;

    public PageVO<OwnerPayableBillListVO> page(OwnerPayableBillQueryDTO query) {
        Page<OwnerPayableBill> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        List<Long> ownerIds = ownerRepo.getOwnerIdsByOwnerName(query.getOwnerName());
        if (ownerIds != null && ownerIds.isEmpty()) {
            return emptyPage(query);
        }
        LambdaQueryWrapper<OwnerPayableBill> wrapper = buildWrapper(query, ownerIds);
        wrapper.orderByDesc(OwnerPayableBill::getGeneratedAt).orderByDesc(OwnerPayableBill::getId);
        Page<OwnerPayableBill> result = ownerPayableBillRepo.page(page, wrapper);
        Map<Long, Owner> ownerMap = ownerRepo.listByIds(result.getRecords().stream().map(OwnerPayableBill::getOwnerId).filter(Objects::nonNull).distinct().toList())
            .stream().collect(Collectors.toMap(Owner::getId, Function.identity()));
        Map<Long, OwnerContract> contractMap = ownerContractRepo.listByIds(result.getRecords().stream().map(OwnerPayableBill::getContractId).filter(Objects::nonNull).distinct().toList())
            .stream().collect(Collectors.toMap(OwnerContract::getId, Function.identity()));
        List<OwnerPayableBillListVO> list = result.getRecords().stream().map(item -> toListVO(item, ownerMap.get(item.getOwnerId()), contractMap.get(item.getContractId()))).toList();
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
        vo.setSubjectType(OwnerContractSubjectTypeEnum.fromCode(bill.getSubjectType()));
        vo.setSubjectId(bill.getSubjectId());
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
        vo.setCreateTime(bill.getCreateTime());
        vo.setUpdateTime(bill.getUpdateTime());
        vo.setLineList(ownerPayableBillLineRepo.lambdaQuery().eq(OwnerPayableBillLine::getBillId, bill.getId()).orderByAsc(OwnerPayableBillLine::getId).list()
            .stream().map(this::toLineVO).toList());
        vo.setPaymentList(buildPaymentList(bill.getId()));
        vo.setOperateLogList(bizOperateLogRepo.lambdaQuery()
            .eq(BizOperateLog::getBizType, BizOperateBizTypeEnum.OWNER_PAYABLE_BILL.getCode())
            .eq(BizOperateLog::getBizId, bill.getId())
            .orderByDesc(BizOperateLog::getId)
            .list().stream().map(this::toLogVO).toList());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(OwnerPayableBillCreateDTO dto, Long operatorId, String operatorName) {
        validateSaveDto(dto);
        OwnerContract contract = mustGetContract(dto.getContractId());
        OwnerPayableBill bill = buildBill(dto, contract, operatorId, null);
        ownerPayableBillRepo.save(bill);
        saveBillLines(bill, dto.getLineList());
        bizOperateLogService.saveLog(contract.getCompanyId(), BizOperateBizTypeEnum.OWNER_PAYABLE_BILL.getCode(), bill.getId(),
            BizOperateTypeEnum.CREATE.getCode(), "新增包租应付单", dto.getRemark(), null, bill, null, null, null, operatorId, operatorName);
        return bill.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long update(OwnerPayableBillUpdateDTO dto, Long operatorId, String operatorName) {
        if (dto == null || dto.getBillId() == null) {
            throw new IllegalArgumentException("应付单ID不能为空");
        }
        validateSaveDto(dto);
        OwnerPayableBill existed = mustGetBill(dto.getBillId());
        ensureEditable(existed);
        OwnerPayableBill before = copyBillSnapshot(existed);
        OwnerContract contract = mustGetContract(dto.getContractId());
        existed.setOwnerId(dto.getOwnerId());
        existed.setContractId(dto.getContractId());
        existed.setSubjectType(dto.getSubjectType() == null ? null : dto.getSubjectType().getCode());
        existed.setSubjectId(dto.getSubjectId());
        existed.setSubjectNameSnapshot(dto.getSubjectName());
        existed.setBillStartDate(dto.getBillStartDate());
        existed.setBillEndDate(dto.getBillEndDate());
        existed.setDueDate(dto.getDueDate());
        existed.setRemark(dto.getRemark());
        BigDecimal payableAmount = calcLineTotal(dto.getLineList());
        existed.setPayableAmount(payableAmount);
        existed.setAdjustAmount(BigDecimal.ZERO);
        existed.setPaidAmount(BigDecimal.ZERO);
        existed.setUnpaidAmount(payableAmount);
        existed.setUpdateBy(operatorId);
        existed.setUpdateTime(new Date());
        ownerPayableBillRepo.updateById(existed);
        ownerPayableBillLineRepo.remove(new LambdaQueryWrapper<OwnerPayableBillLine>().eq(OwnerPayableBillLine::getBillId, existed.getId()));
        saveBillLines(existed, dto.getLineList());
        bizOperateLogService.saveLog(contract.getCompanyId(), BizOperateBizTypeEnum.OWNER_PAYABLE_BILL.getCode(), existed.getId(),
            BizOperateTypeEnum.UPDATE.getCode(), "修改包租应付单", dto.getRemark(), before, existed, null, null, null, operatorId, operatorName);
        return existed.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long cancel(OwnerPayableBillCancelDTO dto, Long operatorId, String operatorName) {
        if (dto == null || dto.getBillId() == null || StrUtil.isBlank(dto.getCancelReason())) {
            throw new IllegalArgumentException("作废参数不正确");
        }
        OwnerPayableBill bill = mustGetBill(dto.getBillId());
        ensureCancelable(bill);
        OwnerPayableBill before = copyBillSnapshot(bill);
        Date now = new Date();
        bill.setBillStatus(OwnerPayableBillStatusEnum.CANCELED.getCode());
        bill.setCancelReason(dto.getCancelReason());
        bill.setCancelBy(operatorId);
        bill.setCancelByName(operatorName);
        bill.setCancelAt(now);
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        ownerPayableBillRepo.updateById(bill);
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("cancelReason", dto.getCancelReason());
        bizOperateLogService.saveLog(bill.getCompanyId(), BizOperateBizTypeEnum.OWNER_PAYABLE_BILL.getCode(), bill.getId(),
            BizOperateTypeEnum.CANCEL.getCode(), "作废包租应付单", dto.getCancelReason(), before, bill, extraData, null, null, operatorId, operatorName);
        return bill.getId();
    }

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
        payment.setCreateTime(now);
        payment.setUpdateBy(operatorId);
        payment.setUpdateTime(now);
        ownerPayableBillPaymentRepo.save(payment);
        if (dto.getVoucherUrls() != null && !dto.getVoucherUrls().isEmpty()) {
            fileAttachRepo.recreateFileAttachList(payment.getId(), FileAttachBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT_VOUCHER.getBizType(), dto.getVoucherUrls());
        }
        bill.setPaidAmount(defaultZero(bill.getPaidAmount()).add(dto.getPayAmount()));
        bill.setUnpaidAmount(defaultZero(bill.getPayableAmount()).subtract(defaultZero(bill.getPaidAmount())).max(BigDecimal.ZERO));
        bill.setPaymentStatus(resolvePaymentStatus(bill));
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        ownerPayableBillRepo.updateById(bill);
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("payAmount", dto.getPayAmount());
        extraData.put("payChannel", dto.getPayChannel().getCode());
        bizOperateLogService.saveLog(bill.getCompanyId(), BizOperateBizTypeEnum.OWNER_PAYABLE_BILL.getCode(), bill.getId(),
            BizOperateTypeEnum.PAY.getCode(), "登记付款", dto.getRemark(), null, payment, extraData, "OWNER_PAYABLE_BILL_PAYMENT", payment.getId(), operatorId, operatorName);
        return payment.getId();
    }

    private void validateSaveDto(OwnerPayableBillCreateDTO dto) {
        if (dto == null || dto.getOwnerId() == null || dto.getContractId() == null) {
            throw new IllegalArgumentException("应付单基础信息不能为空");
        }
        if (dto.getBillStartDate() == null || dto.getBillEndDate() == null || dto.getDueDate() == null) {
            throw new IllegalArgumentException("账期和应付日期不能为空");
        }
        if (dto.getLineList() == null || dto.getLineList().isEmpty()) {
            throw new IllegalArgumentException("应付单明细不能为空");
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
        bill.setOwnerId(dto.getOwnerId());
        bill.setContractId(dto.getContractId());
        bill.setSubjectType(dto.getSubjectType() == null ? null : dto.getSubjectType().getCode());
        bill.setSubjectId(dto.getSubjectId());
        bill.setSubjectNameSnapshot(dto.getSubjectName());
        bill.setBillNo(generateBillNo());
        bill.setBillStartDate(dto.getBillStartDate());
        bill.setBillEndDate(dto.getBillEndDate());
        bill.setDueDate(dto.getDueDate());
        BigDecimal payableAmount = calcLineTotal(dto.getLineList());
        bill.setPayableAmount(payableAmount);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setUnpaidAmount(payableAmount);
        bill.setAdjustAmount(BigDecimal.ZERO);
        bill.setPaymentStatus(OwnerPayableBillPaymentStatusEnum.UNPAID.getCode());
        bill.setBillStatus(OwnerPayableBillStatusEnum.NORMAL.getCode());
        bill.setGeneratedAt(now);
        bill.setRemark(dto.getRemark());
        bill.setCreateBy(operatorId);
        bill.setCreateTime(now);
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        return bill;
    }

    private void saveBillLines(OwnerPayableBill bill, List<OwnerPayableBillLineDTO> lineList) {
        Date now = new Date();
        List<OwnerPayableBillLine> entities = lineList.stream().map(item -> {
            OwnerPayableBillLine line = new OwnerPayableBillLine();
            line.setCompanyId(bill.getCompanyId());
            line.setBillId(bill.getId());
            line.setSourceType(item.getSourceType());
            line.setSourceId(item.getSourceId());
            line.setSubjectType(bill.getSubjectType());
            line.setSubjectId(bill.getSubjectId());
            line.setSubjectNameSnapshot(bill.getSubjectNameSnapshot());
            line.setItemType(item.getItemType());
            line.setItemName(item.getItemName());
            line.setDirection(item.getDirection());
            line.setAmount(defaultZero(item.getAmount()));
            line.setBizTime(item.getBizTime());
            line.setRemark(item.getRemark());
            line.setFormulaSnapshot(item.getFormulaSnapshot());
            line.setCreateTime(now);
            return line;
        }).toList();
        ownerPayableBillLineRepo.saveBatch(entities);
    }

    private BigDecimal calcLineTotal(List<OwnerPayableBillLineDTO> lineList) {
        return lineList.stream().map(OwnerPayableBillLineDTO::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private OwnerPayableBillListVO toListVO(OwnerPayableBill item, Owner owner, OwnerContract contract) {
        OwnerPayableBillListVO vo = new OwnerPayableBillListVO();
        vo.setBillId(item.getId());
        vo.setBillNo(item.getBillNo());
        vo.setOwnerId(item.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getOwnerName() : null);
        vo.setOwnerPhone(owner != null ? owner.getOwnerPhone() : null);
        vo.setContractId(item.getContractId());
        vo.setContractNo(contract != null ? contract.getContractNo() : null);
        vo.setSubjectType(OwnerContractSubjectTypeEnum.fromCode(item.getSubjectType()));
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

    private OwnerPayableBillLineVO toLineVO(OwnerPayableBillLine item) {
        OwnerPayableBillLineVO vo = new OwnerPayableBillLineVO();
        vo.setId(item.getId());
        vo.setSourceType(item.getSourceType());
        vo.setSourceId(item.getSourceId());
        vo.setItemType(item.getItemType());
        vo.setItemName(item.getItemName());
        vo.setDirection(item.getDirection());
        vo.setAmount(item.getAmount());
        vo.setBizTime(item.getBizTime());
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
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private BizOperateLogVO toLogVO(BizOperateLog item) {
        BizOperateLogVO vo = new BizOperateLogVO();
        vo.setId(item.getId());
        vo.setBizType(item.getBizType());
        vo.setBizId(item.getBizId());
        vo.setOperateType(item.getOperateType());
        vo.setOperateDesc(item.getOperateDesc());
        vo.setRemark(item.getRemark());
        vo.setExtraData(item.getExtraData());
        vo.setSourceType(item.getSourceType());
        vo.setSourceId(item.getSourceId());
        vo.setOperatorId(item.getOperatorId());
        vo.setOperatorName(item.getOperatorName());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private OwnerPayableBill copyBillSnapshot(OwnerPayableBill bill) {
        OwnerPayableBill copy = new OwnerPayableBill();
        copy.setId(bill.getId());
        copy.setCompanyId(bill.getCompanyId());
        copy.setOwnerId(bill.getOwnerId());
        copy.setContractId(bill.getContractId());
        copy.setSubjectType(bill.getSubjectType());
        copy.setSubjectId(bill.getSubjectId());
        copy.setSubjectNameSnapshot(bill.getSubjectNameSnapshot());
        copy.setBillNo(bill.getBillNo());
        copy.setBillStartDate(bill.getBillStartDate());
        copy.setBillEndDate(bill.getBillEndDate());
        copy.setDueDate(bill.getDueDate());
        copy.setPayableAmount(bill.getPayableAmount());
        copy.setPaidAmount(bill.getPaidAmount());
        copy.setUnpaidAmount(bill.getUnpaidAmount());
        copy.setAdjustAmount(bill.getAdjustAmount());
        copy.setPaymentStatus(bill.getPaymentStatus());
        copy.setBillStatus(bill.getBillStatus());
        copy.setCancelReason(bill.getCancelReason());
        copy.setCancelBy(bill.getCancelBy());
        copy.setCancelByName(bill.getCancelByName());
        copy.setCancelAt(bill.getCancelAt());
        copy.setGeneratedAt(bill.getGeneratedAt());
        copy.setRemark(bill.getRemark());
        return copy;
    }

    private String generateBillNo() {
        return "OPB" + IdUtil.getSnowflakeNextIdStr();
    }

    private String generatePaymentNo() {
        return "OPP" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }

    private BigDecimal defaultZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
