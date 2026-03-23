package com.homi.service.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowStatusEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.Lease;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseRoom;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.dao.repo.LeaseRepo;
import com.homi.model.dao.repo.LeaseRoomRepo;
import com.homi.model.dao.repo.PaymentFlowRepo;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.model.dao.repo.TenantRepo;
import com.homi.model.finance.dto.PaymentFlowFinanceQueryDTO;
import com.homi.model.finance.vo.PaymentFlowFinanceItemVO;
import com.homi.model.finance.vo.PaymentFlowFinanceSummaryVO;
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
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseRepo leaseRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final TenantRepo tenantRepo;
    private final RoomRepo roomRepo;
    private final RoomService roomService;

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
        if (id == null) {
            return null;
        }
        PaymentFlow paymentFlow = paymentFlowRepo.getById(id);
        if (paymentFlow == null || !Objects.equals(paymentFlow.getBizType(), PaymentFlowBizTypeEnum.LEASE_BILL.getCode())) {
            return null;
        }
        return toItems(List.of(paymentFlow)).stream().findFirst().orElse(null);
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
            .filter(item -> !onlyToday || DateUtil.isSameDay(item.getCreateTime(), today))
            .map(item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private FilterContext resolveFilterContext(PaymentFlowFinanceQueryDTO query) {
        List<Long> tenantIds = null;
        if (StrUtil.isNotBlank(query.getTenantName()) || StrUtil.isNotBlank(query.getTenantPhone())) {
            tenantIds = tenantRepo.getTenantList(query.getTenantName(), query.getTenantPhone(), null).stream()
                .map(Tenant::getId)
                .distinct()
                .toList();
        }

        List<Long> leaseIds = null;
        if (StrUtil.isNotBlank(query.getRoomKeyword())) {
            RoomQueryDTO roomQueryDTO = new RoomQueryDTO();
            roomQueryDTO.setKeywords(query.getRoomKeyword());
            List<Long> roomIds = roomRepo.pageRoomGridList(roomQueryDTO).getRecords().stream()
                .map(RoomListVO::getRoomId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
            leaseIds = leaseRoomRepo.getListByRoomIds(roomIds).stream()
                .map(LeaseRoom::getLeaseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        }

        List<Long> billIds = null;
        if (tenantIds != null || leaseIds != null) {
            LambdaQueryWrapper<LeaseBill> billWrapper = new LambdaQueryWrapper<>();
            if (CollUtil.isNotEmpty(tenantIds)) {
                billWrapper.in(LeaseBill::getTenantId, tenantIds);
            }
            if (CollUtil.isNotEmpty(leaseIds)) {
                billWrapper.in(LeaseBill::getLeaseId, leaseIds);
            }
            billIds = leaseBillRepo.list(billWrapper).stream().map(LeaseBill::getId).distinct().toList();
        }

        boolean emptyResult = (tenantIds != null && tenantIds.isEmpty())
            || (leaseIds != null && leaseIds.isEmpty())
            || (billIds != null && billIds.isEmpty());
        return new FilterContext(billIds, emptyResult);
    }

    private LambdaQueryWrapper<PaymentFlow> buildWrapper(PaymentFlowFinanceQueryDTO query, FilterContext filterContext) {
        LambdaQueryWrapper<PaymentFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentFlow::getBizType, PaymentFlowBizTypeEnum.LEASE_BILL.getCode());
        if (query.getStatus() != null) {
            wrapper.eq(PaymentFlow::getStatus, query.getStatus());
        }
        if (CollUtil.isNotEmpty(filterContext.billIds())) {
            wrapper.in(PaymentFlow::getBizId, filterContext.billIds());
        }
        wrapper.orderByAsc(PaymentFlow::getStatus);
        wrapper.orderByDesc(PaymentFlow::getCreateTime);
        wrapper.orderByDesc(PaymentFlow::getId);
        return wrapper;
    }

    private List<PaymentFlowFinanceItemVO> toItems(List<PaymentFlow> paymentFlows) {
        if (paymentFlows == null || paymentFlows.isEmpty()) {
            return List.of();
        }

        List<Long> billIds = paymentFlows.stream().map(PaymentFlow::getBizId).filter(Objects::nonNull).distinct().toList();
        Map<Long, LeaseBill> billMap = leaseBillRepo.listByIds(billIds).stream()
            .collect(Collectors.toMap(LeaseBill::getId, item -> item));
        Map<Long, Tenant> tenantMap = tenantRepo.listByIds(billMap.values().stream()
                .map(LeaseBill::getTenantId)
                .filter(Objects::nonNull)
                .distinct()
                .toList()).stream()
            .collect(Collectors.toMap(Tenant::getId, item -> item));
        Map<Long, String> roomAddressMap = buildRoomAddressMap(billMap.values().stream().toList());

        return paymentFlows.stream().map(item -> {
            PaymentFlowFinanceItemVO vo = new PaymentFlowFinanceItemVO();
            BeanUtils.copyProperties(item, vo);
            LeaseBill bill = billMap.get(item.getBizId());
            Tenant tenant = bill == null ? null : tenantMap.get(bill.getTenantId());
            vo.setBillId(item.getBizId());
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
            return vo;
        }).toList();
    }

    private Map<Long, String> buildRoomAddressMap(List<LeaseBill> bills) {
        if (bills.isEmpty()) {
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
        if (!roomIds.isEmpty()) {
            return roomService.getRoomAddressByIds(roomIds);
        }
        if (StrUtil.isBlank(lease.getRoomIds())) {
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

    private record FilterContext(List<Long> billIds, boolean emptyResult) {
    }
}
