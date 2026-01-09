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
import com.homi.model.dao.entity.TenantBillOtherFee;
import com.homi.model.dao.repo.TenantBillOtherFeeRepo;
import com.homi.model.dao.repo.TenantBillRepo;
import com.homi.model.dto.room.price.OtherFeeDTO;
import com.homi.model.dto.tenant.TenantDTO;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class TenantBillGenService {
    private final TenantBillRepo tenantBillRepo;
    private final TenantBillOtherFeeRepo tenantBillOtherFeeRepo;

    /**
     * 生成租客账单（押金、租金及其他费用）
     *
     * @param tenantId  租客ID
     * @param tenant    租客信息
     * @param otherFees 其他费用列表
     */
    @Transactional(rollbackFor = Exception.class)
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
            BigDecimal rentalAmount = calculateRentalAmount(tenant.getRentPrice(), paymentMonths, actualMonths);

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

            RentBillContext billContext = RentBillContext.builder()
                .tenantId(tenantId)
                .tenant(tenant)
                .sortOrder(sortOrder++)
                .actualMonths(actualMonths)
                .currentStart(currentStart)
                .currentEnd(currentEnd)
                .rentalAmount(rentalAmount)
                .otherFeeAmount(otherFeeAmount)
                .dueDate(dueDate)
                .build();

            TenantBill bill = createRentBill(billContext);

            billList.add(bill);

            currentStart = currentStart.plusMonths(paymentMonths);
            isFirstBill = false;
        }

        if (billList.isEmpty()) {
            return;
        }

        // 批量保存账单
        tenantBillRepo.saveBatch(billList);

        if (rentRelatedFees.isEmpty()) {
            return;
        }

        // 保存账单的其他费用明细
        List<TenantBillOtherFee> otherFeeDetails = new ArrayList<>();

        for (TenantBill bill : billList) {
            if (bill.getOtherFeeAmount() != null &&
                bill.getOtherFeeAmount().compareTo(BigDecimal.ZERO) > 0) {
                // 为每个费用项创建明细记录
                otherFeeDetails.addAll(
                    createOtherFeeDetails(bill, rentRelatedFees, tenant)
                );
            }
        }

        if (!otherFeeDetails.isEmpty()) {
            tenantBillOtherFeeRepo.saveBatch(otherFeeDetails);
        }
    }

    /**
     * 创建其他费用明细记录
     *
     * @param bill            账单
     * @param rentRelatedFees 随房租付的费用列表
     * @param tenant          租客信息
     * @return 费用明细列表
     */
    private List<TenantBillOtherFee> createOtherFeeDetails(
        TenantBill bill,
        List<OtherFeeDTO> rentRelatedFees,
        TenantDTO tenant) {

        List<TenantBillOtherFee> details = new ArrayList<>();

        LocalDate startDate = LocalDateTimeUtil.of(bill.getRentPeriodStart()).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(bill.getRentPeriodEnd()).toLocalDate();
        int actualMonths = calculateMonths(startDate, endDate);

        // 计算本期租金（用于比例计算）
        BigDecimal periodRentalAmount = bill.getRentalAmount();

        for (OtherFeeDTO fee : rentRelatedFees) {
            // 计算单个费用的金额
            BigDecimal feeAmount = calculateSingleFeeAmount(
                fee, periodRentalAmount, actualMonths
            ).setScale(2, RoundingMode.HALF_UP);

            if (feeAmount.compareTo(BigDecimal.ZERO) > 0) {
                TenantBillOtherFee detail = new TenantBillOtherFee();
                detail.setBillId(bill.getId());
                detail.setDictDataId(fee.getDictDataId());
                detail.setName(fee.getName());
                detail.setAmount(feeAmount);
                detail.setRemark(buildFeeRemark(fee, actualMonths));
                detail.setDeleted(false);
                detail.setCreateBy(tenant.getCreateBy());
                detail.setCreateTime(new Date());

                details.add(detail);
            }
        }

        return details;
    }

    /**
     * 构建费用备注
     *
     * @param fee          费用配置
     * @param actualMonths 实际月数
     * @return 备注信息
     */
    private String buildFeeRemark(OtherFeeDTO fee, int actualMonths) {
        PriceMethodEnum method = PriceMethodEnum.values()[fee.getPriceMethod()];
        return switch (method) {
            case FIXED -> String.format("%s元 × %d月", fee.getPriceInput(), actualMonths);
            case RATIO -> String.format("租金 × %f%%", fee.getPriceInput());
        };
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
     * @param rentPrice   月租金
     * @param paymentMonths 支付月数
     * @param actualMonths  实际月数
     * @return 租金金额
     */
    private BigDecimal calculateRentalAmount(BigDecimal rentPrice,
                                             int paymentMonths,
                                             int actualMonths) {
        return (actualMonths >= paymentMonths)
            ? rentPrice.multiply(BigDecimal.valueOf(paymentMonths))
            : rentPrice.multiply(BigDecimal.valueOf(actualMonths));
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
    private BigDecimal calculateFixedFee(BigDecimal priceInput, int actualMonths) {
        return priceInput
            .multiply(BigDecimal.valueOf(actualMonths));
    }

    /**
     * 计算比例费用：租金 × 比例
     *
     * @param rentalAmount 租金金额
     * @param priceInput   比例（百分比）
     * @return 费用金额
     */
    private BigDecimal calculateRatioFee(BigDecimal rentalAmount, BigDecimal priceInput) {
        return rentalAmount
            .multiply(priceInput)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 创建租金账单实体
     *
     * @param context 账单创建上下文
     * @return 租金账单
     */
    private TenantBill createRentBill(RentBillContext context) {
        TenantBill bill = new TenantBill();
        bill.setTenantId(context.tenantId);
        bill.setCompanyId(context.tenant.getCompanyId());
        bill.setRemark("第" + context.sortOrder + "期，共 " + context.actualMonths + " 月");
        bill.setSortOrder(context.sortOrder);
        bill.setBillType(TenantBillTypeEnum.RENT.getCode());
        bill.setRentPeriodStart(DateUtil.date(context.currentStart));
        bill.setRentPeriodEnd(DateUtil.date(context.currentEnd));
        bill.setRentalAmount(context.rentalAmount);
        bill.setDepositAmount(BigDecimal.ZERO);
        bill.setOtherFeeAmount(context.otherFeeAmount);
        bill.setTotalAmount(context.rentalAmount.add(context.otherFeeAmount));
        bill.setDueDate(context.dueDate);
        bill.setPayStatus(PaymentStatusEnum.UNPAID.getCode());
        bill.setDeleted(false);
        bill.setCreateBy(context.tenant.getCreateBy());
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

        if (independentFees.isEmpty()) {
            return;
        }

        List<TenantBill> billList = new ArrayList<>();
        List<FeeWithBills> feeWithBillsList = new ArrayList<>();
        int baseSortOrder = 1; // 使用较大的序号，避免与租金账单冲突

        for (int i = 0; i < independentFees.size(); i++) {
            OtherFeeDTO fee = independentFees.get(i);
            Integer paymentMethod = fee.getPaymentMethod();

            // 根据付款方式生成账单
            if (PaymentMethodEnum.ALL.getCode().equals(paymentMethod)) {
                // 一次性全支付
                OtherFeeBillContext billContext = OtherFeeBillContext.builder()
                    .tenantId(tenantId)
                    .tenant(tenant)
                    .fee(fee)
                    .periodStart(tenant.getLeaseStart())
                    .periodEnd(tenant.getLeaseEnd())
                    .sortOrder(baseSortOrder + i)
                    .periodNumber(1)
                    .build();

                TenantBill bill = createSingleOtherFeeBill(billContext);
                billList.add(bill);
                feeWithBillsList.add(new FeeWithBills(fee, List.of(bill)));

            } else {
                // 按周期付款（月付、季付等）
                List<TenantBill> periodicBills = createPeriodicOtherFeeBills(
                    tenantId, tenant, fee, baseSortOrder + i * 100);
                billList.addAll(periodicBills);
                feeWithBillsList.add(new FeeWithBills(fee, periodicBills));
            }
        }

        if (!billList.isEmpty()) {
            // 批量保存账单
            tenantBillRepo.saveBatch(billList);

            // 保存独立费用的明细
            List<TenantBillOtherFee> otherFeeDetails = new ArrayList<>();

            for (FeeWithBills feeWithBills : feeWithBillsList) {
                OtherFeeDTO fee = feeWithBills.fee;
                List<TenantBill> bills = feeWithBills.bills;

                for (TenantBill bill : bills) {
                    TenantBillOtherFee detail = new TenantBillOtherFee();
                    detail.setBillId(bill.getId());
                    detail.setDictDataId(fee.getDictDataId());
                    detail.setName(fee.getName());
                    detail.setAmount(bill.getOtherFeeAmount());
                    detail.setRemark(bill.getRemark());
                    detail.setDeleted(false);
                    detail.setCreateBy(tenant.getCreateBy());
                    detail.setCreateTime(new Date());

                    otherFeeDetails.add(detail);
                }
            }

            if (!otherFeeDetails.isEmpty()) {
                tenantBillOtherFeeRepo.saveBatch(otherFeeDetails);
            }
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

            OtherFeeBillContext billContext = OtherFeeBillContext.builder()
                .tenantId(tenantId)
                .tenant(tenant)
                .fee(fee)
                .periodStart(DateUtil.date(currentStart))
                .periodEnd(DateUtil.date(currentEnd))
                .sortOrder(baseSortOrder + sortOrder)
                .periodNumber(sortOrder)
                .build();

            TenantBill bill = createSingleOtherFeeBill(billContext);
            billList.add(bill);

            currentStart = currentStart.plusMonths(periodMonths);
            sortOrder++;
        }

        return billList;
    }

    /**
     * 创建单个其他费用账单
     *
     * @param context 账单创建上下文
     * @return 账单实体
     */
    private TenantBill createSingleOtherFeeBill(OtherFeeBillContext context) {
        LocalDate startDate = LocalDateTimeUtil.of(context.periodStart).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(context.periodEnd).toLocalDate();
        int actualMonths = calculateMonths(startDate, endDate);

        // 计算费用金额
        BigDecimal rentalAmount = context.tenant.getRentPrice()
            .multiply(BigDecimal.valueOf(actualMonths));
        BigDecimal feeAmount = calculateSingleFeeAmount(context.fee, rentalAmount, actualMonths)
            .setScale(2, RoundingMode.HALF_UP);

        // 创建账单配置
        BillConfig config = BillConfig.builder()
            .periodStart(startDate)
            .isFirstBill(context.periodNumber == 1)
            .firstBillDay(context.tenant.getFirstBillDay())
            .rentDueType(context.tenant.getRentDueType())
            .rentDueDay(context.tenant.getRentDueDay())
            .rentDueOffsetDays(context.tenant.getRentDueOffsetDays())
            .build();

        TenantBill bill = new TenantBill();
        bill.setTenantId(context.tenantId);
        bill.setCompanyId(context.tenant.getCompanyId());
        bill.setRemark(context.fee.getName() + " - 第" + context.periodNumber + "期，共 " + actualMonths + " 月");
        bill.setSortOrder(context.sortOrder);
        bill.setBillType(TenantBillTypeEnum.OTHER_FEE.getCode());
        bill.setRentPeriodStart(context.periodStart);
        bill.setRentPeriodEnd(context.periodEnd);
        bill.setRentalAmount(BigDecimal.ZERO);
        bill.setDepositAmount(BigDecimal.ZERO);
        bill.setOtherFeeAmount(feeAmount);
        bill.setTotalAmount(feeAmount);
        bill.setDueDate(calculateDueDate(config));
        bill.setPayStatus(PaymentStatusEnum.UNPAID.getCode());
        bill.setDeleted(false);
        bill.setCreateBy(context.tenant.getCreateBy());
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
        // 首期账单且跟随合同创建日
        if (config.isFirstBill && config.firstBillDay != null &&
            config.firstBillDay.equals(TenantFirstBillDayEnum.FOLLOW_CONTRACT_CREATE.getCode())) {
            return new Date();
        }

        // 根据收租类型计算应收日期
        LocalDate dueDate = calculateDueDateByType(config);
        return DateUtil.date(dueDate);
    }

    /**
     * 根据收租类型计算应收日期
     *
     * @param config 账单配置参数
     * @return 应收日期
     */
    private LocalDate calculateDueDateByType(BillConfig config) {
        // 提前收租：账期开始日 - 偏移天数
        if (Objects.equals(config.rentDueType, TenantRentDueTypeEnum.EARLY.getCode())) {
            int offsetDays = config.rentDueOffsetDays != null ? config.rentDueOffsetDays : 0;
            return config.periodStart.minusDays(offsetDays);
        }

        // 固定日收租：当月的固定日期
        if (Objects.equals(config.rentDueType, TenantRentDueTypeEnum.FIXED.getCode())) {
            return calculateFixedDueDate(config.periodStart, config.rentDueDay);
        }

        // 延后收租：账期开始日 + 偏移天数
        if (Objects.equals(config.rentDueType, TenantRentDueTypeEnum.LATE.getCode())) {
            int offsetDays = config.rentDueOffsetDays != null ? config.rentDueOffsetDays : 0;
            return config.periodStart.plusDays(offsetDays);
        }

        // 默认：应收日为账期开始日
        return config.periodStart;
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
        if (tenant.getDepositMonths() == null || tenant.getDepositMonths() <= 0) {
            return;
        }

        TenantBill depositBill = new TenantBill();
        depositBill.setTenantId(tenantId);
        depositBill.setCompanyId(tenant.getCompanyId());
        depositBill.setSortOrder(0);
        depositBill.setBillType(TenantBillTypeEnum.DEPOSIT.getCode());
        depositBill.setRentPeriodStart(tenant.getLeaseStart());
        depositBill.setRentPeriodEnd(tenant.getLeaseEnd());

        // 计算押金金额 = 月租金 × 押金月数
        BigDecimal depositAmount = tenant.getRentPrice()
            .multiply(BigDecimal.valueOf(tenant.getDepositMonths()))
            .setScale(2, RoundingMode.HALF_UP);

        depositBill.setDepositAmount(depositAmount);
        depositBill.setRentalAmount(BigDecimal.ZERO);
        depositBill.setOtherFeeAmount(BigDecimal.ZERO);
        depositBill.setTotalAmount(depositAmount);

        // 根据收租规则计算押金应收日期
        Date dueDate = calculateDepositDueDate(tenant);
        depositBill.setDueDate(dueDate);

        depositBill.setPayStatus(PaymentStatusEnum.UNPAID.getCode());
        depositBill.setRemark("押金账单");
        depositBill.setDeleted(false);
        depositBill.setCreateBy(tenant.getCreateBy());
        depositBill.setCreateTime(new Date());

        tenantBillRepo.save(depositBill);
    }

    /**
     * 计算押金应收日期
     *
     * @param tenant 租客信息
     * @return 应收日期
     */
    private Date calculateDepositDueDate(TenantDTO tenant) {
        // 如果首期账单跟随合同创建日，则押金立即应收（当天）
        if (tenant.getFirstBillDay() != null && tenant.getFirstBillDay().equals(TenantFirstBillDayEnum.FOLLOW_CONTRACT_CREATE.getCode())) {
            return new Date();
        }

        // 否则，根据收租类型和起租日计算押金应收日期
        LocalDate leaseStartDate = LocalDateTimeUtil.of(tenant.getLeaseStart()).toLocalDate();

        BillConfig config = BillConfig.builder()
            .periodStart(leaseStartDate)
            .isFirstBill(true)
            .firstBillDay(tenant.getFirstBillDay())
            .rentDueType(tenant.getRentDueType())
            .rentDueDay(tenant.getRentDueDay())
            .rentDueOffsetDays(tenant.getRentDueOffsetDays())
            .build();

        return calculateDueDate(config);
    }

    /**
     * 账单配置参数类（用于减少方法参数数量）
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

    /**
     * 费用与账单关联类（用于保存明细时关联）
     */
    private record FeeWithBills(OtherFeeDTO fee, List<TenantBill> bills) {
    }

    /**
     * 租金账单创建上下文
     */
    @Builder
    private record RentBillContext(
        Long tenantId,
        TenantDTO tenant,
        int sortOrder,
        int actualMonths,
        LocalDate currentStart,
        LocalDate currentEnd,
        BigDecimal rentalAmount,
        BigDecimal otherFeeAmount,
        Date dueDate
    ) {
    }

    /**
     * 其他费用账单创建上下文
     */
    @Builder
    private record OtherFeeBillContext(
        Long tenantId,
        TenantDTO tenant,
        OtherFeeDTO fee,
        Date periodStart,
        Date periodEnd,
        int sortOrder,
        int periodNumber
    ) {
    }
}
