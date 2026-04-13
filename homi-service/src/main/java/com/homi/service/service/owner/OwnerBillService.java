package com.homi.service.service.owner;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowChannelEnum;
import com.homi.common.lib.enums.owner.OwnerBillBizTypeEnum;
import com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum;
import com.homi.common.lib.enums.owner.OwnerCooperationModeEnum;
import com.homi.common.lib.enums.owner.OwnerBillSettlementStatusEnum;
import com.homi.common.lib.enums.owner.OwnerWithdrawOperateEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.Owner;
import com.homi.model.dao.entity.OwnerAccount;
import com.homi.model.dao.entity.OwnerAccountFlow;
import com.homi.model.dao.entity.OwnerBill;
import com.homi.model.dao.entity.OwnerBillLine;
import com.homi.model.dao.entity.OwnerBillPayment;
import com.homi.model.dao.entity.OwnerBillReduction;
import com.homi.model.dao.entity.OwnerContract;
import com.homi.model.dao.entity.OwnerWithdrawApply;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.OwnerAccountFlowRepo;
import com.homi.model.dao.repo.OwnerAccountRepo;
import com.homi.model.dao.repo.OwnerBillLineRepo;
import com.homi.model.dao.repo.OwnerBillPaymentRepo;
import com.homi.model.dao.repo.OwnerBillReductionRepo;
import com.homi.model.dao.repo.OwnerBillRepo;
import com.homi.model.dao.repo.OwnerContractRepo;
import com.homi.model.dao.repo.OwnerRepo;
import com.homi.model.dao.repo.OwnerWithdrawApplyRepo;
import com.homi.model.owner.dto.OwnerBillIdDTO;
import com.homi.model.owner.dto.OwnerBillPaymentCreateDTO;
import com.homi.model.owner.dto.OwnerBillQueryDTO;
import com.homi.model.owner.dto.OwnerWithdrawApplyIdDTO;
import com.homi.model.owner.dto.OwnerWithdrawApplyQueryDTO;
import com.homi.model.owner.dto.OwnerWithdrawCreateDTO;
import com.homi.model.owner.dto.OwnerWithdrawOperateDTO;
import com.homi.model.owner.vo.OwnerAccountFlowVO;
import com.homi.model.owner.vo.OwnerBillDetailVO;
import com.homi.model.owner.vo.OwnerBillLineVO;
import com.homi.model.owner.vo.OwnerBillListVO;
import com.homi.model.owner.vo.OwnerBillPaymentVO;
import com.homi.model.owner.vo.OwnerBillReductionVO;
import com.homi.model.owner.vo.OwnerWithdrawApplyDetailVO;
import com.homi.model.owner.vo.OwnerWithdrawApplyListVO;
import com.homi.model.owner.vo.OwnerBillSummaryVO;
import com.homi.model.owner.vo.OwnerWithdrawSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerBillService {
    private final OwnerBillRepo ownerBillRepo;
    private final OwnerBillLineRepo ownerBillLineRepo;
    private final OwnerBillPaymentRepo ownerBillPaymentRepo;
    private final OwnerBillReductionRepo ownerBillReductionRepo;
    private final OwnerWithdrawApplyRepo ownerWithdrawApplyRepo;
    private final OwnerAccountFlowRepo ownerAccountFlowRepo;
    private final OwnerAccountRepo ownerAccountRepo;
    private final OwnerRepo ownerRepo;
    private final OwnerContractRepo ownerContractRepo;
    private final FileAttachRepo fileAttachRepo;

    public PageVO<OwnerBillListVO> pageOwnerBills(OwnerBillQueryDTO query) {
        Page<OwnerBill> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        LambdaQueryWrapper<OwnerBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getOwnerId() != null, OwnerBill::getOwnerId, query.getOwnerId());
        wrapper.eq(query.getContractId() != null, OwnerBill::getContractId, query.getContractId());
        wrapper.like(StrUtil.isNotBlank(query.getBillNo()), OwnerBill::getBillNo, query.getBillNo());
        List<Long> ownerIds = resolveOwnerIds(query.getOwnerName());
        List<Long> contractIds = resolveContractIds(query);
        if (ownerIds != null && ownerIds.isEmpty()) {
            return emptyBillPage(query);
        }
        if (contractIds != null && contractIds.isEmpty()) {
            return emptyBillPage(query);
        }
        wrapper.in(ownerIds != null, OwnerBill::getOwnerId, ownerIds);
        wrapper.in(contractIds != null, OwnerBill::getContractId, contractIds);
        wrapper.orderByDesc(OwnerBill::getGeneratedAt);
        wrapper.orderByDesc(OwnerBill::getId);
        Page<OwnerBill> result = ownerBillRepo.page(page, wrapper);

        Map<Long, Owner> ownerMap = ownerRepo.listByIds(result.getRecords().stream().map(OwnerBill::getOwnerId).filter(Objects::nonNull).distinct().toList())
            .stream().collect(Collectors.toMap(Owner::getId, item -> item));
        Map<Long, OwnerContract> contractMap = ownerContractRepo.listByIds(result.getRecords().stream().map(OwnerBill::getContractId).filter(Objects::nonNull).distinct().toList())
            .stream().collect(Collectors.toMap(OwnerContract::getId, item -> item));

        List<OwnerBillListVO> list = result.getRecords().stream().map(item -> toOwnerBillListVO(item, ownerMap.get(item.getOwnerId()), contractMap.get(item.getContractId()))).toList();
        return PageVO.<OwnerBillListVO>builder()
            .currentPage(result.getCurrent())
            .pageSize(result.getSize())
            .total(result.getTotal())
            .pages(result.getPages())
            .list(list)
            .build();
    }

    public OwnerBillDetailVO getOwnerBillDetail(OwnerBillIdDTO dto) {
        if (dto == null || dto.getBillId() == null) {
            throw new IllegalArgumentException("业主账单ID不能为空");
        }
        OwnerBill bill = ownerBillRepo.getById(dto.getBillId());
        if (bill == null) {
            throw new IllegalArgumentException("业主账单不存在");
        }
        Owner owner = ownerRepo.getById(bill.getOwnerId());
        OwnerContract contract = ownerContractRepo.getById(bill.getContractId());
        OwnerBillDetailVO vo = new OwnerBillDetailVO();
        vo.setBillId(bill.getId());
        vo.setBillNo(bill.getBillNo());
        vo.setBillBizType(OwnerBillBizTypeEnum.fromCode(bill.getBillBizType()));
        vo.setOwnerId(bill.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getOwnerName() : null);
        vo.setOwnerPhone(owner != null ? owner.getOwnerPhone() : null);
        vo.setContractId(bill.getContractId());
        vo.setContractNo(contract != null ? contract.getContractNo() : null);
        vo.setSubjectType(OwnerContractSubjectTypeEnum.fromCode(bill.getSubjectType()));
        vo.setSubjectId(bill.getSubjectId());
        vo.setSubjectName(bill.getSubjectNameSnapshot());
        vo.setCooperationMode(contract != null && contract.getCooperationMode() != null ? com.homi.common.lib.enums.owner.OwnerCooperationModeEnum.valueOf(contract.getCooperationMode()) : null);
        vo.setBillStart(bill.getBillStart());
        vo.setBillEnd(bill.getBillEnd());
        vo.setDueDate(bill.getDueDate());
        vo.setIncomeAmount(bill.getIncomeAmount());
        vo.setReductionAmount(bill.getReductionAmount());
        vo.setExpenseAmount(bill.getExpenseAmount());
        vo.setAdjustAmount(bill.getAdjustAmount());
        vo.setPayableAmount(bill.getPayableAmount());
        vo.setSettledAmount(bill.getSettledAmount());
        vo.setUnpaidAmount(defaultZero(bill.getPayableAmount()).subtract(defaultZero(bill.getSettledAmount())).max(BigDecimal.ZERO));
        vo.setWithdrawnAmount(bill.getWithdrawnAmount());
        vo.setFreezeAmount(bill.getFreezeAmount());
        vo.setWithdrawableAmount(OwnerCooperationModeEnum.MASTER_LEASE.equals(vo.getCooperationMode()) ? BigDecimal.ZERO : bill.getWithdrawableAmount());
        vo.setBillStatus(bill.getBillStatus());
        vo.setApprovalStatus(bill.getApprovalStatus());
        vo.setSettlementStatus(bill.getSettlementStatus());
        vo.setGeneratedAt(bill.getGeneratedAt());
        vo.setApprovedAt(bill.getApprovedAt());
        vo.setRemark(bill.getRemark());
        vo.setCreateTime(bill.getCreateTime());
        vo.setUpdateTime(bill.getUpdateTime());
        vo.setLineList(ownerBillLineRepo.list(new LambdaQueryWrapper<OwnerBillLine>().eq(OwnerBillLine::getBillId, bill.getId()).orderByAsc(OwnerBillLine::getId))
            .stream().map(this::toOwnerBillLineVO).toList());
        vo.setReductionList(ownerBillReductionRepo.list(new LambdaQueryWrapper<OwnerBillReduction>().eq(OwnerBillReduction::getBillId, bill.getId()).orderByAsc(OwnerBillReduction::getId))
            .stream().map(this::toOwnerBillReductionVO).toList());
        vo.setPaymentList(buildOwnerBillPaymentVOList(bill.getId()));
        return vo;
    }

    public OwnerBillSummaryVO summaryOwnerBills(OwnerBillQueryDTO query) {
        OwnerBillSummaryVO vo = new OwnerBillSummaryVO();
        List<Long> ownerIds = resolveOwnerIds(query.getOwnerName());
        List<Long> contractIds = resolveContractIds(query);
        if (ownerIds != null && ownerIds.isEmpty()) {
            vo.setBillCount(0L);
            vo.setTotalIncomeAmount(BigDecimal.ZERO);
            vo.setTotalPayableAmount(BigDecimal.ZERO);
            vo.setTotalSettledAmount(BigDecimal.ZERO);
            vo.setTotalUnpaidAmount(BigDecimal.ZERO);
            vo.setTotalWithdrawableAmount(BigDecimal.ZERO);
            return vo;
        }
        if (contractIds != null && contractIds.isEmpty()) {
            vo.setBillCount(0L);
            vo.setTotalIncomeAmount(BigDecimal.ZERO);
            vo.setTotalPayableAmount(BigDecimal.ZERO);
            vo.setTotalSettledAmount(BigDecimal.ZERO);
            vo.setTotalUnpaidAmount(BigDecimal.ZERO);
            vo.setTotalWithdrawableAmount(BigDecimal.ZERO);
            return vo;
        }
        List<OwnerBill> bills = ownerBillRepo.list(buildOwnerBillWrapper(query, ownerIds, contractIds));
        vo.setBillCount((long) bills.size());
        vo.setTotalIncomeAmount(sumBillAmount(bills, OwnerBill::getIncomeAmount));
        vo.setTotalPayableAmount(sumBillAmount(bills, OwnerBill::getPayableAmount));
        vo.setTotalSettledAmount(sumBillAmount(bills, OwnerBill::getSettledAmount));
        vo.setTotalUnpaidAmount(bills.stream()
            .map(item -> defaultZero(item.getPayableAmount()).subtract(defaultZero(item.getSettledAmount())).max(BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setTotalWithdrawableAmount(sumBillAmount(bills, OwnerBill::getWithdrawableAmount));
        return vo;
    }

    public PageVO<OwnerWithdrawApplyListVO> pageOwnerWithdrawApplies(OwnerWithdrawApplyQueryDTO query) {
        Page<OwnerWithdrawApply> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        LambdaQueryWrapper<OwnerWithdrawApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getOwnerId() != null, OwnerWithdrawApply::getOwnerId, query.getOwnerId());
        wrapper.like(StrUtil.isNotBlank(query.getApplyNo()), OwnerWithdrawApply::getApplyNo, query.getApplyNo());
        wrapper.eq(query.getApprovalStatus() != null, OwnerWithdrawApply::getApprovalStatus, query.getApprovalStatus());
        wrapper.eq(query.getWithdrawStatus() != null, OwnerWithdrawApply::getWithdrawStatus, query.getWithdrawStatus());
        List<Long> ownerIds = resolveOwnerIds(query.getOwnerName());
        if (ownerIds != null && ownerIds.isEmpty()) {
            return emptyWithdrawPage(query);
        }
        wrapper.in(ownerIds != null, OwnerWithdrawApply::getOwnerId, ownerIds);
        wrapper.orderByDesc(OwnerWithdrawApply::getAppliedAt);
        wrapper.orderByDesc(OwnerWithdrawApply::getId);
        Page<OwnerWithdrawApply> result = ownerWithdrawApplyRepo.page(page, wrapper);

        Map<Long, Owner> ownerMap = ownerRepo.listByIds(result.getRecords().stream().map(OwnerWithdrawApply::getOwnerId).filter(Objects::nonNull).distinct().toList())
            .stream().collect(Collectors.toMap(Owner::getId, item -> item));
        List<OwnerWithdrawApplyListVO> list = result.getRecords().stream().map(item -> toOwnerWithdrawListVO(item, ownerMap.get(item.getOwnerId()))).toList();
        return PageVO.<OwnerWithdrawApplyListVO>builder()
            .currentPage(result.getCurrent())
            .pageSize(result.getSize())
            .total(result.getTotal())
            .pages(result.getPages())
            .list(list)
            .build();
    }

    public OwnerWithdrawApplyDetailVO getOwnerWithdrawApplyDetail(OwnerWithdrawApplyIdDTO dto) {
        if (dto == null || dto.getApplyId() == null) {
            throw new IllegalArgumentException("提现申请ID不能为空");
        }
        OwnerWithdrawApply apply = ownerWithdrawApplyRepo.getById(dto.getApplyId());
        if (apply == null) {
            throw new IllegalArgumentException("提现申请不存在");
        }
        Owner owner = ownerRepo.getById(apply.getOwnerId());
        OwnerWithdrawApplyDetailVO vo = new OwnerWithdrawApplyDetailVO();
        vo.setApplyId(apply.getId());
        vo.setApplyNo(apply.getApplyNo());
        vo.setOwnerId(apply.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getOwnerName() : null);
        vo.setOwnerPhone(owner != null ? owner.getOwnerPhone() : null);
        vo.setApplyAmount(apply.getApplyAmount());
        vo.setFeeAmount(apply.getFeeAmount());
        vo.setActualAmount(apply.getActualAmount());
        vo.setApprovalStatus(apply.getApprovalStatus());
        vo.setWithdrawStatus(apply.getWithdrawStatus());
        vo.setPayeeName(apply.getPayeeName());
        vo.setPayeeAccountNo(apply.getPayeeAccountNo());
        vo.setPayeeBankName(apply.getPayeeBankName());
        vo.setChannel(apply.getChannel());
        vo.setThirdTradeNo(apply.getThirdTradeNo());
        vo.setFailureReason(apply.getFailureReason());
        vo.setRemark(apply.getRemark());
        vo.setAppliedAt(apply.getAppliedAt());
        vo.setApprovedAt(apply.getApprovedAt());
        vo.setPaidAt(apply.getPaidAt());
        vo.setCreateTime(apply.getCreateTime());
        vo.setUpdateTime(apply.getUpdateTime());
        vo.setFlowList(ownerAccountFlowRepo.list(new LambdaQueryWrapper<OwnerAccountFlow>()
                .eq(OwnerAccountFlow::getOwnerId, apply.getOwnerId())
                .eq(OwnerAccountFlow::getBizId, apply.getId())
                .orderByDesc(OwnerAccountFlow::getId))
            .stream().map(this::toOwnerAccountFlowVO).toList());
        return vo;
    }

    public OwnerWithdrawSummaryVO summaryOwnerWithdrawApplies(OwnerWithdrawApplyQueryDTO query) {
        OwnerWithdrawSummaryVO vo = new OwnerWithdrawSummaryVO();
        List<Long> ownerIds = resolveOwnerIds(query.getOwnerName());
        if (ownerIds != null && ownerIds.isEmpty()) {
            fillEmptyWithdrawSummary(vo);
            return vo;
        }
        List<OwnerWithdrawApply> list = ownerWithdrawApplyRepo.list(buildOwnerWithdrawWrapper(query, ownerIds));
        vo.setApplyCount((long) list.size());
        vo.setPendingApprovalCount(list.stream().filter(item -> BizApprovalStatusEnum.PENDING.getCode().equals(item.getApprovalStatus())).count());
        vo.setProcessingCount(list.stream().filter(item -> Integer.valueOf(1).equals(item.getWithdrawStatus())).count());
        vo.setSuccessCount(list.stream().filter(item -> Integer.valueOf(2).equals(item.getWithdrawStatus())).count());
        vo.setTotalApplyAmount(list.stream().map(OwnerWithdrawApply::getApplyAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setTotalActualAmount(list.stream().map(OwnerWithdrawApply::getActualAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        OwnerAccount account = query.getOwnerId() == null ? null : ownerAccountRepo.getByOwnerId(query.getOwnerId());
        vo.setAvailableAmount(account != null ? account.getAvailableAmount() : BigDecimal.ZERO);
        vo.setFrozenAmount(account != null ? account.getFrozenAmount() : BigDecimal.ZERO);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createOwnerBillPayment(OwnerBillPaymentCreateDTO dto, Long operatorId) {
        if (dto == null || dto.getBillId() == null) {
            throw new IllegalArgumentException("业主账单ID不能为空");
        }
        if (dto.getPayAmount() == null || dto.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("付款金额必须大于0");
        }
        if (dto.getPayTime() == null) {
            throw new IllegalArgumentException("付款时间不能为空");
        }
        if (dto.getPayChannel() == null) {
            throw new IllegalArgumentException("付款渠道不能为空");
        }

        OwnerBill bill = ownerBillRepo.getById(dto.getBillId());
        if (bill == null) {
            throw new IllegalArgumentException("业主账单不存在");
        }
        OwnerContract contract = ownerContractRepo.getById(bill.getContractId());
        if (contract == null || !OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode())) {
            throw new IllegalArgumentException("仅包租账单支持登记付款");
        }

        BigDecimal unpaidAmount = defaultZero(bill.getPayableAmount()).subtract(defaultZero(bill.getSettledAmount())).max(BigDecimal.ZERO);
        if (dto.getPayAmount().compareTo(unpaidAmount) > 0) {
            throw new IllegalArgumentException("付款金额不能超过当前未结金额");
        }

        Date now = new Date();
        OwnerBillPayment payment = new OwnerBillPayment();
        payment.setCompanyId(bill.getCompanyId());
        payment.setBillId(bill.getId());
        payment.setOwnerId(bill.getOwnerId());
        payment.setContractId(bill.getContractId());
        payment.setPaymentNo(generateOwnerBillPaymentNo());
        payment.setPayAmount(dto.getPayAmount());
        payment.setPayTime(dto.getPayTime());
        payment.setPayChannel(dto.getPayChannel().getCode());
        payment.setThirdTradeNo(dto.getThirdTradeNo());
        payment.setRemark(dto.getRemark());
        payment.setCreateBy(operatorId);
        payment.setCreateTime(now);
        payment.setUpdateBy(operatorId);
        payment.setUpdateTime(now);
        ownerBillPaymentRepo.save(payment);

        if (dto.getVoucherUrls() != null && !dto.getVoucherUrls().isEmpty()) {
            fileAttachRepo.recreateFileAttachList(payment.getId(), FileAttachBizTypeEnum.OWNER_BILL_PAYMENT_VOUCHER.getBizType(), dto.getVoucherUrls());
        }

        BigDecimal nextSettledAmount = defaultZero(bill.getSettledAmount()).add(dto.getPayAmount());
        bill.setSettledAmount(nextSettledAmount);
        if (nextSettledAmount.compareTo(defaultZero(bill.getPayableAmount())) >= 0) {
            bill.setSettlementStatus(OwnerBillSettlementStatusEnum.SETTLED.getCode());
        } else {
            bill.setSettlementStatus(OwnerBillSettlementStatusEnum.PART_SETTLED.getCode());
        }
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        ownerBillRepo.updateById(bill);
        return payment.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createOwnerWithdrawApply(OwnerWithdrawCreateDTO dto, Long operatorId) {
        if (dto == null || dto.getOwnerId() == null || dto.getApplyAmount() == null || dto.getApplyAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("提现申请参数不正确");
        }
        OwnerAccount account = ownerAccountRepo.getByOwnerId(dto.getOwnerId());
        if (account == null) {
            throw new IllegalArgumentException("业主账户不存在");
        }
        BigDecimal feeAmount = dto.getFeeAmount() == null ? BigDecimal.ZERO : dto.getFeeAmount();
        if (defaultZero(account.getAvailableAmount()).compareTo(dto.getApplyAmount()) < 0) {
            throw new IllegalArgumentException("可提现余额不足");
        }
        Date now = DateUtil.date();
        OwnerWithdrawApply apply = new OwnerWithdrawApply();
        apply.setCompanyId(account.getCompanyId());
        apply.setOwnerId(dto.getOwnerId());
        apply.setApplyNo("OWA" + System.currentTimeMillis());
        apply.setApplyAmount(dto.getApplyAmount());
        apply.setFeeAmount(feeAmount);
        apply.setActualAmount(dto.getApplyAmount().subtract(feeAmount));
        apply.setApprovalStatus(BizApprovalStatusEnum.PENDING.getCode());
        apply.setWithdrawStatus(0);
        apply.setPayeeName(dto.getPayeeName());
        apply.setPayeeAccountNo(dto.getPayeeAccountNo());
        apply.setPayeeBankName(dto.getPayeeBankName());
        apply.setRemark(dto.getRemark());
        apply.setAppliedAt(now);
        apply.setCreateBy(operatorId);
        apply.setCreateTime(now);
        apply.setUpdateBy(operatorId);
        apply.setUpdateTime(now);
        ownerWithdrawApplyRepo.save(apply);

        BigDecimal availableBefore = defaultZero(account.getAvailableAmount());
        BigDecimal frozenBefore = defaultZero(account.getFrozenAmount());
        account.setAvailableAmount(availableBefore.subtract(dto.getApplyAmount()));
        account.setFrozenAmount(frozenBefore.add(dto.getApplyAmount()));
        ownerAccountRepo.updateById(account);
        saveOwnerAccountFlow(account.getCompanyId(), dto.getOwnerId(), "OWNER_WITHDRAW_APPLY", apply.getId(), "OUT", "WITHDRAW_FREEZE",
            dto.getApplyAmount(), availableBefore, account.getAvailableAmount(), frozenBefore, account.getFrozenAmount(), "发起提现冻结", operatorId, now);
        return apply.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long operateOwnerWithdrawApply(OwnerWithdrawOperateDTO dto, Long operatorId) {
        if (dto == null || dto.getApplyId() == null || dto.getOperateType() == null) {
            throw new IllegalArgumentException("提现操作参数不正确");
        }
        OwnerWithdrawApply apply = ownerWithdrawApplyRepo.getById(dto.getApplyId());
        if (apply == null) {
            throw new IllegalArgumentException("提现申请不存在");
        }
        OwnerAccount account = ownerAccountRepo.getByOwnerId(apply.getOwnerId());
        if (account == null) {
            throw new IllegalArgumentException("业主账户不存在");
        }
        Date now = DateUtil.date();
        switch (dto.getOperateType()) {
            case APPROVE -> apply.setApprovalStatus(BizApprovalStatusEnum.APPROVED.getCode());
            case REJECT -> {
                apply.setApprovalStatus(BizApprovalStatusEnum.REJECTED.getCode());
                apply.setWithdrawStatus(4);
                unfreezeWithdrawAmount(account, apply.getApplyAmount(), apply.getOwnerId(), apply.getId(), "WITHDRAW_REJECT", "提现驳回解冻", operatorId, now);
            }
            case PAYING -> apply.setWithdrawStatus(1);
            case SUCCESS -> {
                apply.setWithdrawStatus(2);
                BigDecimal availableBefore = defaultZero(account.getAvailableAmount());
                BigDecimal frozenBefore = defaultZero(account.getFrozenAmount());
                account.setFrozenAmount(frozenBefore.subtract(apply.getApplyAmount()));
                account.setTotalWithdrawAmount(defaultZero(account.getTotalWithdrawAmount()).add(apply.getApplyAmount()));
                ownerAccountRepo.updateById(account);
                saveOwnerAccountFlow(account.getCompanyId(), apply.getOwnerId(), "OWNER_WITHDRAW_APPLY", apply.getId(), "OUT", "WITHDRAW_SUCCESS",
                    apply.getApplyAmount(), availableBefore, account.getAvailableAmount(), frozenBefore, account.getFrozenAmount(), "提现成功扣减冻结", operatorId, now);
                apply.setPaidAt(now);
                apply.setThirdTradeNo(dto.getThirdTradeNo());
                apply.setChannel(dto.getChannel());
            }
            case FAIL -> {
                apply.setWithdrawStatus(3);
                apply.setFailureReason(dto.getFailureReason());
                unfreezeWithdrawAmount(account, apply.getApplyAmount(), apply.getOwnerId(), apply.getId(), "WITHDRAW_FAIL", "提现失败解冻", operatorId, now);
            }
            case CANCEL -> {
                apply.setApprovalStatus(BizApprovalStatusEnum.WITHDRAWN.getCode());
                apply.setWithdrawStatus(4);
                unfreezeWithdrawAmount(account, apply.getApplyAmount(), apply.getOwnerId(), apply.getId(), "WITHDRAW_CANCEL", "提现取消解冻", operatorId, now);
            }
            default -> throw new IllegalArgumentException("不支持的提现操作");
        }
        apply.setUpdateBy(operatorId);
        apply.setUpdateTime(now);
        if (dto.getOperateType() == OwnerWithdrawOperateEnum.APPROVE) {
            apply.setApprovedAt(now);
        }
        ownerWithdrawApplyRepo.updateById(apply);
        return apply.getId();
    }

    private PageVO<OwnerBillListVO> emptyBillPage(OwnerBillQueryDTO query) {
        return PageVO.<OwnerBillListVO>builder()
            .currentPage(query.getCurrentPage())
            .pageSize(query.getPageSize())
            .total(0L)
            .pages(0L)
            .list(Collections.emptyList())
            .build();
    }

    private PageVO<OwnerWithdrawApplyListVO> emptyWithdrawPage(OwnerWithdrawApplyQueryDTO query) {
        return PageVO.<OwnerWithdrawApplyListVO>builder()
            .currentPage(query.getCurrentPage())
            .pageSize(query.getPageSize())
            .total(0L)
            .pages(0L)
            .list(Collections.emptyList())
            .build();
    }

    private List<Long> resolveOwnerIds(String ownerName) {
        if (StrUtil.isBlank(ownerName)) {
            return null;
        }
        return ownerRepo.list(new LambdaQueryWrapper<Owner>().like(Owner::getOwnerName, ownerName)).stream().map(Owner::getId).toList();
    }

    private LambdaQueryWrapper<OwnerBill> buildOwnerBillWrapper(OwnerBillQueryDTO query, List<Long> ownerIds, List<Long> contractIds) {
        LambdaQueryWrapper<OwnerBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getOwnerId() != null, OwnerBill::getOwnerId, query.getOwnerId());
        wrapper.eq(query.getContractId() != null, OwnerBill::getContractId, query.getContractId());
        wrapper.like(StrUtil.isNotBlank(query.getBillNo()), OwnerBill::getBillNo, query.getBillNo());
        wrapper.eq(query.getApprovalStatus() != null, OwnerBill::getApprovalStatus, query.getApprovalStatus());
        wrapper.eq(query.getSettlementStatus() != null, OwnerBill::getSettlementStatus, query.getSettlementStatus());
        wrapper.in(ownerIds != null, OwnerBill::getOwnerId, ownerIds);
        wrapper.in(contractIds != null, OwnerBill::getContractId, contractIds);
        return wrapper;
    }

    private LambdaQueryWrapper<OwnerWithdrawApply> buildOwnerWithdrawWrapper(OwnerWithdrawApplyQueryDTO query, List<Long> ownerIds) {
        LambdaQueryWrapper<OwnerWithdrawApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getOwnerId() != null, OwnerWithdrawApply::getOwnerId, query.getOwnerId());
        wrapper.like(StrUtil.isNotBlank(query.getApplyNo()), OwnerWithdrawApply::getApplyNo, query.getApplyNo());
        wrapper.eq(query.getApprovalStatus() != null, OwnerWithdrawApply::getApprovalStatus, query.getApprovalStatus());
        wrapper.eq(query.getWithdrawStatus() != null, OwnerWithdrawApply::getWithdrawStatus, query.getWithdrawStatus());
        wrapper.in(ownerIds != null, OwnerWithdrawApply::getOwnerId, ownerIds);
        return wrapper;
    }

    private BigDecimal sumBillAmount(List<OwnerBill> bills, java.util.function.Function<OwnerBill, BigDecimal> getter) {
        return bills.stream().map(getter).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void fillEmptyWithdrawSummary(OwnerWithdrawSummaryVO vo) {
        vo.setApplyCount(0L);
        vo.setPendingApprovalCount(0L);
        vo.setProcessingCount(0L);
        vo.setSuccessCount(0L);
        vo.setTotalApplyAmount(BigDecimal.ZERO);
        vo.setTotalActualAmount(BigDecimal.ZERO);
        vo.setAvailableAmount(BigDecimal.ZERO);
        vo.setFrozenAmount(BigDecimal.ZERO);
    }

    private void unfreezeWithdrawAmount(OwnerAccount account, BigDecimal amount, Long ownerId, Long applyId, String changeType, String remark, Long operatorId, Date now) {
        BigDecimal availableBefore = defaultZero(account.getAvailableAmount());
        BigDecimal frozenBefore = defaultZero(account.getFrozenAmount());
        account.setAvailableAmount(availableBefore.add(amount));
        account.setFrozenAmount(frozenBefore.subtract(amount));
        ownerAccountRepo.updateById(account);
        saveOwnerAccountFlow(account.getCompanyId(), ownerId, "OWNER_WITHDRAW_APPLY", applyId, "IN", changeType,
            amount, availableBefore, account.getAvailableAmount(), frozenBefore, account.getFrozenAmount(), remark, operatorId, now);
    }

    private void saveOwnerAccountFlow(Long companyId, Long ownerId, String bizType, Long bizId, String flowDirection, String changeType,
                                      BigDecimal amount, BigDecimal availableBefore, BigDecimal availableAfter,
                                      BigDecimal frozenBefore, BigDecimal frozenAfter, String remark, Long operatorId, Date now) {
        OwnerAccountFlow flow = new OwnerAccountFlow();
        flow.setCompanyId(companyId);
        flow.setOwnerId(ownerId);
        flow.setBizType(bizType);
        flow.setBizId(bizId);
        flow.setFlowDirection(flowDirection);
        flow.setChangeType(changeType);
        flow.setAmount(amount);
        flow.setAvailableBefore(availableBefore);
        flow.setAvailableAfter(availableAfter);
        flow.setFrozenBefore(frozenBefore);
        flow.setFrozenAfter(frozenAfter);
        flow.setRemark(remark);
        flow.setCreateBy(operatorId);
        flow.setCreateTime(now);
        ownerAccountFlowRepo.save(flow);
    }

    private OwnerBillListVO toOwnerBillListVO(OwnerBill item, Owner owner, OwnerContract contract) {
        OwnerBillListVO vo = new OwnerBillListVO();
        vo.setBillId(item.getId());
        vo.setBillNo(item.getBillNo());
        vo.setBillBizType(OwnerBillBizTypeEnum.fromCode(item.getBillBizType()));
        vo.setOwnerId(item.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getOwnerName() : null);
        vo.setOwnerPhone(owner != null ? owner.getOwnerPhone() : null);
        vo.setContractId(item.getContractId());
        vo.setContractNo(contract != null ? contract.getContractNo() : null);
        vo.setSubjectType(OwnerContractSubjectTypeEnum.fromCode(item.getSubjectType()));
        vo.setSubjectId(item.getSubjectId());
        vo.setSubjectName(item.getSubjectNameSnapshot());
        vo.setCooperationMode(contract != null && contract.getCooperationMode() != null ? com.homi.common.lib.enums.owner.OwnerCooperationModeEnum.valueOf(contract.getCooperationMode()) : null);
        vo.setBillStart(item.getBillStart());
        vo.setBillEnd(item.getBillEnd());
        vo.setDueDate(item.getDueDate());
        vo.setIncomeAmount(item.getIncomeAmount());
        vo.setReductionAmount(item.getReductionAmount());
        vo.setExpenseAmount(item.getExpenseAmount());
        vo.setPayableAmount(item.getPayableAmount());
        vo.setSettledAmount(item.getSettledAmount());
        vo.setUnpaidAmount(defaultZero(item.getPayableAmount()).subtract(defaultZero(item.getSettledAmount())).max(BigDecimal.ZERO));
        vo.setWithdrawableAmount(contract != null && OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode()) ? BigDecimal.ZERO : item.getWithdrawableAmount());
        vo.setBillStatus(item.getBillStatus());
        vo.setApprovalStatus(item.getApprovalStatus());
        vo.setSettlementStatus(item.getSettlementStatus());
        vo.setGeneratedAt(item.getGeneratedAt());
        vo.setApprovedAt(item.getApprovedAt());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private BigDecimal defaultZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private List<Long> resolveContractIds(OwnerBillQueryDTO query) {
        if (query.getCooperationMode() == null) {
            return null;
        }
        return ownerContractRepo.list(new LambdaQueryWrapper<OwnerContract>()
                .eq(OwnerContract::getCooperationMode, query.getCooperationMode().name())
                .select(OwnerContract::getId))
            .stream()
            .map(OwnerContract::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private OwnerWithdrawApplyListVO toOwnerWithdrawListVO(OwnerWithdrawApply item, Owner owner) {
        OwnerWithdrawApplyListVO vo = new OwnerWithdrawApplyListVO();
        vo.setApplyId(item.getId());
        vo.setApplyNo(item.getApplyNo());
        vo.setOwnerId(item.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getOwnerName() : null);
        vo.setOwnerPhone(owner != null ? owner.getOwnerPhone() : null);
        vo.setApplyAmount(item.getApplyAmount());
        vo.setFeeAmount(item.getFeeAmount());
        vo.setActualAmount(item.getActualAmount());
        vo.setApprovalStatus(item.getApprovalStatus());
        vo.setWithdrawStatus(item.getWithdrawStatus());
        vo.setPayeeName(item.getPayeeName());
        vo.setPayeeBankName(item.getPayeeBankName());
        vo.setChannel(item.getChannel());
        vo.setAppliedAt(item.getAppliedAt());
        vo.setApprovedAt(item.getApprovedAt());
        vo.setPaidAt(item.getPaidAt());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private OwnerBillLineVO toOwnerBillLineVO(OwnerBillLine item) {
        OwnerBillLineVO vo = new OwnerBillLineVO();
        vo.setId(item.getId());
        vo.setSourceType(item.getSourceType());
        vo.setSourceId(item.getSourceId());
        vo.setItemType(item.getItemType());
        vo.setItemName(item.getItemName());
        vo.setDirection(item.getDirection());
        vo.setAmount(item.getAmount());
        vo.setBizDate(item.getBizDate());
        vo.setRemark(item.getRemark());
        vo.setFormulaSnapshot(item.getFormulaSnapshot());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private OwnerBillReductionVO toOwnerBillReductionVO(OwnerBillReduction item) {
        OwnerBillReductionVO vo = new OwnerBillReductionVO();
        vo.setId(item.getId());
        vo.setSourceType(item.getSourceType());
        vo.setSourceId(item.getSourceId());
        vo.setReductionType(item.getReductionType());
        vo.setReductionName(item.getReductionName());
        vo.setAmount(item.getAmount());
        vo.setBizDate(item.getBizDate());
        vo.setRemark(item.getRemark());
        vo.setRuleSnapshot(item.getRuleSnapshot());
        vo.setStatus(item.getStatus());
        return vo;
    }

    private List<OwnerBillPaymentVO> buildOwnerBillPaymentVOList(Long billId) {
        if (billId == null) {
            return Collections.emptyList();
        }
        List<OwnerBillPayment> paymentList = ownerBillPaymentRepo.list(new LambdaQueryWrapper<OwnerBillPayment>()
            .eq(OwnerBillPayment::getBillId, billId)
            .orderByDesc(OwnerBillPayment::getPayTime)
            .orderByDesc(OwnerBillPayment::getId));
        if (paymentList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> paymentIds = paymentList.stream().map(OwnerBillPayment::getId).toList();
        Map<Long, List<String>> voucherMap = fileAttachRepo.lambdaQuery()
            .in(FileAttach::getBizId, paymentIds)
            .eq(FileAttach::getBizType, FileAttachBizTypeEnum.OWNER_BILL_PAYMENT_VOUCHER.getBizType())
            .orderByAsc(FileAttach::getSortOrder)
            .list()
            .stream()
            .collect(Collectors.groupingBy(FileAttach::getBizId, Collectors.mapping(FileAttach::getFileUrl, Collectors.toList())));
        return paymentList.stream().map(item -> toOwnerBillPaymentVO(item, voucherMap.get(item.getId()))).toList();
    }

    private OwnerBillPaymentVO toOwnerBillPaymentVO(OwnerBillPayment item, List<String> voucherUrls) {
        OwnerBillPaymentVO vo = new OwnerBillPaymentVO();
        vo.setPaymentId(item.getId());
        vo.setPaymentNo(item.getPaymentNo());
        vo.setPayAmount(item.getPayAmount());
        vo.setPayTime(item.getPayTime());
        vo.setPayChannel(item.getPayChannel() == null ? null : PaymentFlowChannelEnum.valueOf(item.getPayChannel()));
        vo.setThirdTradeNo(item.getThirdTradeNo());
        vo.setRemark(item.getRemark());
        vo.setVoucherUrls(voucherUrls == null ? Collections.emptyList() : voucherUrls);
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private String generateOwnerBillPaymentNo() {
        return "OBP" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }

    private OwnerAccountFlowVO toOwnerAccountFlowVO(OwnerAccountFlow item) {
        OwnerAccountFlowVO vo = new OwnerAccountFlowVO();
        vo.setId(item.getId());
        vo.setBizType(item.getBizType());
        vo.setBizId(item.getBizId());
        vo.setFlowDirection(item.getFlowDirection());
        vo.setChangeType(item.getChangeType());
        vo.setAmount(item.getAmount());
        vo.setAvailableBefore(item.getAvailableBefore());
        vo.setAvailableAfter(item.getAvailableAfter());
        vo.setFrozenBefore(item.getFrozenBefore());
        vo.setFrozenAfter(item.getFrozenAfter());
        vo.setRemark(item.getRemark());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }
}
