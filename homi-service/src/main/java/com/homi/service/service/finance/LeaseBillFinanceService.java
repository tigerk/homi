package com.homi.service.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.lease.LeaseBillFeeTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.enums.pay.PayStatusEnum;
import com.homi.common.lib.utils.TimeUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.entity.LeaseRoom;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.repo.LeaseBillFeeRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.dao.repo.LeaseRoomRepo;
import com.homi.model.dao.repo.PaymentFlowRepo;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.model.dao.repo.TenantRepo;
import com.homi.model.finance.dto.LeaseBillFinanceQueryDTO;
import com.homi.model.finance.vo.LeaseBillFeeFinanceItemVO;
import com.homi.model.finance.vo.LeaseBillFinanceItemVO;
import com.homi.model.finance.vo.LeaseBillFinanceSummaryVO;
import com.homi.model.room.dto.RoomQueryDTO;
import com.homi.model.room.vo.RoomListVO;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaseBillFinanceService {
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;
    private final TenantRepo tenantRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final RoomRepo roomRepo;
    private final RoomService roomService;
    private final PaymentFlowRepo paymentFlowRepo;

    public PageVO<LeaseBillFinanceItemVO> pageBills(LeaseBillFinanceQueryDTO query) {
        FilterContext filterContext = resolveFilterContext(query);
        if (filterContext.emptyResult()) {
            return emptyPage(query);
        }

        Page<LeaseBill> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        IPage<LeaseBill> billPage = leaseBillRepo.page(page, buildBillWrapper(query, filterContext));
        List<LeaseBillFinanceItemVO> records = toBillFinanceItems(billPage.getRecords());
        return PageVO.<LeaseBillFinanceItemVO>builder()
            .currentPage(billPage.getCurrent())
            .pageSize(billPage.getSize())
            .total(billPage.getTotal())
            .pages(billPage.getPages())
            .list(records)
            .build();
    }

    public PageVO<LeaseBillFeeFinanceItemVO> pageBillFees(LeaseBillFinanceQueryDTO query) {
        FilterContext filterContext = resolveFilterContext(query);
        if (filterContext.emptyResult()) {
            return emptyFeePage(query);
        }

        List<Long> allBillIds = listFilteredBillIds(query, filterContext);
        if (allBillIds.isEmpty()) {
            return emptyFeePage(query);
        }

        Page<LeaseBillFee> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        LambdaQueryWrapper<LeaseBillFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(LeaseBillFee::getBillId, allBillIds);
        wrapper.orderByDesc(LeaseBillFee::getCreateTime);
        wrapper.orderByDesc(LeaseBillFee::getId);
        IPage<LeaseBillFee> feePage = leaseBillFeeRepo.page(page, wrapper);

        Map<Long, LeaseBill> billMap = leaseBillRepo.listByIds(feePage.getRecords().stream().map(LeaseBillFee::getBillId).distinct().toList()).stream()
            .collect(Collectors.toMap(LeaseBill::getId, item -> item));
        List<LeaseBillFinanceItemVO> billContexts = toBillFinanceItems(new ArrayList<>(billMap.values()));
        Map<Long, LeaseBillFinanceItemVO> billContextMap = billContexts.stream()
            .collect(Collectors.toMap(LeaseBillFinanceItemVO::getId, item -> item));

        List<LeaseBillFeeFinanceItemVO> records = feePage.getRecords().stream().map(fee -> {
            LeaseBill bill = billMap.get(fee.getBillId());
            LeaseBillFinanceItemVO billContext = billContextMap.get(fee.getBillId());
            LeaseBillFeeFinanceItemVO item = new LeaseBillFeeFinanceItemVO();
            item.setId(fee.getId());
            item.setBillId(fee.getBillId());
            item.setLeaseId(bill != null ? bill.getLeaseId() : null);
            item.setTenantId(bill != null ? bill.getTenantId() : null);
            item.setSortOrder(bill != null ? bill.getSortOrder() : null);
            item.setTenantName(billContext != null ? billContext.getTenantName() : null);
            item.setTenantPhone(billContext != null ? billContext.getTenantPhone() : null);
            item.setRoomAddress(billContext != null ? billContext.getRoomAddress() : null);
            item.setFeeType(fee.getFeeType());
            item.setFeeName(fee.getFeeName());
            item.setAmount(ObjectUtil.defaultIfNull(fee.getAmount(), BigDecimal.ZERO));
            item.setPaidAmount(ObjectUtil.defaultIfNull(fee.getPaidAmount(), BigDecimal.ZERO));
            item.setUnpaidAmount(ObjectUtil.defaultIfNull(fee.getUnpaidAmount(), BigDecimal.ZERO));
            item.setPayStatus(fee.getPayStatus());
            item.setOverdue(isOverdueBill(bill));
            item.setFeeStart(fee.getFeeStart());
            item.setFeeEnd(fee.getFeeEnd());
            item.setDueDate(bill != null ? bill.getDueDate() : null);
            item.setRemark(fee.getRemark());
            return item;
        }).toList();

        return PageVO.<LeaseBillFeeFinanceItemVO>builder()
            .currentPage(feePage.getCurrent())
            .pageSize(feePage.getSize())
            .total(feePage.getTotal())
            .pages(feePage.getPages())
            .list(records)
            .build();
    }

    public LeaseBillFinanceSummaryVO summary(LeaseBillFinanceQueryDTO query) {
        FilterContext filterContext = resolveFilterContext(query);
        LeaseBillFinanceSummaryVO vo = new LeaseBillFinanceSummaryVO();
        vo.setReceivableAmount(BigDecimal.ZERO);
        vo.setTodayReceivableAmount(BigDecimal.ZERO);
        vo.setPaidAmount(BigDecimal.ZERO);
        vo.setTodayPaidAmount(BigDecimal.ZERO);
        vo.setCategoryStats(List.of());
        if (filterContext.emptyResult()) {
            return vo;
        }

        List<LeaseBill> bills = leaseBillRepo.list(buildBillWrapper(query, filterContext));
        if (bills.isEmpty()) {
            return vo;
        }

        Date today = DateUtil.beginOfDay(new Date());
        List<Long> billIds = bills.stream().map(LeaseBill::getId).toList();
        List<LeaseBillFee> feeList = leaseBillFeeRepo.getFeesByBillIds(billIds);
        List<PaymentFlow> paymentFlows = paymentFlowRepo.listByBizIds(PaymentFlowBizTypeEnum.LEASE_BILL.getCode(), billIds);

        vo.setReceivableAmount(sumBills(bills, LeaseBill::getTotalAmount));
        vo.setTodayReceivableAmount(bills.stream()
            .filter(item -> TimeUtils.isSameDay(item.getDueDate(), today))
            .map(item -> ObjectUtil.defaultIfNull(item.getTotalAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setPaidAmount(sumBills(bills, LeaseBill::getPaidAmount));
        vo.setTodayPaidAmount(paymentFlows.stream()
            .filter(item -> TimeUtils.isSameDay(item.getPayTime(), today))
            .map(item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        Map<String, LeaseBillFinanceSummaryVO.CategoryStatVO> categoryMap = new LinkedHashMap<>();
        initCategory(categoryMap, "RENTAL", "租金");
        initCategory(categoryMap, "DEPOSIT", "押金");
        initCategory(categoryMap, "OTHER_FEE", "其他费用");
        for (LeaseBillFee fee : feeList) {
            LeaseBillFinanceSummaryVO.CategoryStatVO category = categoryMap.computeIfAbsent(fee.getFeeType(), key -> {
                LeaseBillFinanceSummaryVO.CategoryStatVO item = new LeaseBillFinanceSummaryVO.CategoryStatVO();
                item.setFeeType(key);
                item.setFeeTypeLabel(LeaseBillFeeTypeEnum.getLabelByCode(key));
                item.setReceivableAmount(BigDecimal.ZERO);
                item.setPaidAmount(BigDecimal.ZERO);
                item.setUnpaidAmount(BigDecimal.ZERO);
                return item;
            });
            category.setReceivableAmount(category.getReceivableAmount().add(ObjectUtil.defaultIfNull(fee.getAmount(), BigDecimal.ZERO)));
            category.setPaidAmount(category.getPaidAmount().add(ObjectUtil.defaultIfNull(fee.getPaidAmount(), BigDecimal.ZERO)));
            category.setUnpaidAmount(category.getUnpaidAmount().add(ObjectUtil.defaultIfNull(fee.getUnpaidAmount(), BigDecimal.ZERO)));
        }
        vo.setCategoryStats(new ArrayList<>(categoryMap.values()));
        return vo;
    }

    private void initCategory(Map<String, LeaseBillFinanceSummaryVO.CategoryStatVO> categoryMap, String feeType, String label) {
        LeaseBillFinanceSummaryVO.CategoryStatVO item = new LeaseBillFinanceSummaryVO.CategoryStatVO();
        item.setFeeType(feeType);
        item.setFeeTypeLabel(label);
        item.setReceivableAmount(BigDecimal.ZERO);
        item.setPaidAmount(BigDecimal.ZERO);
        item.setUnpaidAmount(BigDecimal.ZERO);
        categoryMap.put(feeType, item);
    }

    private FilterContext resolveFilterContext(LeaseBillFinanceQueryDTO query) {
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

        boolean emptyResult = (tenantIds != null && tenantIds.isEmpty()) || (leaseIds != null && leaseIds.isEmpty());
        return new FilterContext(tenantIds, leaseIds, emptyResult);
    }

    private LambdaQueryWrapper<LeaseBill> buildBillWrapper(LeaseBillFinanceQueryDTO query, FilterContext filterContext) {
        LambdaQueryWrapper<LeaseBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeaseBill::getValid, true);
        if (query.getPayStatus() != null) {
            wrapper.eq(LeaseBill::getPayStatus, query.getPayStatus());
        }
        if (Boolean.TRUE.equals(query.getOverdueOnly())) {
            wrapper.ne(LeaseBill::getPayStatus, PayStatusEnum.PAID.getCode());
            wrapper.lt(LeaseBill::getDueDate, DateUtil.beginOfDay(new Date()));
        }
        if (CollUtil.isNotEmpty(filterContext.tenantIds())) {
            wrapper.in(LeaseBill::getTenantId, filterContext.tenantIds());
        }
        if (CollUtil.isNotEmpty(filterContext.leaseIds())) {
            wrapper.in(LeaseBill::getLeaseId, filterContext.leaseIds());
        }
        wrapper.orderByAsc(LeaseBill::getPayStatus);
        wrapper.orderByAsc(LeaseBill::getDueDate);
        wrapper.orderByDesc(LeaseBill::getId);
        return wrapper;
    }

    private List<Long> listFilteredBillIds(LeaseBillFinanceQueryDTO query, FilterContext filterContext) {
        return leaseBillRepo.list(buildBillWrapper(query, filterContext)).stream()
            .map(LeaseBill::getId)
            .toList();
    }

    private List<LeaseBillFinanceItemVO> toBillFinanceItems(List<LeaseBill> bills) {
        if (bills == null || bills.isEmpty()) {
            return List.of();
        }
        Map<Long, Tenant> tenantMap = tenantRepo.listByIds(bills.stream().map(LeaseBill::getTenantId).filter(Objects::nonNull).distinct().toList()).stream()
            .collect(Collectors.toMap(Tenant::getId, item -> item));
        Map<Long, String> roomAddressMap = resolveRoomAddressMap(bills.stream()
            .map(LeaseBill::getLeaseId)
            .filter(Objects::nonNull)
            .distinct()
            .toList());

        return bills.stream().map(bill -> {
            Tenant tenant = tenantMap.get(bill.getTenantId());
            LeaseBillFinanceItemVO item = new LeaseBillFinanceItemVO();
            item.setId(bill.getId());
            item.setTenantId(bill.getTenantId());
            item.setLeaseId(bill.getLeaseId());
            item.setSortOrder(bill.getSortOrder());
            item.setBillType(bill.getBillType());
            item.setTenantName(tenant != null ? tenant.getTenantName() : null);
            item.setTenantPhone(tenant != null ? tenant.getTenantPhone() : null);
            item.setRoomAddress(roomAddressMap.get(bill.getLeaseId()));
            item.setTotalAmount(ObjectUtil.defaultIfNull(bill.getTotalAmount(), BigDecimal.ZERO));
            item.setPaidAmount(ObjectUtil.defaultIfNull(bill.getPaidAmount(), BigDecimal.ZERO));
            item.setUnpaidAmount(ObjectUtil.defaultIfNull(bill.getUnpaidAmount(), BigDecimal.ZERO));
            item.setPayStatus(bill.getPayStatus());
            item.setOverdue(isOverdueBill(bill));
            item.setBillStart(bill.getBillStart());
            item.setBillEnd(bill.getBillEnd());
            item.setDueDate(bill.getDueDate());
            item.setRemark(bill.getRemark());
            item.setCreateTime(bill.getCreateTime());
            return item;
        }).toList();
    }

    private Map<Long, String> resolveRoomAddressMap(List<Long> leaseIds) {
        if (CollUtil.isEmpty(leaseIds)) {
            return Map.of();
        }

        Map<Long, List<Long>> leaseRoomMap = leaseRoomRepo.getListByLeaseIds(leaseIds).stream()
            .filter(item -> item.getLeaseId() != null && item.getRoomId() != null)
            .collect(Collectors.groupingBy(LeaseRoom::getLeaseId,
                Collectors.mapping(LeaseRoom::getRoomId, Collectors.toList())));

        Map<Long, String> roomAddressMap = new LinkedHashMap<>();
        for (Long leaseId : leaseIds) {
            List<Long> roomIds = leaseRoomMap.get(leaseId);
            if (CollUtil.isEmpty(roomIds)) {
                roomAddressMap.put(leaseId, null);
                continue;
            }
            roomAddressMap.put(leaseId, roomService.getRoomAddressByIds(roomIds));
        }
        return roomAddressMap;
    }

    private boolean isOverdueBill(LeaseBill bill) {
        return bill != null
            && !Objects.equals(bill.getPayStatus(), PayStatusEnum.PAID.getCode())
            && bill.getDueDate() != null
            && DateUtil.beginOfDay(bill.getDueDate()).before(DateUtil.beginOfDay(new Date()));
    }

    private BigDecimal sumBills(List<LeaseBill> bills, java.util.function.Function<LeaseBill, BigDecimal> getter) {
        return bills.stream()
            .map(getter)
            .map(item -> ObjectUtil.defaultIfNull(item, BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PageVO<LeaseBillFinanceItemVO> emptyPage(LeaseBillFinanceQueryDTO query) {
        return PageVO.<LeaseBillFinanceItemVO>builder()
            .currentPage(query.getCurrentPage())
            .pageSize(query.getPageSize())
            .total(0L)
            .pages(0L)
            .list(List.of())
            .build();
    }

    private PageVO<LeaseBillFeeFinanceItemVO> emptyFeePage(LeaseBillFinanceQueryDTO query) {
        return PageVO.<LeaseBillFeeFinanceItemVO>builder()
            .currentPage(query.getCurrentPage())
            .pageSize(query.getPageSize())
            .total(0L)
            .pages(0L)
            .list(List.of())
            .build();
    }

    private record FilterContext(List<Long> tenantIds, List<Long> leaseIds, boolean emptyResult) {
    }
}
