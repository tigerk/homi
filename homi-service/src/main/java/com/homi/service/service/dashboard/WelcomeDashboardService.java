package com.homi.service.service.dashboard;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.enums.booking.BookingStatusEnum;
import com.homi.common.lib.enums.finance.FinanceFlowStatusEnum;
import com.homi.common.lib.enums.finance.PaymentFlowStatusEnum;
import com.homi.common.lib.enums.lease.LeaseStatusEnum;
import com.homi.common.lib.enums.room.OccupancyStatusEnum;
import com.homi.model.dao.entity.Booking;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.Lease;
import com.homi.model.dao.entity.LeaseRoom;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.entity.SysNotice;
import com.homi.model.dao.repo.BookingRepo;
import com.homi.model.dao.repo.FinanceFlowRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.dao.repo.LeaseRepo;
import com.homi.model.dao.repo.LeaseRoomRepo;
import com.homi.model.dao.repo.PaymentFlowRepo;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.model.dao.repo.SysNoticeRepo;
import com.homi.model.dashboard.vo.WelcomeDashboardVO;
import com.homi.model.dashboard.vo.WelcomeCountBucketVO;
import com.homi.model.dashboard.vo.WelcomeContractWarningVO;
import com.homi.model.dashboard.vo.WelcomeNoticeVO;
import com.homi.model.dashboard.vo.WelcomeOverdueBucketVO;
import com.homi.model.dashboard.vo.WelcomeOverdueTenantVO;
import com.homi.model.dashboard.vo.WelcomePeriodAmountVO;
import com.homi.model.dashboard.vo.WelcomeRoomOverviewVO;
import com.homi.model.dashboard.vo.WelcomeTenantStatsVO;
import com.homi.model.room.dto.RoomQueryDTO;
import com.homi.model.room.vo.RoomListVO;
import com.homi.model.room.vo.RoomTotalVO;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WelcomeDashboardService {
    private static final int RECENT_NOTICE_LIMIT = 5;
    private static final int ROOM_MODE_FOCUS = 1;
    private static final int ROOM_MODE_SCATTER = 2;

    private final FinanceFlowRepo financeFlowRepo;
    private final PaymentFlowRepo paymentFlowRepo;
    private final SysNoticeRepo sysNoticeRepo;
    private final RoomService roomService;
    private final RoomRepo roomRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final LeaseRepo leaseRepo;
    private final LeaseBillRepo leaseBillRepo;
    private final BookingRepo bookingRepo;

    /**
     * 聚合欢迎页核心业务统计，避免首页发起多次分散请求。
     */
    public WelcomeDashboardVO getSummary(Long companyId, List<Long> roleIds) {
        WelcomeDashboardVO summary = new WelcomeDashboardVO();
        summary.setFinanceSummary(buildFinanceSummary());
        summary.setPaymentSummary(buildPaymentSummary());
        summary.setNotices(buildNoticeList(companyId, roleIds));
        summary.setRoomOverviewList(List.of(
            buildRoomOverview(ROOM_MODE_SCATTER, "整/合租"),
            buildRoomOverview(ROOM_MODE_FOCUS, "集中式")
        ));
        summary.setOverdueBuckets(buildOverdueBuckets());
        summary.setVacancyBuckets(buildVacancyBuckets(null));
        summary.setContractWarning(buildContractWarning());
        summary.setOverdueTenantTopList(buildOverdueTenantTopList());
        summary.setTenantStats(buildTenantStats());
        return summary;
    }

    private WelcomePeriodAmountVO buildFinanceSummary() {
        List<FinanceFlow> financeFlows = financeFlowRepo.list(new LambdaQueryWrapper<FinanceFlow>()
            .eq(FinanceFlow::getStatus, FinanceFlowStatusEnum.SUCCESS.getCode()));
        return buildPeriodAmount(financeFlows, item -> ObjectUtil.defaultIfNull(item.getFlowTime(), item.getCreateTime()), FinanceFlow::getAmount);
    }

    private WelcomePeriodAmountVO buildPaymentSummary() {
        List<PaymentFlow> paymentFlows = paymentFlowRepo.list(new LambdaQueryWrapper<PaymentFlow>()
            .eq(PaymentFlow::getStatus, PaymentFlowStatusEnum.SUCCESS.getCode()));
        return buildPeriodAmount(paymentFlows, item -> ObjectUtil.defaultIfNull(item.getPayTime(), item.getCreateTime()), PaymentFlow::getAmount);
    }

    private <T> WelcomePeriodAmountVO buildPeriodAmount(List<T> source, Function<T, Date> timeGetter, Function<T, BigDecimal> amountGetter) {
        Date now = new Date();
        Date todayStart = DateUtil.beginOfDay(now);
        Date todayEnd = DateUtil.endOfDay(now);
        Date yesterdayStart = DateUtil.beginOfDay(DateUtil.offsetDay(now, -1));
        Date yesterdayEnd = DateUtil.endOfDay(DateUtil.offsetDay(now, -1));
        Date thisMonthStart = DateUtil.beginOfMonth(now);
        Date thisMonthEnd = DateUtil.endOfMonth(now);
        Date lastMonthStart = DateUtil.beginOfMonth(DateUtil.offsetMonth(now, -1));
        Date lastMonthEnd = DateUtil.endOfMonth(DateUtil.offsetMonth(now, -1));
        Date thisYearStart = DateUtil.beginOfYear(now);
        Date thisYearEnd = DateUtil.endOfYear(now);

        WelcomePeriodAmountVO summary = new WelcomePeriodAmountVO();
        summary.setTodayAmount(sumAmount(source, timeGetter, amountGetter, todayStart, todayEnd));
        summary.setYesterdayAmount(sumAmount(source, timeGetter, amountGetter, yesterdayStart, yesterdayEnd));
        summary.setThisMonthAmount(sumAmount(source, timeGetter, amountGetter, thisMonthStart, thisMonthEnd));
        summary.setLastMonthAmount(sumAmount(source, timeGetter, amountGetter, lastMonthStart, lastMonthEnd));
        summary.setThisYearAmount(sumAmount(source, timeGetter, amountGetter, thisYearStart, thisYearEnd));
        summary.setTotalAmount(sumAmount(source, timeGetter, amountGetter, null, null));
        return summary;
    }

    private <T> BigDecimal sumAmount(List<T> source, Function<T, Date> timeGetter, Function<T, BigDecimal> amountGetter, Date start, Date end) {
        if (CollUtil.isEmpty(source)) {
            return BigDecimal.ZERO;
        }
        return source.stream()
            .filter(item -> isInRange(timeGetter.apply(item), start, end))
            .map(item -> ObjectUtil.defaultIfNull(amountGetter.apply(item), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isInRange(Date value, Date start, Date end) {
        if (value == null) {
            return false;
        }
        if (start != null && value.before(start)) {
            return false;
        }
        return end == null || !value.after(end);
    }

    private List<WelcomeNoticeVO> buildNoticeList(Long companyId, List<Long> roleIds) {
        return sysNoticeRepo.getRecentNotices(companyId, RECENT_NOTICE_LIMIT, roleIds).stream()
            .map(this::toNoticeVo)
            .toList();
    }

    private WelcomeNoticeVO toNoticeVo(SysNotice notice) {
        WelcomeNoticeVO vo = new WelcomeNoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setNoticeType(notice.getNoticeType());
        vo.setPublishTime(notice.getPublishTime());
        vo.setCreateByName(notice.getCreateByName());
        return vo;
    }

    private WelcomeRoomOverviewVO buildRoomOverview(Integer leaseMode, String leaseModeName) {
        RoomQueryDTO roomQuery = new RoomQueryDTO();
        roomQuery.setLeaseMode(leaseMode);
        RoomTotalVO roomTotal = roomService.getRoomStatusTotal(roomQuery);
        Map<Integer, Integer> statusMap = roomTotal.getStatusList() == null ? Map.of() : roomTotal.getStatusList().stream()
            .filter(item -> item.getRoomStatus() != null)
            .collect(Collectors.toMap(item -> item.getRoomStatus(), item -> ObjectUtil.defaultIfNull(item.getTotal(), 0), (left, right) -> right));

        WelcomeRoomOverviewVO overview = new WelcomeRoomOverviewVO();
        overview.setLeaseMode(leaseMode);
        overview.setLeaseModeName(leaseModeName);
        overview.setTotal(ObjectUtil.defaultIfNull(roomTotal.getTotal(), 0));
        overview.setAvailableCount(statusMap.getOrDefault(OccupancyStatusEnum.AVAILABLE.getCode(), 0));
        overview.setPreparingCount(statusMap.getOrDefault(OccupancyStatusEnum.PREPARING.getCode(), 0));
        overview.setLeasedCount(statusMap.getOrDefault(OccupancyStatusEnum.LEASED.getCode(), 0));
        overview.setOccupancyRate(calculateOccupancyRate(overview.getLeasedCount(), overview.getTotal()));

        List<Long> roomIds = roomRepo.pageRoomGridList(roomQuery).getRecords().stream()
            .map(RoomListVO::getRoomId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(roomIds)) {
            overview.setUpcomingCheckInCount(0);
            overview.setUpcomingCheckOutCount(0);
            overview.setOverdueCheckOutCount(0);
            return overview;
        }

        List<Long> leaseIds = leaseRoomRepo.getListByRoomIds(roomIds).stream()
            .map(LeaseRoom::getLeaseId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(leaseIds)) {
            overview.setUpcomingCheckInCount(0);
            overview.setUpcomingCheckOutCount(0);
            overview.setOverdueCheckOutCount(0);
            return overview;
        }

        List<Lease> leases = leaseRepo.listByIds(leaseIds);
        Date today = DateUtil.beginOfDay(new Date());
        Date nextThirtyDay = DateUtil.endOfDay(DateUtil.offsetDay(today, 30));
        overview.setUpcomingCheckInCount((int) leases.stream()
            .filter(item -> LeaseStatusEnum.getValidStatus().contains(item.getStatus()))
            .filter(item -> isInRange(ObjectUtil.defaultIfNull(item.getCheckInTime(), item.getLeaseStart()), today, nextThirtyDay))
            .count());
        overview.setUpcomingCheckOutCount((int) leases.stream()
            .filter(item -> Objects.equals(item.getStatus(), LeaseStatusEnum.EFFECTIVE.getCode()))
            .filter(item -> isInRange(item.getLeaseEnd(), today, nextThirtyDay))
            .count());
        overview.setOverdueCheckOutCount((int) leases.stream()
            .filter(item -> Objects.equals(item.getStatus(), LeaseStatusEnum.EFFECTIVE.getCode()))
            .filter(item -> item.getLeaseEnd() != null && item.getLeaseEnd().before(today))
            .count());
        return overview;
    }

    private BigDecimal calculateOccupancyRate(Integer leasedCount, Integer total) {
        if (ObjectUtil.defaultIfNull(total, 0) <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(ObjectUtil.defaultIfNull(leasedCount, 0))
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private List<WelcomeOverdueBucketVO> buildOverdueBuckets() {
        Map<String, BigDecimal> bucketAmountMap = leaseBillRepo.getWelcomeOverdueBuckets().stream()
            .collect(Collectors.toMap(WelcomeOverdueBucketVO::getKey, item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO), BigDecimal::add));

        return List.of(
            buildOverdueBucket("today_due", "今天到期", bucketAmountMap),
            buildOverdueBucket("overdue_1_3", "1-3 天", bucketAmountMap),
            buildOverdueBucket("overdue_4_7", "4-7 天", bucketAmountMap),
            buildOverdueBucket("overdue_8_14", "8-14 天", bucketAmountMap),
            buildOverdueBucket("overdue_gt_14", ">14 天", bucketAmountMap)
        );
    }

    private WelcomeOverdueBucketVO buildOverdueBucket(String key, String label, Map<String, BigDecimal> bucketAmountMap) {
        WelcomeOverdueBucketVO bucket = new WelcomeOverdueBucketVO();
        bucket.setKey(key);
        bucket.setLabel(label);
        bucket.setAmount(bucketAmountMap.getOrDefault(key, BigDecimal.ZERO));
        return bucket;
    }

    private List<WelcomeCountBucketVO> buildVacancyBuckets(Integer leaseMode) {
        Map<String, Integer> bucketCountMap = roomRepo.getWelcomeVacancyBuckets(leaseMode).stream()
            .collect(Collectors.toMap(WelcomeCountBucketVO::getKey, item -> ObjectUtil.defaultIfNull(item.getCount(), 0), Integer::sum));
        return List.of(
            buildVacancyBucket("vacancy_1_7", "1-7 天", bucketCountMap),
            buildVacancyBucket("vacancy_8_15", "8-15 天", bucketCountMap),
            buildVacancyBucket("vacancy_gt_15", "15 天以上", bucketCountMap)
        );
    }

    private WelcomeCountBucketVO buildVacancyBucket(String key, String label, Map<String, Integer> bucketCountMap) {
        WelcomeCountBucketVO bucket = new WelcomeCountBucketVO();
        bucket.setKey(key);
        bucket.setLabel(label);
        bucket.setCount(bucketCountMap.getOrDefault(key, 0));
        return bucket;
    }

    private WelcomeContractWarningVO buildContractWarning() {
        Date today = DateUtil.beginOfDay(new Date());
        Date day7 = DateUtil.endOfDay(DateUtil.offsetDay(today, 7));
        Date day30 = DateUtil.endOfDay(DateUtil.offsetDay(today, 30));
        List<Lease> effectiveLeases = leaseRepo.list(new LambdaQueryWrapper<Lease>()
            .eq(Lease::getStatus, LeaseStatusEnum.EFFECTIVE.getCode())
            .isNotNull(Lease::getLeaseEnd));

        WelcomeContractWarningVO warning = new WelcomeContractWarningVO();
        warning.setNext7DaysReceivableAmount(ObjectUtil.defaultIfNull(leaseBillRepo.getNext7DaysReceivableAmount(), BigDecimal.ZERO));
        warning.setExpiring7DaysCount((int) effectiveLeases.stream()
            .filter(item -> isInRange(item.getLeaseEnd(), today, day7))
            .count());
        warning.setExpiring30DaysCount((int) effectiveLeases.stream()
            .filter(item -> isInRange(item.getLeaseEnd(), today, day30))
            .count());
        return warning;
    }

    private List<WelcomeOverdueTenantVO> buildOverdueTenantTopList() {
        return leaseBillRepo.getWelcomeOverdueTenantTopList().stream()
            .peek(item -> item.setUnpaidAmount(ObjectUtil.defaultIfNull(item.getUnpaidAmount(), BigDecimal.ZERO)))
            .toList();
    }

    private WelcomeTenantStatsVO buildTenantStats() {
        Date now = new Date();
        Date todayStart = DateUtil.beginOfDay(now);
        Date monthStart = DateUtil.beginOfMonth(now);
        Date todayEnd = DateUtil.endOfDay(now);
        Date monthEnd = DateUtil.endOfMonth(now);

        List<Booking> bookings = bookingRepo.list(new LambdaQueryWrapper<Booking>()
            .eq(Booking::getBookingStatus, BookingStatusEnum.BOOKING.getCode()));
        List<Lease> leases = leaseRepo.list(new LambdaQueryWrapper<Lease>()
            .ne(Lease::getStatus, LeaseStatusEnum.CANCELLED.getCode()));

        WelcomeTenantStatsVO stats = new WelcomeTenantStatsVO();
        stats.setTodayDepositCount((int) bookings.stream()
            .filter(item -> isInRange(ObjectUtil.defaultIfNull(item.getBookingTime(), item.getCreateTime()), todayStart, todayEnd))
            .count());
        stats.setMonthDepositCount((int) bookings.stream()
            .filter(item -> isInRange(ObjectUtil.defaultIfNull(item.getBookingTime(), item.getCreateTime()), monthStart, monthEnd))
            .count());
        stats.setTodayNewSignCount((int) leases.stream()
            .filter(item -> Objects.equals(item.getContractNature(), 1))
            .filter(item -> isInRange(item.getCreateTime(), todayStart, todayEnd))
            .count());
        stats.setMonthNewSignCount((int) leases.stream()
            .filter(item -> Objects.equals(item.getContractNature(), 1))
            .filter(item -> isInRange(item.getCreateTime(), monthStart, monthEnd))
            .count());
        stats.setTodayRenewCount((int) leases.stream()
            .filter(item -> Objects.equals(item.getContractNature(), 2))
            .filter(item -> isInRange(item.getCreateTime(), todayStart, todayEnd))
            .count());
        stats.setMonthRenewCount((int) leases.stream()
            .filter(item -> Objects.equals(item.getContractNature(), 2))
            .filter(item -> isInRange(item.getCreateTime(), monthStart, monthEnd))
            .count());
        return stats;
    }
}
