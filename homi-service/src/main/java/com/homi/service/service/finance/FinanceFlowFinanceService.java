package com.homi.service.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.FinanceFlowStatusEnum;
import com.homi.common.lib.utils.TimeUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.finance.dto.FinanceFlowFinanceQueryDTO;
import com.homi.model.finance.vo.FinanceFlowFinanceItemVO;
import com.homi.model.finance.vo.FinanceFlowFinanceSummaryVO;
import com.homi.model.room.dto.RoomQueryDTO;
import com.homi.model.room.vo.RoomListVO;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceFlowFinanceService {
    private final FinanceFlowRepo financeFlowRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseRepo leaseRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final PaymentFlowRepo paymentFlowRepo;
    private final TenantRepo tenantRepo;
    private final RoomRepo roomRepo;
    private final RoomService roomService;

    public PageVO<FinanceFlowFinanceItemVO> page(FinanceFlowFinanceQueryDTO query) {
        FilterContext filterContext = resolveFilterContext(query);
        if (filterContext.emptyResult()) {
            return emptyPage(query);
        }
        Page<FinanceFlow> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        IPage<FinanceFlow> financeFlowPage = financeFlowRepo.page(page, buildWrapper(query, filterContext));
        return PageVO.<FinanceFlowFinanceItemVO>builder()
            .currentPage(financeFlowPage.getCurrent())
            .pageSize(financeFlowPage.getSize())
            .total(financeFlowPage.getTotal())
            .pages(financeFlowPage.getPages())
            .list(toItems(financeFlowPage.getRecords()))
            .build();
    }

    public FinanceFlowFinanceItemVO detail(Long id) {
        FinanceFlow financeFlow = financeFlowRepo.getById(id);
        if (financeFlow == null || !Objects.equals(financeFlow.getBizType(), FinanceBizTypeEnum.LEASE_BILL_FEE.getCode())) {
            return null;
        }
        return toItems(List.of(financeFlow)).stream().findFirst().orElse(null);
    }

    public FinanceFlowFinanceSummaryVO summary(FinanceFlowFinanceQueryDTO query) {
        FilterContext filterContext = resolveFilterContext(query);
        FinanceFlowFinanceSummaryVO vo = initSummary();
        if (filterContext.emptyResult()) {
            return vo;
        }
        List<FinanceFlow> financeFlows = financeFlowRepo.list(buildWrapper(query, filterContext));
        Date now = new Date();
        vo.setPendingAmount(sumAmountByStatus(financeFlows, FinanceFlowStatusEnum.PENDING.getCode()));
        vo.setTodayPendingAmount(sumTodayAmountByStatus(financeFlows, FinanceFlowStatusEnum.PENDING.getCode(), now));
        vo.setSuccessAmount(sumAmountByStatus(financeFlows, FinanceFlowStatusEnum.SUCCESS.getCode()));
        vo.setTodaySuccessAmount(sumTodayAmountByStatus(financeFlows, FinanceFlowStatusEnum.SUCCESS.getCode(), now));
        vo.setVoidedAmount(sumAmountByStatus(financeFlows, FinanceFlowStatusEnum.VOIDED.getCode()));
        vo.setTodayVoidedAmount(sumTodayAmountByStatus(financeFlows, FinanceFlowStatusEnum.VOIDED.getCode(), now));
        return vo;
    }

    private FinanceFlowFinanceSummaryVO initSummary() {
        FinanceFlowFinanceSummaryVO vo = new FinanceFlowFinanceSummaryVO();
        vo.setPendingAmount(BigDecimal.ZERO);
        vo.setTodayPendingAmount(BigDecimal.ZERO);
        vo.setSuccessAmount(BigDecimal.ZERO);
        vo.setTodaySuccessAmount(BigDecimal.ZERO);
        vo.setVoidedAmount(BigDecimal.ZERO);
        vo.setTodayVoidedAmount(BigDecimal.ZERO);
        return vo;
    }

    private BigDecimal sumAmountByStatus(List<FinanceFlow> financeFlows, Integer status) {
        return financeFlows.stream()
            .filter(item -> Objects.equals(item.getStatus(), status))
            .map(item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumTodayAmountByStatus(List<FinanceFlow> financeFlows, Integer status, Date today) {
        return financeFlows.stream()
            .filter(item -> Objects.equals(item.getStatus(), status))
            .filter(item -> TimeUtils.isSameDay(item.getFlowAt(), today) || TimeUtils.isSameDay(item.getCreateAt(), today))
            .map(item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private FilterContext resolveFilterContext(FinanceFlowFinanceQueryDTO query) {
        List<Long> billIds = null;
        if (CharSequenceUtil.isNotBlank(query.getRoomKeyword())) {
            RoomQueryDTO roomQueryDTO = new RoomQueryDTO();
            roomQueryDTO.setKeywords(query.getRoomKeyword());
            List<Long> roomIds = roomRepo.pageRoomGridList(roomQueryDTO).getRecords().stream()
                .map(RoomListVO::getRoomId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
            if (CollUtil.isEmpty(roomIds)) {
                return new FilterContext(List.of(), true);
            }
            List<Long> leaseIds = leaseRoomRepo.getListByRoomIds(roomIds).stream()
                .map(LeaseRoom::getLeaseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
            if (CollUtil.isEmpty(leaseIds)) {
                return new FilterContext(List.of(), true);
            }
            LambdaQueryWrapper<LeaseBill> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(LeaseBill::getLeaseId, leaseIds);
            billIds = leaseBillRepo.list(wrapper).stream().map(LeaseBill::getId).distinct().toList();
        }

        List<Long> feeIds = null;
        if (billIds != null || CharSequenceUtil.isNotBlank(query.getFeeType())) {
            LambdaQueryWrapper<LeaseBillFee> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(CollUtil.isNotEmpty(billIds), LeaseBillFee::getBillId, billIds);
            wrapper.eq(CharSequenceUtil.isNotBlank(query.getFeeType()), LeaseBillFee::getFeeType, query.getFeeType());
            feeIds = leaseBillFeeRepo.list(wrapper).stream().map(LeaseBillFee::getId).distinct().toList();
        }

        boolean emptyResult = (billIds != null && billIds.isEmpty()) || (feeIds != null && feeIds.isEmpty());
        return new FilterContext(feeIds, emptyResult);
    }

    private LambdaQueryWrapper<FinanceFlow> buildWrapper(FinanceFlowFinanceQueryDTO query, FilterContext filterContext) {
        LambdaQueryWrapper<FinanceFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceFlow::getBizType, FinanceBizTypeEnum.LEASE_BILL_FEE.getCode());
        wrapper.eq(query.getStatus() != null, FinanceFlow::getStatus, query.getStatus());
        wrapper.eq(CharSequenceUtil.isNotBlank(query.getFlowType()), FinanceFlow::getFlowType, query.getFlowType());
        wrapper.in(CollUtil.isNotEmpty(filterContext.feeIds()), FinanceFlow::getBizId, filterContext.feeIds());
        wrapper.orderByAsc(FinanceFlow::getStatus);
        wrapper.orderByDesc(FinanceFlow::getFlowAt);
        wrapper.orderByDesc(FinanceFlow::getId);
        return wrapper;
    }

    private List<FinanceFlowFinanceItemVO> toItems(List<FinanceFlow> financeFlows) {
        if (CollUtil.isEmpty(financeFlows)) {
            return List.of();
        }
        Map<Long, LeaseBillFee> feeMap = leaseBillFeeRepo.getByIds(financeFlows.stream()
                .map(FinanceFlow::getBizId)
                .filter(Objects::nonNull)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(LeaseBillFee::getId, item -> item));
        Map<Long, LeaseBill> billMap = leaseBillRepo.listByIds(feeMap.values().stream()
                .map(LeaseBillFee::getBillId)
                .filter(Objects::nonNull)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(LeaseBill::getId, item -> item));
        Map<Long, Tenant> tenantMap = tenantRepo.listByIds(billMap.values().stream()
                .map(LeaseBill::getTenantId)
                .filter(Objects::nonNull)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(Tenant::getId, item -> item));
        Map<Long, PaymentFlow> paymentFlowMap = paymentFlowRepo.listByIds(financeFlows.stream()
                .map(FinanceFlow::getPaymentFlowId)
                .filter(Objects::nonNull)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(PaymentFlow::getId, item -> item));
        Map<Long, String> roomAddressMap = buildRoomAddressMap(billMap.values().stream().toList());

        return financeFlows.stream().map(item -> {
            FinanceFlowFinanceItemVO vo = new FinanceFlowFinanceItemVO();
            BeanUtils.copyProperties(item, vo);
            LeaseBillFee fee = feeMap.get(item.getBizId());
            LeaseBill bill = fee == null ? null : billMap.get(fee.getBillId());
            Tenant tenant = bill == null ? null : tenantMap.get(bill.getTenantId());
            PaymentFlow paymentFlow = paymentFlowMap.get(item.getPaymentFlowId());
            if (fee != null) {
                vo.setFeeType(fee.getFeeType());
                vo.setFeeName(fee.getFeeName());
                vo.setBillId(fee.getBillId());
            }
            if (bill != null) {
                vo.setLeaseId(bill.getLeaseId());
                vo.setTenantId(bill.getTenantId());
                vo.setSortOrder(bill.getSortOrder());
                vo.setDueDate(bill.getDueDate());
                vo.setBillStart(bill.getBillStart());
                vo.setBillEnd(bill.getBillEnd());
                vo.setRoomAddress(roomAddressMap.get(bill.getId()));
            }
            if (tenant != null) {
                vo.setTenantName(tenant.getTenantName());
                vo.setTenantPhone(tenant.getTenantPhone());
            }
            if (paymentFlow != null) {
                vo.setPaymentNo(paymentFlow.getPaymentNo());
                vo.setPaymentChannel(paymentFlow.getChannel());
                vo.setPaymentApprovalStatus(paymentFlow.getApprovalStatus());
                vo.setPaymentStatus(paymentFlow.getStatus());
                vo.setThirdTradeNo(paymentFlow.getThirdTradeNo());
                vo.setPaymentVoucherUrl(paymentFlow.getPaymentVoucherUrl());
                vo.setPaymentRemark(paymentFlow.getRemark());
                vo.setPayAt(paymentFlow.getPayAt());
            }
            return vo;
        }).toList();
    }

    private Map<Long, String> buildRoomAddressMap(List<LeaseBill> bills) {
        if (CollUtil.isEmpty(bills)) {
            return Map.of();
        }
        Map<Long, Lease> leaseMap = leaseRepo.listByIds(bills.stream()
                .map(LeaseBill::getLeaseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(Lease::getId, item -> item));
        return bills.stream().collect(Collectors.toMap(
            LeaseBill::getId,
            bill -> resolveRoomAddress(leaseMap.get(bill.getLeaseId())),
            (left, right) -> left
        ));
    }

    private String resolveRoomAddress(Lease lease) {
        if (lease == null) {
            return null;
        }
        List<Long> roomIds = leaseRoomRepo.getListByLeaseId(lease.getId()).stream()
            .map(LeaseRoom::getRoomId)
            .filter(Objects::nonNull)
            .toList();
        if (CollUtil.isNotEmpty(roomIds)) {
            return roomService.getRoomAddressByIds(roomIds);
        }
        if (CharSequenceUtil.isBlank(lease.getRoomIds())) {
            return null;
        }
        List<Long> leaseRoomIds = JSONUtil.toList(lease.getRoomIds(), Long.class);
        return CollUtil.isEmpty(leaseRoomIds) ? null : roomService.getRoomAddressByIds(leaseRoomIds);
    }

    private PageVO<FinanceFlowFinanceItemVO> emptyPage(FinanceFlowFinanceQueryDTO query) {
        return PageVO.<FinanceFlowFinanceItemVO>builder()
            .currentPage(query.getCurrentPage())
            .pageSize(query.getPageSize())
            .total(0L)
            .pages(0L)
            .list(List.of())
            .build();
    }

    private record FilterContext(List<Long> feeIds, boolean emptyResult) {
    }
}
