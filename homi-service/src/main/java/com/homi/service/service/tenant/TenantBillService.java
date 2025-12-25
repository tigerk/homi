package com.homi.service.service.tenant;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.homi.common.lib.enums.payment.PaymentStatusEnum;
import com.homi.common.lib.enums.price.PaymentMethodEnum;
import com.homi.common.lib.enums.price.PriceMethodEnum;
import com.homi.common.lib.enums.tenant.TenantBillTypeEnum;
import com.homi.common.lib.enums.tenant.TenantFirstBillDayEnum;
import com.homi.common.lib.enums.tenant.TenantRentDueTypeEnum;
import com.homi.model.dao.entity.TenantBill;
import com.homi.model.dao.repo.TenantBillRepo;
import com.homi.model.dto.room.price.OtherFeeDTO;
import com.homi.model.dto.tenant.TenantDTO;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantBillService {
    private final TenantBillRepo tenantBillRepo;

    /**
     * 生成租客账单（押金、租金及其他费用）
     *
     * @param tenantId  租客ID
     * @param tenant    租客信息
     * @param otherFees 其他费用列表
     */
    public void addTenantBill(Long tenantId, TenantDTO tenant, List<OtherFeeDTO> otherFees) {
        // 生成押金账单
        addTenantDepositBill(tenantId, tenant);

        // 生成租金账单（包含随房租付的其他费用）
        addTenantRentBill(tenantId, tenant, otherFees);

        // 生成独立的其他费用账单
        addTenantOtherFeeBills(tenantId, tenant, otherFees);
    }

    /**
     * 生成租金账单（包含随房租付的其他费用）
     *
     * @param tenantId  租客ID
     * @param tenant    租客信息
     * @param otherFees 其他费用列表
     */
    private void addTenantRentBill(Long tenantId, TenantDTO tenant, List<OtherFeeDTO> otherFees) {
        int sortOrder = 1;
        LocalDate currentStart = LocalDateTimeUtil.of(tenant.getLeaseStart()).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(tenant.getLeaseEnd()).toLocalDate();
        int paymentMonths = tenant.getPaymentMonths();

        // 筛选出随房租付的其他费用
        List<OtherFeeDTO> rentRelatedFees = filterRentRelatedFees(otherFees);

        List<TenantBill> billList = new ArrayList<>();
        boolean isFirstBill = true;

        // 按支付周期循环生成账单
        while (!currentStart.isAfter(endDate)) {
            LocalDate currentEnd = currentStart.plusMonths(paymentMonths).minusDays(1);
            if (currentEnd.isAfter(endDate)) {
                currentEnd = endDate;
            }

            int actualMonths = calculateMonths(currentStart, currentEnd);

            // 计算租金金额
            BigDecimal rentalAmount = calculateRentalAmount(tenant.getRentalPrice(), paymentMonths, actualMonths);

            // 计算其他费用金额
            BigDecimal otherFeeAmount = calculateOtherFeeAmount(rentRelatedFees, rentalAmount, actualMonths);

            // 创建账单配置参数对象
            BillConfig config = BillConfig.builder()
                .periodStart(currentStart)
                .isFirstBill(isFirstBill)
                .firstBillDay(tenant.getFirstBillDay())
                .rentDueType(tenant.getRentDueType())
                .rentDueDay(tenant.getRentDueDay())
                .rentDueOffsetDays(tenant.getRentDueOffsetDays())
                .build();

            Date dueDate = calculateDueDate(config);

            TenantBill bill = createRentBill(tenantId, tenant, sortOrder++, actualMonths,
                currentStart, currentEnd, rentalAmount, otherFeeAmount, dueDate);

            billList.add(bill);

            currentStart = currentStart.plusMonths(paymentMonths);
            isFirstBill = false;
        }

        if (!billList.isEmpty()) {
            tenantBillRepo.saveBatch(billList);
        }
    }

    /**
     * 筛选随房租付的其他费用
     *
     * @param otherFees 其他费用列表
     * @return 随房租付的费用列表
     */
    private List<OtherFeeDTO> filterRentRelatedFees(List<OtherFeeDTO> otherFees) {
        if (otherFees == null) {
            return new ArrayList<>();
        }
        return otherFees.stream()
            .filter(fee -> PaymentMethodEnum.RENT.getCode().equals(fee.getPaymentMethod()))
            .collect(Collectors.toList());
    }

    /**
     * 计算租金金额
     *
     * @param rentalPrice   月租金
     * @param paymentMonths 支付月数
     * @param actualMonths  实际月数
     * @return 租金金额
     */
    private BigDecimal calculateRentalAmount(BigDecimal rentalPrice,
                                             int paymentMonths,
                                             int actualMonths) {
        return (actualMonths >= paymentMonths)
            ? rentalPrice.multiply(BigDecimal.valueOf(paymentMonths))
            : rentalPrice.multiply(BigDecimal.valueOf(actualMonths));
    }

    /**
     * 计算随房租付的其他费用总额
     *
     * @param rentRelatedFees 随房租付的费用列表
     * @param rentalAmount    本期租金金额
     * @param actualMonths    实际月数
     * @return 其他费用总额
     */
    private BigDecimal calculateOtherFeeAmount(List<OtherFeeDTO> rentRelatedFees,
                                               BigDecimal rentalAmount,
                                               int actualMonths) {
        return rentRelatedFees.stream()
            .map(fee -> calculateSingleFeeAmount(fee, rentalAmount, actualMonths))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算单个费用金额（核心计算逻辑）
     *
     * @param fee          费用配置
     * @param rentalAmount 租金金额（用于比例计算）
     * @param actualMonths 实际月数
     * @return 费用金额
     */
    private BigDecimal calculateSingleFeeAmount(OtherFeeDTO fee,
                                                BigDecimal rentalAmount,
                                                int actualMonths) {
        return switch (PriceMethodEnum.values()[fee.getPriceMethod()]) {
            case FIXED -> calculateFixedFee(fee.getPriceInput(), actualMonths);
            case RATIO -> calculateRatioFee(rentalAmount, fee.getPriceInput());
        };
    }

    /**
     * 计算固定金额费用：固定金额 × 月数
     *
     * @param priceInput   单价
     * @param actualMonths 实际月数
     * @return 费用金额
     */
    private BigDecimal calculateFixedFee(Integer priceInput, int actualMonths) {
        return BigDecimal.valueOf(priceInput)
            .multiply(BigDecimal.valueOf(actualMonths));
    }

    /**
     * 计算比例费用：租金 × 比例
     *
     * @param rentalAmount 租金金额
     * @param priceInput   比例（百分比）
     * @return 费用金额
     */
    private BigDecimal calculateRatioFee(BigDecimal rentalAmount, Integer priceInput) {
        return rentalAmount
            .multiply(BigDecimal.valueOf(priceInput))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 创建租金账单实体
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/25 16:00
     *
     * @param tenantId       租客ID
     * @param tenant         租客信息
     * @param sortOrder      排序顺序
     * @param actualMonths   实际月数
     * @param currentStart   账单开始日期
     * @param currentEnd     账单结束日期
     * @param rentalAmount   租金金额
     * @param otherFeeAmount 其他费用金额
     * @param dueDate        支付日期
     * @return com.homi.model.dao.entity.TenantBill
     */
    private TenantBill createRentBill(Long tenantId, TenantDTO tenant, int sortOrder,
                                      int actualMonths, LocalDate currentStart,
                                      LocalDate currentEnd, BigDecimal rentalAmount,
                                      BigDecimal otherFeeAmount, Date dueDate) {
        TenantBill bill = new TenantBill();
        bill.setTenantId(tenantId);
        bill.setCompanyId(tenant.getCompanyId());
        bill.setRemark("第" + sortOrder + "期，共 " + actualMonths + " 月");
        bill.setSortOrder(sortOrder);
        bill.setBillType(TenantBillTypeEnum.RENT.getCode());
        bill.setRentPeriodStart(DateUtil.date(currentStart));
        bill.setRentPeriodEnd(DateUtil.date(currentEnd));
        bill.setRentalAmount(rentalAmount);
        bill.setDepositAmount(BigDecimal.ZERO);
        bill.setOtherFeeAmount(otherFeeAmount);
        bill.setTotalAmount(rentalAmount.add(otherFeeAmount));
        bill.setDueDate(dueDate);
        bill.setPaymentStatus(PaymentStatusEnum.UNPAID.getCode());
        bill.setDeleted(false);
        bill.setCreateBy(tenant.getCreateBy());
        bill.setCreateTime(DateUtil.date());
        return bill;
    }

    /**
     * 生成独立的其他费用账单
     *
     * @param tenantId  租客ID
     * @param tenant    租客信息
     * @param otherFees 其他费用列表
     */
    private void addTenantOtherFeeBills(Long tenantId, TenantDTO tenant, List<OtherFeeDTO> otherFees) {
        if (otherFees == null || otherFees.isEmpty()) {
            return;
        }

        // 筛选出非随房租付的其他费用
        List<OtherFeeDTO> independentFees = otherFees.stream()
            .filter(fee -> !PaymentMethodEnum.RENT.getCode().equals(fee.getPaymentMethod()))
            .toList();

        List<TenantBill> billList = new ArrayList<>();
        int baseSortOrder = 1000; // 使用较大的序号，避免与租金账单冲突

        for (int i = 0; i < independentFees.size(); i++) {
            OtherFeeDTO fee = independentFees.get(i);
            Integer paymentMethod = fee.getPaymentMethod();

            // 根据付款方式生成账单
            if (PaymentMethodEnum.ALL.getCode().equals(paymentMethod)) {
                // 一次性全支付
                billList.add(createSingleOtherFeeBill(tenantId, tenant, fee,
                    tenant.getLeaseStart(), tenant.getLeaseEnd(),
                    baseSortOrder + i, 1));

            } else {
                // 按周期付款（月付、季付等）
                List<TenantBill> periodicBills = createPeriodicOtherFeeBills(
                    tenantId, tenant, fee, baseSortOrder + i * 100);
                billList.addAll(periodicBills);
            }
        }

        if (!billList.isEmpty()) {
            tenantBillRepo.saveBatch(billList);
        }
    }

    /**
     * 创建周期性其他费用账单
     *
     * @param tenantId      租客ID
     * @param tenant        租客信息
     * @param fee           费用配置
     * @param baseSortOrder 基础排序号
     * @return 账单列表
     */
    private List<TenantBill> createPeriodicOtherFeeBills(Long tenantId, TenantDTO tenant,
                                                         OtherFeeDTO fee, int baseSortOrder) {
        List<TenantBill> billList = new ArrayList<>();

        // 根据付款方式确定周期月数
        int periodMonths = getPaymentPeriodMonths(fee.getPaymentMethod());

        LocalDate currentStart = LocalDateTimeUtil.of(tenant.getLeaseStart()).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(tenant.getLeaseEnd()).toLocalDate();
        int sortOrder = 1;

        while (!currentStart.isAfter(endDate)) {
            LocalDate currentEnd = currentStart.plusMonths(periodMonths).minusDays(1);
            if (currentEnd.isAfter(endDate)) {
                currentEnd = endDate;
            }

            // 注意：createSingleOtherFeeBill 内部会重新计算 actualMonths
            // 这里不需要提前计算，直接传递日期即可
            TenantBill bill = createSingleOtherFeeBill(tenantId, tenant, fee,
                DateUtil.date(currentStart), DateUtil.date(currentEnd),
                baseSortOrder + sortOrder, sortOrder);

            billList.add(bill);

            currentStart = currentStart.plusMonths(periodMonths);
            sortOrder++;
        }

        return billList;
    }

    /**
     * 创建单个其他费用账单
     *
     * @param tenantId     租客ID
     * @param tenant       租客信息
     * @param fee          费用配置
     * @param periodStart  账期开始时间
     * @param periodEnd    账期结束时间
     * @param sortOrder    排序号
     * @param periodNumber 期数
     * @return 账单实体
     */
    private TenantBill createSingleOtherFeeBill(Long tenantId, TenantDTO tenant,
                                                OtherFeeDTO fee, Date periodStart,
                                                Date periodEnd, int sortOrder,
                                                int periodNumber) {
        LocalDate startDate = LocalDateTimeUtil.of(periodStart).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(periodEnd).toLocalDate();
        int actualMonths = calculateMonths(startDate, endDate);

        // 计算费用金额
        BigDecimal rentalAmount = tenant.getRentalPrice()
            .multiply(BigDecimal.valueOf(actualMonths));
        BigDecimal feeAmount = calculateSingleFeeAmount(fee, rentalAmount, actualMonths)
            .setScale(2, RoundingMode.HALF_UP);

        // 创建账单配置
        BillConfig config = BillConfig.builder()
            .periodStart(startDate)
            .isFirstBill(periodNumber == 1)
            .firstBillDay(tenant.getFirstBillDay())
            .rentDueType(tenant.getRentDueType())
            .rentDueDay(tenant.getRentDueDay())
            .rentDueOffsetDays(tenant.getRentDueOffsetDays())
            .build();

        TenantBill bill = new TenantBill();
        bill.setTenantId(tenantId);
        bill.setCompanyId(tenant.getCompanyId());
        bill.setRemark(fee.getName() + " - 第" + periodNumber + "期，共 " + actualMonths + " 月");
        bill.setSortOrder(sortOrder);
        bill.setBillType(TenantBillTypeEnum.OTHER_FEE.getCode());
        bill.setRentPeriodStart(periodStart);
        bill.setRentPeriodEnd(periodEnd);
        bill.setRentalAmount(BigDecimal.ZERO);
        bill.setDepositAmount(BigDecimal.ZERO);
        bill.setOtherFeeAmount(feeAmount);
        bill.setTotalAmount(feeAmount);
        bill.setDueDate(calculateDueDate(config));
        bill.setPaymentStatus(PaymentStatusEnum.UNPAID.getCode());
        bill.setDeleted(false);
        bill.setCreateBy(tenant.getCreateBy());
        bill.setCreateTime(new Date());

        return bill;
    }

    /**
     * 根据付款方式获取周期月数
     *
     * @param paymentMethod 付款方式代码
     * @return 周期月数
     */
    private int getPaymentPeriodMonths(Integer paymentMethod) {
        return switch (PaymentMethodEnum.values()[paymentMethod]) {
            case MONTH -> 1;
            case BI_MONTH -> 2;
            case QUARTER -> 3;
            case HALF_YEAR -> 6;
            case YEAR -> 12;
            default -> 1; // 默认月付
        };
    }

    /**
     * 计算应收日期
     *
     * @param config 账单配置参数
     * @return 应收日期
     */
    private Date calculateDueDate(BillConfig config) {
        LocalDate dueDate;

        // 首期账单且跟随合同创建日
        if (config.isFirstBill && config.firstBillDay != null && config.firstBillDay == 1) {
            dueDate = LocalDate.now();
        } else {
            if (Objects.equals(config.rentDueType, TenantRentDueTypeEnum.EARLY.getCode())) {
                // 提前收租
                dueDate = config.periodStart.minusDays(config.rentDueOffsetDays != null ? config.rentDueOffsetDays : 0);
            } else if (Objects.equals(config.rentDueType, TenantRentDueTypeEnum.FIXED.getCode())) {
                // 固定日收租
                dueDate = calculateFixedDueDate(config.periodStart, config.rentDueDay);
            } else if (Objects.equals(config.rentDueType, TenantRentDueTypeEnum.LATE.getCode())) {
                // 延后收租
                dueDate = config.periodStart.plusDays(config.rentDueOffsetDays != null ? config.rentDueOffsetDays : 0);
            } else {
                // 默认应收日为账期开始日
                dueDate = config.periodStart;
            }
        }

        return DateUtil.date(dueDate);
    }

    /**
     * 计算固定日期应收日
     *
     * @param periodStart 账期开始日期
     * @param rentDueDay  收租日（1-31，0表示月末）
     * @return 应收日期
     */
    private LocalDate calculateFixedDueDate(LocalDate periodStart, Integer rentDueDay) {
        if (rentDueDay == null || rentDueDay == 0) {
            // 0表示当月最后一天
            return periodStart.withDayOfMonth(periodStart.lengthOfMonth());
        }
        // 确保不超过当月最大天数
        int maxDayOfMonth = periodStart.lengthOfMonth();
        int actualDay = Math.min(rentDueDay, maxDayOfMonth);
        return periodStart.withDayOfMonth(actualDay);
    }

    /**
     * 计算两个日期之间的月数（向上取整）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 月数
     */
    private int calculateMonths(LocalDate startDate, LocalDate endDate) {
        Period period = Period.between(startDate, endDate.plusDays(1));
        int months = period.getYears() * 12 + period.getMonths();
        if (period.getDays() > 0) {
            months += 1;
        }
        return Math.max(months, 1);
    }

    /**
     * 生成押金账单
     *
     * @param tenantId 租客ID
     * @param tenant   租客信息
     */
    private void addTenantDepositBill(Long tenantId, TenantDTO tenant) {
        if (tenant.getDepositMonths() <= 0) {
            return;
        }

        TenantBill depositBill = new TenantBill();
        depositBill.setTenantId(tenantId);
        depositBill.setSortOrder(0);
        depositBill.setBillType(TenantBillTypeEnum.DEPOSIT.getCode());
        depositBill.setRentPeriodStart(DateUtil.date());
        depositBill.setRentPeriodEnd(tenant.getLeaseEnd());

        // 计算押金金额 = 月租金 × 押金月数
        BigDecimal depositAmount = tenant.getRentalPrice()
            .multiply(BigDecimal.valueOf(tenant.getDepositMonths()))
            .setScale(2, RoundingMode.HALF_UP);
        depositBill.setDepositAmount(depositAmount);
        depositBill.setTotalAmount(depositAmount);

        // 根据首期账单规则设置应收日期
        if (tenant.getFirstBillDay().equals(
            TenantFirstBillDayEnum.FOLLOW_CONTRACT_START.getCode())) {
            depositBill.setDueDate(tenant.getLeaseStart());
        } else {
            depositBill.setDueDate(DateUtil.date());
        }

        depositBill.setPaymentStatus(PaymentStatusEnum.UNPAID.getCode());
        depositBill.setRemark("第 0 期");
        depositBill.setCreateBy(tenant.getCreateBy());

        tenantBillRepo.save(depositBill);
    }

    /**
     * 账单配置参数类（用于减少方法参数数量）
     * 使用 Lombok @Builder 简化构建器模式
     */
    @Builder
    private record BillConfig(
        LocalDate periodStart,
        boolean isFirstBill,
        Integer firstBillDay,
        Integer rentDueType,
        Integer rentDueDay,
        Integer rentDueOffsetDays
    ) {
    }
}
