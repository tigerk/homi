package com.homi.service.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowStatusEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.Lease;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.entity.LeaseRoom;
import com.homi.model.dao.entity.Owner;
import com.homi.model.dao.entity.OwnerPayableBill;
import com.homi.model.dao.entity.OwnerPayableBillFee;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.repo.LeaseBillFeeRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.dao.repo.LeaseRepo;
import com.homi.model.dao.repo.LeaseRoomRepo;
import com.homi.model.dao.repo.OwnerPayableBillRepo;
import com.homi.model.dao.repo.OwnerPayableBillFeeRepo;
import com.homi.model.dao.repo.OwnerRepo;
import com.homi.model.dao.repo.PaymentFlowRepo;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.model.dao.repo.TenantRepo;
import com.homi.model.finance.dto.PaymentFlowFinanceQueryDTO;
import com.homi.model.finance.vo.PaymentFlowFinanceItemVO;
import com.homi.model.finance.vo.PaymentFlowFinanceSummaryVO;
import com.homi.model.tenant.vo.bill.FinanceFlowVO;
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
public class PaymentFlowFinanceService {
    private final PaymentFlowRepo paymentFlowRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseRepo leaseRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final OwnerPayableBillRepo ownerPayableBillRepo;
    private final OwnerPayableBillFeeRepo ownerPayableBillFeeRepo;
    private final OwnerRepo ownerRepo;
    private final TenantRepo tenantRepo;
    private final RoomRepo roomRepo;
    private final RoomService roomService;
    private final FinanceFlowService financeFlowService;

    public PageVO<PaymentFlowFinanceItemVO> page(PaymentFlowFinanceQueryDTO query) {
        FilterContext filterContext = resolveFilterContext(query);
        if (filterContext.emptyResult()) {
            return emptyPage(query);
        }

        Page<PaymentFlow> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        IPage<PaymentFlow> paymentFlowPage = paymentFlowRepo.page(page, buildWrapper(query, filterContext));
        List<PaymentFlowFinanceItemVO> records = toItems(paymentFlowPage.getRecords());
        return PageVO.<PaymentFlowFinanceItemVO>builder()
            .currentPage(paymentFlowPage.getCurrent())
            .pageSize(paymentFlowPage.getSize())
            .total(paymentFlowPage.getTotal())
            .pages(paymentFlowPage.getPages())
            .list(records)
            .build();
    }

    public PaymentFlowFinanceItemVO detail(Long id) {
        PaymentFlow paymentFlow = paymentFlowRepo.getById(id);
        if (paymentFlow == null) {
            return null;
        }
        PaymentFlowFinanceItemVO detail = toItems(List.of(paymentFlow)).stream().findFirst().orElse(null);
        if (detail == null) {
            return null;
        }
        List<FinanceFlow> financeFlows = financeFlowService.getListByPaymentFlowId(id);
        List<Long> feeIds = financeFlows.stream()
            .filter(item -> Objects.equals(item.getBizType(), FinanceBizTypeEnum.LEASE_BILL_FEE.getCode()))
            .map(FinanceFlow::getBizId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, LeaseBillFee> feeMap = feeIds.isEmpty() ? Map.of() : leaseBillFeeRepo.getByIds(feeIds).stream()
            .collect(Collectors.toMap(LeaseBillFee::getId, item -> item, (left, right) -> left));
        List<Long> ownerFeeIds = financeFlows.stream()
            .filter(item -> Objects.equals(item.getBizType(), FinanceBizTypeEnum.OWNER_PAYABLE_BILL_FEE.getCode()))
            .map(FinanceFlow::getBizId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, OwnerPayableBillFee> ownerFeeMap = ownerFeeIds.isEmpty() ? Map.of() : ownerPayableBillFeeRepo.getByIds(ownerFeeIds).stream()
            .collect(Collectors.toMap(OwnerPayableBillFee::getId, item -> item, (left, right) -> left));
        detail.setFinanceFlowList(financeFlows.stream().map(item -> {
            FinanceFlowVO vo = BeanCopyUtils.copyBean(item, FinanceFlowVO.class);
            assert vo != null;
            LeaseBillFee fee = feeMap.get(item.getBizId());
            if (fee != null) {
                vo.setFeeType(fee.getFeeType());
                vo.setFeeName(fee.getFeeName());
            }
            OwnerPayableBillFee ownerFee = ownerFeeMap.get(item.getBizId());
            if (ownerFee != null) {
                vo.setFeeType(ownerFee.getFeeType());
                vo.setFeeName(ownerFee.getFeeName());
            }
            return vo;
        }).toList());
        return detail;
    }

    public PaymentFlowFinanceSummaryVO summary(PaymentFlowFinanceQueryDTO query) {
        FilterContext filterContext = resolveFilterContext(query);
        PaymentFlowFinanceSummaryVO vo = initSummary();
        if (filterContext.emptyResult()) {
            return vo;
        }

        List<PaymentFlow> paymentFlows = paymentFlowRepo.list(buildWrapper(query, filterContext));
        Date today = DateUtil.beginOfDay(new Date());
        vo.setPendingApprovalAmount(sumByStatus(paymentFlows, PaymentFlowStatusEnum.PENDING_APPROVAL.getCode(), false, today));
        vo.setTodayPendingApprovalAmount(sumByStatus(paymentFlows, PaymentFlowStatusEnum.PENDING_APPROVAL.getCode(), true, today));
        vo.setSuccessAmount(sumByStatus(paymentFlows, PaymentFlowStatusEnum.SUCCESS.getCode(), false, today));
        vo.setTodaySuccessAmount(sumByStatus(paymentFlows, PaymentFlowStatusEnum.SUCCESS.getCode(), true, today));
        vo.setClosedAmount(sumByStatus(paymentFlows, PaymentFlowStatusEnum.CLOSED.getCode(), false, today));
        vo.setTodayClosedAmount(sumByStatus(paymentFlows, PaymentFlowStatusEnum.CLOSED.getCode(), true, today));
        return vo;
    }

    private PaymentFlowFinanceSummaryVO initSummary() {
        PaymentFlowFinanceSummaryVO vo = new PaymentFlowFinanceSummaryVO();
        vo.setPendingApprovalAmount(BigDecimal.ZERO);
        vo.setTodayPendingApprovalAmount(BigDecimal.ZERO);
        vo.setSuccessAmount(BigDecimal.ZERO);
        vo.setTodaySuccessAmount(BigDecimal.ZERO);
        vo.setClosedAmount(BigDecimal.ZERO);
        vo.setTodayClosedAmount(BigDecimal.ZERO);
        return vo;
    }

    private BigDecimal sumByStatus(List<PaymentFlow> paymentFlows, Integer status, boolean onlyToday, Date today) {
        return paymentFlows.stream()
            .filter(item -> Objects.equals(item.getStatus(), status))
            .filter(item -> !onlyToday || DateUtil.isSameDay(item.getCreateAt(), today))
            .map(item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private FilterContext resolveFilterContext(PaymentFlowFinanceQueryDTO query) {
        String bizType = query.getBizType();
        boolean searchLeaseBill = CharSequenceUtil.isBlank(bizType)
            || Objects.equals(bizType, PaymentFlowBizTypeEnum.LEASE_BILL.getCode());
        boolean searchOwnerPayableBill = CharSequenceUtil.isBlank(bizType)
            || Objects.equals(bizType, PaymentFlowBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode());
        boolean hasObjectFilter = CharSequenceUtil.isNotBlank(query.getTenantName())
            || CharSequenceUtil.isNotBlank(query.getTenantPhone());
        boolean hasRoomFilter = CharSequenceUtil.isNotBlank(query.getRoomKeyword());

        List<Long> tenantIds = null;
        if (searchLeaseBill && hasObjectFilter) {
            tenantIds = tenantRepo.getTenantList(query.getTenantName(), query.getTenantPhone(), null).stream()
                .map(Tenant::getId)
                .distinct()
                .toList();
        }

        List<Long> ownerIds = null;
        if (searchOwnerPayableBill && hasObjectFilter) {
            LambdaQueryWrapper<Owner> ownerWrapper = new LambdaQueryWrapper<>();
            ownerWrapper.like(CharSequenceUtil.isNotBlank(query.getTenantName()), Owner::getOwnerName, query.getTenantName());
            ownerWrapper.like(CharSequenceUtil.isNotBlank(query.getTenantPhone()), Owner::getOwnerPhone, query.getTenantPhone());
            ownerIds = ownerRepo.list(ownerWrapper).stream()
                .map(Owner::getId)
                .distinct()
                .toList();
        }

        List<Long> leaseIds = null;
        if (searchLeaseBill && hasRoomFilter) {
            RoomQueryDTO roomQueryDTO = new RoomQueryDTO();
            roomQueryDTO.setKeywords(query.getRoomKeyword());
            List<Long> roomIds = roomRepo.pageRoomGridList(roomQueryDTO).getRecords().stream()
                .map(RoomListVO::getRoomId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
            if (CollUtil.isEmpty(roomIds)) {
                leaseIds = List.of();
            } else {
                leaseIds = leaseRoomRepo.getListByRoomIds(roomIds).stream()
                    .map(LeaseRoom::getLeaseId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
                if (CollUtil.isEmpty(leaseIds)) {
                    leaseIds = List.of();
                }
            }
        }

        List<Long> leaseBillIds = null;
        if (searchLeaseBill && (tenantIds != null || leaseIds != null)) {
            LambdaQueryWrapper<LeaseBill> billWrapper = new LambdaQueryWrapper<>();
            if (CollUtil.isNotEmpty(tenantIds)) {
                billWrapper.in(LeaseBill::getTenantId, tenantIds);
            }
            if (CollUtil.isNotEmpty(leaseIds)) {
                billWrapper.in(LeaseBill::getLeaseId, leaseIds);
            }
            leaseBillIds = (CollUtil.isEmpty(tenantIds) && tenantIds != null) || (CollUtil.isEmpty(leaseIds) && leaseIds != null)
                ? List.of()
                : leaseBillRepo.list(billWrapper).stream().map(LeaseBill::getId).distinct().toList();
        }

        List<Long> ownerBillIds = null;
        if (searchOwnerPayableBill && (ownerIds != null || hasRoomFilter)) {
            LambdaQueryWrapper<OwnerPayableBill> ownerBillWrapper = new LambdaQueryWrapper<>();
            if (CollUtil.isNotEmpty(ownerIds)) {
                ownerBillWrapper.in(OwnerPayableBill::getOwnerId, ownerIds);
            }
            if (hasRoomFilter) {
                ownerBillWrapper.and(wrapper -> wrapper
                    .like(OwnerPayableBill::getSubjectNameSnapshot, query.getRoomKeyword())
                    .or()
                    .like(OwnerPayableBill::getBillNo, query.getRoomKeyword()));
            }
            ownerBillIds = CollUtil.isEmpty(ownerIds) && ownerIds != null
                ? List.of()
                : ownerPayableBillRepo.list(ownerBillWrapper).stream().map(OwnerPayableBill::getId).distinct().toList();
        }

        boolean emptyResult = isPaymentFilterEmpty(bizType, hasObjectFilter || hasRoomFilter, leaseBillIds, ownerBillIds);
        return new FilterContext(leaseBillIds, ownerBillIds, emptyResult);
    }

    private LambdaQueryWrapper<PaymentFlow> buildWrapper(PaymentFlowFinanceQueryDTO query, FilterContext filterContext) {
        LambdaQueryWrapper<PaymentFlow> wrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(query.getBizType())) {
            wrapper.eq(PaymentFlow::getBizType, query.getBizType());
            if (Objects.equals(query.getBizType(), PaymentFlowBizTypeEnum.LEASE_BILL.getCode())
                && CollUtil.isNotEmpty(filterContext.leaseBillIds())) {
                wrapper.in(PaymentFlow::getBizId, filterContext.leaseBillIds());
            }
            if (Objects.equals(query.getBizType(), PaymentFlowBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode())
                && CollUtil.isNotEmpty(filterContext.ownerBillIds())) {
                wrapper.in(PaymentFlow::getBizId, filterContext.ownerBillIds());
            }
        } else {
            applyPaymentBizFilter(wrapper, filterContext);
        }
        if (query.getStatus() != null) {
            wrapper.eq(PaymentFlow::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(PaymentFlow::getStatus);
        wrapper.orderByDesc(PaymentFlow::getCreateAt);
        wrapper.orderByDesc(PaymentFlow::getId);
        return wrapper;
    }

    private void applyPaymentBizFilter(LambdaQueryWrapper<PaymentFlow> wrapper, FilterContext filterContext) {
        boolean hasLeaseBillFilter = CollUtil.isNotEmpty(filterContext.leaseBillIds());
        boolean hasOwnerBillFilter = CollUtil.isNotEmpty(filterContext.ownerBillIds());
        if (hasLeaseBillFilter && hasOwnerBillFilter) {
            wrapper.and(item -> item
                .eq(PaymentFlow::getBizType, PaymentFlowBizTypeEnum.LEASE_BILL.getCode())
                .in(PaymentFlow::getBizId, filterContext.leaseBillIds())
                .or()
                .eq(PaymentFlow::getBizType, PaymentFlowBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode())
                .in(PaymentFlow::getBizId, filterContext.ownerBillIds()));
            return;
        }
        if (hasLeaseBillFilter) {
            wrapper.eq(PaymentFlow::getBizType, PaymentFlowBizTypeEnum.LEASE_BILL.getCode());
            wrapper.in(PaymentFlow::getBizId, filterContext.leaseBillIds());
            return;
        }
        if (hasOwnerBillFilter) {
            wrapper.eq(PaymentFlow::getBizType, PaymentFlowBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode());
            wrapper.in(PaymentFlow::getBizId, filterContext.ownerBillIds());
        }
    }

    private boolean isPaymentFilterEmpty(String bizType, boolean hasBizFilter, List<Long> leaseBillIds, List<Long> ownerBillIds) {
        if (!hasBizFilter) {
            return false;
        }
        if (Objects.equals(bizType, PaymentFlowBizTypeEnum.LEASE_BILL.getCode())) {
            return leaseBillIds != null && leaseBillIds.isEmpty();
        }
        if (Objects.equals(bizType, PaymentFlowBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode())) {
            return ownerBillIds != null && ownerBillIds.isEmpty();
        }
        if (CharSequenceUtil.isNotBlank(bizType)) {
            return false;
        }
        boolean leaseEmpty = leaseBillIds == null || leaseBillIds.isEmpty();
        boolean ownerEmpty = ownerBillIds == null || ownerBillIds.isEmpty();
        return leaseEmpty && ownerEmpty;
    }

    private List<PaymentFlowFinanceItemVO> toItems(List<PaymentFlow> paymentFlows) {
        if (paymentFlows == null || paymentFlows.isEmpty()) {
            return List.of();
        }

        List<Long> billIds = paymentFlows.stream()
            .filter(item -> Objects.equals(item.getBizType(), PaymentFlowBizTypeEnum.LEASE_BILL.getCode()))
            .map(PaymentFlow::getBizId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, LeaseBill> billMap = billIds.isEmpty() ? Map.of() : leaseBillRepo.listByIds(billIds).stream()
            .collect(Collectors.toMap(LeaseBill::getId, item -> item, (left, right) -> left));
        List<Long> tenantIds = billMap.values().stream()
            .map(LeaseBill::getTenantId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, Tenant> tenantMap = tenantIds.isEmpty() ? Map.of() : tenantRepo.listByIds(tenantIds).stream()
            .collect(Collectors.toMap(Tenant::getId, item -> item, (left, right) -> left));
        Map<Long, String> roomAddressMap = buildRoomAddressMap(billMap.values().stream().toList());
        List<Long> ownerBillIds = paymentFlows.stream()
            .filter(item -> Objects.equals(item.getBizType(), PaymentFlowBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode()))
            .map(PaymentFlow::getBizId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, OwnerPayableBill> ownerBillMap = ownerBillIds.isEmpty() ? Map.of() : ownerPayableBillRepo.listByIds(ownerBillIds).stream()
            .collect(Collectors.toMap(OwnerPayableBill::getId, item -> item, (left, right) -> left));
        List<Long> ownerIds = ownerBillMap.values().stream()
            .map(OwnerPayableBill::getOwnerId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, Owner> ownerMap = ownerIds.isEmpty() ? Map.of() : ownerRepo.listByIds(ownerIds).stream()
            .collect(Collectors.toMap(Owner::getId, item -> item, (left, right) -> left));

        return paymentFlows.stream().map(item -> {
            PaymentFlowFinanceItemVO vo = new PaymentFlowFinanceItemVO();
            BeanUtils.copyProperties(item, vo);
            vo.setBizType(item.getBizType());
            vo.setBizId(item.getBizId());
            vo.setBizNo(item.getBizNo());
            vo.setFlowDirection(item.getFlowDirection());
            vo.setReceiverName(item.getReceiverName());
            LeaseBill bill = billMap.get(item.getBizId());
            Tenant tenant = bill == null ? null : tenantMap.get(bill.getTenantId());
            if (bill != null) {
                vo.setBillId(bill.getId());
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
            OwnerPayableBill ownerBill = ownerBillMap.get(item.getBizId());
            Owner owner = ownerBill == null ? null : ownerMap.get(ownerBill.getOwnerId());
            if (ownerBill != null) {
                vo.setBillId(ownerBill.getId());
                vo.setOwnerPayableBillId(ownerBill.getId());
                vo.setOwnerPayableBillNo(ownerBill.getBillNo());
                vo.setOwnerPayableBillSubjectName(ownerBill.getSubjectNameSnapshot());
                vo.setDueDate(ownerBill.getDueDate());
                vo.setBillStart(ownerBill.getBillStartDate());
                vo.setBillEnd(ownerBill.getBillEndDate());
                vo.setRoomAddress(ownerBill.getSubjectNameSnapshot());
            }
            if (owner != null) {
                vo.setOwnerId(owner.getId());
                vo.setOwnerName(owner.getOwnerName());
                vo.setOwnerPhone(owner.getOwnerPhone());
            }
            return vo;
        }).toList();
    }

    private Map<Long, String> buildRoomAddressMap(List<LeaseBill> bills) {
        if (bills.isEmpty()) {
            return Map.of();
        }
        List<Long> leaseIds = bills.stream()
            .map(LeaseBill::getLeaseId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, Lease> leaseMap = leaseIds.isEmpty() ? Map.of() : leaseRepo.listByIds(leaseIds).stream()
            .collect(Collectors.toMap(Lease::getId, item -> item, (left, right) -> left));

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
        if (!roomIds.isEmpty()) {
            return roomService.getRoomAddressByIds(roomIds);
        }
        if (CharSequenceUtil.isBlank(lease.getRoomIds())) {
            return null;
        }
        List<Long> leaseRoomIds = JSONUtil.toList(lease.getRoomIds(), Long.class);
        if (leaseRoomIds.isEmpty()) {
            return null;
        }
        return roomService.getRoomAddressByIds(leaseRoomIds);
    }

    private PageVO<PaymentFlowFinanceItemVO> emptyPage(PaymentFlowFinanceQueryDTO query) {
        return PageVO.<PaymentFlowFinanceItemVO>builder()
            .currentPage(query.getCurrentPage())
            .pageSize(query.getPageSize())
            .total(0L)
            .pages(0L)
            .list(List.of())
            .build();
    }

    private record FilterContext(List<Long> leaseBillIds, List<Long> ownerBillIds, boolean emptyResult) {
    }
}
