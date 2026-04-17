package com.homi.service.service.owner;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.owner.dto.OwnerSettlementBillIdDTO;
import com.homi.model.owner.dto.OwnerSettlementBillQueryDTO;
import com.homi.model.owner.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerSettlementBillService {
    private final OwnerSettlementBillRepo ownerSettlementBillRepo;
    private final OwnerSettlementBillFeeRepo ownerSettlementBillFeeRepo;
    private final OwnerSettlementBillReductionRepo ownerSettlementBillReductionRepo;
    private final OwnerRepo ownerRepo;
    private final OwnerContractRepo ownerContractRepo;

    public PageVO<OwnerSettlementBillListVO> page(OwnerSettlementBillQueryDTO query) {
        Page<OwnerSettlementBill> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        List<Long> ownerIds = ownerRepo.getOwnerIdsByOwnerName(query.getOwnerName());
        if (ownerIds != null && ownerIds.isEmpty()) {
            return emptyPage(query);
        }
        LambdaQueryWrapper<OwnerSettlementBill> wrapper = buildWrapper(query, ownerIds);
        wrapper.orderByDesc(OwnerSettlementBill::getGeneratedAt).orderByDesc(OwnerSettlementBill::getId);
        Page<OwnerSettlementBill> result = ownerSettlementBillRepo.page(page, wrapper);

        List<OwnerSettlementBillListVO> list = Lists.newArrayList();
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            List<Long> settlementOwnerIds = result.getRecords().stream().map(OwnerSettlementBill::getOwnerId).filter(Objects::nonNull).distinct().toList();
            Map<Long, Owner> ownerMap = ownerRepo.listByIds(settlementOwnerIds).stream().collect(Collectors.toMap(Owner::getId, Function.identity()));

            List<Long> settlementContractIds = result.getRecords().stream().map(OwnerSettlementBill::getContractId).filter(Objects::nonNull).distinct().toList();
            Map<Long, OwnerContract> contractMap = ownerContractRepo.listByIds(settlementContractIds).stream().collect(Collectors.toMap(OwnerContract::getId, Function.identity()));

            list = result.getRecords().stream().map(item -> toListVO(item, ownerMap.get(item.getOwnerId()), contractMap.get(item.getContractId()))).toList();
        }

        return PageVO.<OwnerSettlementBillListVO>builder()
            .currentPage(result.getCurrent())
            .pageSize(result.getSize())
            .total(result.getTotal())
            .pages(result.getPages())
            .list(list)
            .build();
    }

    public OwnerSettlementBillSummaryVO summary(OwnerSettlementBillQueryDTO query) {
        OwnerSettlementBillSummaryVO vo = new OwnerSettlementBillSummaryVO();
        List<Long> ownerIds = ownerRepo.getOwnerIdsByOwnerName(query.getOwnerName());
        if (ownerIds != null && ownerIds.isEmpty()) {
            fillEmptySummary(vo);
            return vo;
        }
        List<OwnerSettlementBill> list = ownerSettlementBillRepo.list(buildWrapper(query, ownerIds));
        vo.setBillCount((long) list.size());
        vo.setTotalIncomeAmount(sum(list, OwnerSettlementBill::getIncomeAmount));
        vo.setTotalPayableAmount(sum(list, OwnerSettlementBill::getPayableAmount));
        vo.setTotalSettledAmount(sum(list, OwnerSettlementBill::getSettledAmount));
        vo.setTotalUnpaidAmount(list.stream()
            .map(item -> defaultZero(item.getPayableAmount()).subtract(defaultZero(item.getSettledAmount())).max(BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setTotalWithdrawableAmount(sum(list, OwnerSettlementBill::getWithdrawableAmount));
        return vo;
    }

    public OwnerSettlementBillDetailVO detail(OwnerSettlementBillIdDTO dto) {
        if (dto == null || dto.getBillId() == null) {
            throw new IllegalArgumentException("结算单ID不能为空");
        }
        OwnerSettlementBill bill = ownerSettlementBillRepo.getById(dto.getBillId());
        if (bill == null) {
            throw new IllegalArgumentException("轻托管业主结算单不存在");
        }
        Owner owner = ownerRepo.getById(bill.getOwnerId());
        OwnerContract contract = ownerContractRepo.getById(bill.getContractId());
        OwnerSettlementBillDetailVO vo = new OwnerSettlementBillDetailVO();
        vo.setBillId(bill.getId());
        vo.setBillNo(bill.getBillNo());
        vo.setOwnerId(bill.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getOwnerName() : null);
        vo.setOwnerPhone(owner != null ? owner.getOwnerPhone() : null);
        vo.setContractId(bill.getContractId());
        vo.setContractNo(contract != null ? contract.getContractNo() : null);
        vo.setSubjectType(contract == null ? null : com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum.fromCode(bill.getSubjectType()));
        vo.setSubjectId(bill.getSubjectId());
        vo.setSubjectName(bill.getSubjectNameSnapshot());
        vo.setBillStartDate(bill.getBillStartDate());
        vo.setBillEndDate(bill.getBillEndDate());
        vo.setIncomeAmount(bill.getIncomeAmount());
        vo.setReductionAmount(bill.getReductionAmount());
        vo.setExpenseAmount(bill.getExpenseAmount());
        vo.setAdjustAmount(bill.getAdjustAmount());
        vo.setPayableAmount(bill.getPayableAmount());
        vo.setSettledAmount(bill.getSettledAmount());
        vo.setUnpaidAmount(defaultZero(bill.getPayableAmount()).subtract(defaultZero(bill.getSettledAmount())).max(BigDecimal.ZERO));
        vo.setWithdrawnAmount(bill.getWithdrawnAmount());
        vo.setFreezeAmount(bill.getFreezeAmount());
        vo.setWithdrawableAmount(bill.getWithdrawableAmount());
        vo.setBillStatus(bill.getBillStatus());
        vo.setApprovalStatus(bill.getApprovalStatus());
        vo.setSettlementStatus(bill.getSettlementStatus());
        vo.setGeneratedAt(bill.getGeneratedAt());
        vo.setApprovedAt(bill.getApprovedAt());
        vo.setRemark(bill.getRemark());
        vo.setCreateAt(bill.getCreateAt());
        vo.setUpdateAt(bill.getUpdateAt());
        vo.setFeeList(ownerSettlementBillFeeRepo.lambdaQuery()
            .eq(OwnerSettlementBillFee::getBillId, bill.getId())
            .orderByAsc(OwnerSettlementBillFee::getId)
            .list().stream().map(this::toFeeVO).toList());
        vo.setReductionList(ownerSettlementBillReductionRepo.lambdaQuery()
            .eq(OwnerSettlementBillReduction::getBillId, bill.getId())
            .orderByAsc(OwnerSettlementBillReduction::getId)
            .list().stream().map(this::toReductionVO).toList());
        return vo;
    }

    private LambdaQueryWrapper<OwnerSettlementBill> buildWrapper(OwnerSettlementBillQueryDTO query, List<Long> ownerIds) {
        return new LambdaQueryWrapper<OwnerSettlementBill>()
            .eq(query.getOwnerId() != null, OwnerSettlementBill::getOwnerId, query.getOwnerId())
            .eq(query.getContractId() != null, OwnerSettlementBill::getContractId, query.getContractId())
            .like(StrUtil.isNotBlank(query.getBillNo()), OwnerSettlementBill::getBillNo, query.getBillNo())
            .eq(query.getApprovalStatus() != null, OwnerSettlementBill::getApprovalStatus, query.getApprovalStatus())
            .eq(query.getSettlementStatus() != null, OwnerSettlementBill::getSettlementStatus, query.getSettlementStatus())
            .in(ownerIds != null, OwnerSettlementBill::getOwnerId, ownerIds);
    }

    private PageVO<OwnerSettlementBillListVO> emptyPage(OwnerSettlementBillQueryDTO query) {
        return PageVO.<OwnerSettlementBillListVO>builder()
            .currentPage(query.getCurrentPage())
            .pageSize(query.getPageSize())
            .total(0L)
            .pages(0L)
            .list(Collections.emptyList())
            .build();
    }

    private void fillEmptySummary(OwnerSettlementBillSummaryVO vo) {
        vo.setBillCount(0L);
        vo.setTotalIncomeAmount(BigDecimal.ZERO);
        vo.setTotalPayableAmount(BigDecimal.ZERO);
        vo.setTotalSettledAmount(BigDecimal.ZERO);
        vo.setTotalUnpaidAmount(BigDecimal.ZERO);
        vo.setTotalWithdrawableAmount(BigDecimal.ZERO);
    }

    private BigDecimal sum(List<OwnerSettlementBill> list, Function<OwnerSettlementBill, BigDecimal> getter) {
        return list.stream().map(getter).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal defaultZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    /**
     * 生成结算单列表VO
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/4/16 16:51
     *
     * @param item     参数说明
     * @param owner    参数说明
     * @param contract 参数说明
     * @return com.homi.model.owner.vo.OwnerSettlementBillListVO
     */
    private OwnerSettlementBillListVO toListVO(OwnerSettlementBill item, Owner owner, OwnerContract contract) {
        OwnerSettlementBillListVO vo = new OwnerSettlementBillListVO();
        vo.setBillId(item.getId());
        vo.setBillNo(item.getBillNo());
        vo.setOwnerId(item.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getOwnerName() : null);
        vo.setOwnerPhone(owner != null ? owner.getOwnerPhone() : null);
        vo.setContractId(item.getContractId());
        vo.setContractNo(contract != null ? contract.getContractNo() : null);
        vo.setSubjectType(com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum.fromCode(item.getSubjectType()));
        vo.setSubjectName(item.getSubjectNameSnapshot());
        vo.setBillStartDate(item.getBillStartDate());
        vo.setBillEndDate(item.getBillEndDate());
        vo.setIncomeAmount(item.getIncomeAmount());
        vo.setExpenseAmount(item.getExpenseAmount());
        vo.setPayableAmount(item.getPayableAmount());
        vo.setSettledAmount(item.getSettledAmount());
        vo.setUnpaidAmount(defaultZero(item.getPayableAmount()).subtract(defaultZero(item.getSettledAmount())).max(BigDecimal.ZERO));
        vo.setWithdrawableAmount(item.getWithdrawableAmount());
        vo.setApprovalStatus(item.getApprovalStatus());
        vo.setSettlementStatus(item.getSettlementStatus());
        vo.setGeneratedAt(item.getGeneratedAt());
        return vo;
    }

    private OwnerSettlementBillFeeVO toFeeVO(OwnerSettlementBillFee item) {
        OwnerSettlementBillFeeVO vo = new OwnerSettlementBillFeeVO();
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
        vo.setCreateAt(item.getCreateAt());
        return vo;
    }

    private OwnerSettlementBillReductionVO toReductionVO(OwnerSettlementBillReduction item) {
        OwnerSettlementBillReductionVO vo = new OwnerSettlementBillReductionVO();
        vo.setId(item.getId());
        vo.setSourceType(item.getSourceType());
        vo.setSourceId(item.getSourceId());
        vo.setReductionType(item.getReductionType());
        vo.setReductionName(item.getReductionName());
        vo.setAmount(item.getAmount());
        vo.setBizDate(item.getBizDate());
        vo.setRemark(item.getRemark());
        vo.setRuleSnapshot(item.getRuleSnapshot());
        return vo;
    }
}
