package com.homi.service.service.lease.bill;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.EnumUtil;
import com.homi.common.lib.enums.pay.PayStatusEnum;
import com.homi.common.lib.enums.lease.LeaseBillStatusEnum;
import com.homi.common.lib.enums.price.PaymentMethodEnum;
import com.homi.common.lib.enums.price.PriceMethodEnum;
import com.homi.common.lib.enums.lease.LeaseBillFeeTypeEnum;
import com.homi.common.lib.enums.lease.LeaseBillTypeEnum;
import com.homi.common.lib.enums.lease.LeaseFirstBillDayEnum;
import com.homi.common.lib.enums.lease.LeaseRentDueTypeEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.repo.LeaseBillFeeRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.room.dto.price.OtherFeeDTO;
import com.homi.model.tenant.dto.LeaseDTO;
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
public class LeaseBillGenService {
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;

    /**
     * 生成租客账单（押金、租金及其他费用）
     *
     * @param leaseId   租约ID
     * @param tenantId  租客ID
     * @param lease     租约信息
     * @param otherFees 其他费用列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void addLeaseBill(Long leaseId, Long tenantId, LeaseDTO lease, List<OtherFeeDTO> otherFees) {
        // 生成押金账单
        addTenantDepositBill(leaseId, tenantId, lease);

        // 生成租金账单（包含随房租付的其他费用）
        addTenantRentBill(leaseId, tenantId, lease, otherFees);

        // 生成独立的其他费用账单
        addLeaseOtherFeeBills(leaseId, tenantId, lease, otherFees);
    }

    /**
     * 生成租金账单（包含随房租付的其他费用）
     *
     * @param leaseId   租约ID
     * @param tenantId  租客ID
     * @param lease     租约信息
     * @param otherFees 其他费用列表
     */
    private void addTenantRentBill(Long leaseId, Long tenantId, LeaseDTO lease, List<OtherFeeDTO> otherFees) {
        int sortOrder = 1;
        LocalDate currentStart = LocalDateTimeUtil.of(lease.getLeaseStart()).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(lease.getLeaseEnd()).toLocalDate();
        int paymentMonths = lease.getPaymentMonths();

        // 筛选出随房租付的其他费用
        List<OtherFeeDTO> rentRelatedFees = filterRentRelatedFees(otherFees);

        List<LeaseBill> billList = new ArrayList<>();
        boolean isFirstBill = true;

        // 按支付周期循环生成账单
        while (!currentStart.isAfter(endDate)) {
            LocalDate currentEnd = currentStart.plusMonths(paymentMonths).minusDays(1);
            if (currentEnd.isAfter(endDate)) {
                currentEnd = endDate;
            }

            int actualMonths = calculateMonths(currentStart, currentEnd);

            // 计算租金金额
            BigDecimal periodRentAmount = calculateRentAmount(lease.getRentPrice(), paymentMonths, actualMonths);

            // 计算其他费用金额
            BigDecimal periodOtherFeeAmount = calculateOtherFeeAmount(rentRelatedFees, periodRentAmount, actualMonths);

            // 创建账单配置参数对象
            BillConfig config = BillConfig.builder()
                .periodStart(currentStart)
                .isFirstBill(isFirstBill)
                .firstBillDay(lease.getFirstBillDay())
                .rentDueType(lease.getRentDueType())
                .rentDueDay(lease.getRentDueDay())
                .rentDueOffsetDays(lease.getRentDueOffsetDays())
                .build();

            Date dueDate = calculateDueDate(config);

            RentBillContext billContext = RentBillContext.builder()
                .leaseId(leaseId)
                .tenantId(tenantId)
                .lease(lease)
                .sortOrder(sortOrder++)
                .actualMonths(actualMonths)
                .currentStart(currentStart)
                .currentEnd(currentEnd)
                .periodRentAmount(periodRentAmount)
                .periodOtherFeeAmount(periodOtherFeeAmount)
                .dueDate(dueDate)
                .build();

            LeaseBill bill = createRentBill(billContext);

            billList.add(bill);

            currentStart = currentStart.plusMonths(paymentMonths);
            isFirstBill = false;
        }

        if (billList.isEmpty()) {
            return;
        }

        // 批量保存账单
        leaseBillRepo.saveBatch(billList);

        // 保存账单费用明细（租金 + 随房租付费用）
        List<LeaseBillFee> feeDetails = new ArrayList<>();

        for (LeaseBill bill : billList) {
            LocalDate feeStartDate = LocalDateTimeUtil.of(bill.getBillStart()).toLocalDate();
            LocalDate feeEndDate = LocalDateTimeUtil.of(bill.getBillEnd()).toLocalDate();
            int actualMonths = calculateMonths(feeStartDate, feeEndDate);

            BigDecimal periodRentAmount = lease.getRentPrice()
                .multiply(BigDecimal.valueOf(actualMonths))
                .setScale(2, RoundingMode.HALF_UP);

            LeaseBillFee rentFee = new LeaseBillFee();
            rentFee.setBillId(bill.getId());
            rentFee.setFeeType(LeaseBillFeeTypeEnum.RENTAL.getCode());
            rentFee.setFeeName("租金");
            rentFee.setAmount(periodRentAmount);
            rentFee.setPaidAmount(BigDecimal.ZERO);
            rentFee.setUnpaidAmount(periodRentAmount);
            rentFee.setPayStatus(PayStatusEnum.UNPAID.getCode());
            rentFee.setFeeStart(bill.getBillStart());
            rentFee.setFeeEnd(bill.getBillEnd());
            rentFee.setRemark(bill.getRemark());
            rentFee.setDeleted(false);
            rentFee.setCreateBy(lease.getCreateBy());
            rentFee.setCreateAt(new Date());
            feeDetails.add(rentFee);

            if (!rentRelatedFees.isEmpty()) {
                feeDetails.addAll(createOtherFeeDetails(bill, rentRelatedFees, lease));
            }
        }

        if (!feeDetails.isEmpty()) {
            leaseBillFeeRepo.saveBatch(feeDetails);
        }
    }

    /**
     * 创建其他费用明细记录
     *
     * @param bill            账单
     * @param rentRelatedFees 随房租付的费用列表
     * @param lease           租约信息
     * @return 费用明细列表
     */
    private List<LeaseBillFee> createOtherFeeDetails(LeaseBill bill, List<OtherFeeDTO> rentRelatedFees, LeaseDTO lease) {

        List<LeaseBillFee> details = new ArrayList<>();

        LocalDate startDate = LocalDateTimeUtil.of(bill.getBillStart()).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(bill.getBillEnd()).toLocalDate();
        int actualMonths = calculateMonths(startDate, endDate);

        // 计算本期租金（用于比例计算）
        BigDecimal periodRentalAmount = lease.getRentPrice().multiply(BigDecimal.valueOf(actualMonths));

        for (OtherFeeDTO fee : rentRelatedFees) {
            // 计算单个费用的金额
            BigDecimal feeAmount = calculateSingleFeeAmount(
                fee, periodRentalAmount, actualMonths
            ).setScale(2, RoundingMode.HALF_UP);

            if (feeAmount.compareTo(BigDecimal.ZERO) > 0) {
                LeaseBillFee detail = new LeaseBillFee();
                detail.setBillId(bill.getId());
                detail.setFeeType(LeaseBillFeeTypeEnum.OTHER_FEE.getCode());
                detail.setDictDataId(fee.getDictDataId());
                detail.setFeeName(fee.getName());
                detail.setAmount(feeAmount);
                detail.setPaidAmount(BigDecimal.ZERO);
                detail.setUnpaidAmount(feeAmount);
                detail.setPayStatus(PayStatusEnum.UNPAID.getCode());
                detail.setFeeStart(bill.getBillStart());
                detail.setFeeEnd(bill.getBillEnd());
                detail.setRemark(buildFeeRemark(fee, actualMonths));
                detail.setDeleted(false);
                detail.setCreateBy(lease.getCreateBy());
                detail.setCreateAt(new Date());

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
        PriceMethodEnum method = EnumUtil.getBy(PriceMethodEnum::getCode, fee.getPriceMethod());
        if (method == null) {
            return "";
        }
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
     * @param rentPrice     月租金
     * @param paymentMonths 支付月数
     * @param actualMonths  实际月数
     * @return 租金金额
     */
    private BigDecimal calculateRentAmount(BigDecimal rentPrice,
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
     * @param periodRentAmount 本期租金金额
     * @param actualMonths    实际月数
     * @return 其他费用总额
     */
    private BigDecimal calculateOtherFeeAmount(List<OtherFeeDTO> rentRelatedFees,
                                               BigDecimal periodRentAmount,
                                               int actualMonths) {
        return rentRelatedFees.stream()
            .map(fee -> calculateSingleFeeAmount(fee, periodRentAmount, actualMonths))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算单个费用金额（核心计算逻辑）
     *
     * @param fee          费用配置
     * @param periodRentAmount 租金金额（用于比例计算）
     * @param actualMonths 实际月数
     * @return 费用金额
     */
    private BigDecimal calculateSingleFeeAmount(OtherFeeDTO fee, BigDecimal periodRentAmount, int actualMonths) {
        PriceMethodEnum priceMethodEnum = EnumUtil.getBy(PriceMethodEnum::getCode, fee.getPriceMethod());
        return switch (priceMethodEnum) {
            case FIXED -> calculateFixedFee(fee.getPriceInput(), actualMonths);
            case RATIO -> calculateRatioFee(periodRentAmount, fee.getPriceInput());
        };
    }

    /**
     * 计算一次性支付的单个费用金额(不乘以月数)
     *
     * @param fee       费用配置
     * @param rentPrice 月租金(用于比例计算)
     * @return 费用金额
     */
    private BigDecimal calculateSingleFeeAmountForOneTime(OtherFeeDTO fee, BigDecimal rentPrice) {
        PriceMethodEnum priceMethodEnum = EnumUtil.getBy(PriceMethodEnum::getCode, fee.getPriceMethod());
        return switch (priceMethodEnum) {
            case FIXED -> fee.getPriceInput(); // 固定金额,不乘月数
            case RATIO -> calculateRatioFee(rentPrice, fee.getPriceInput());
        };
    }

    /**
     * 计算固定金额费用: 固定金额 × 月数
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
     * @param periodRentAmount 租金金额
     * @param priceInput   比例（百分比）
     * @return 费用金额
     */
    private BigDecimal calculateRatioFee(BigDecimal periodRentAmount, BigDecimal priceInput) {
        return periodRentAmount
            .multiply(priceInput)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 创建租金账单实体
     *
     * @param context 账单创建上下文
     * @return 租金账单
     */
    private LeaseBill createRentBill(RentBillContext context) {
        LeaseBill bill = new LeaseBill();
        bill.setTenantId(context.tenantId);
        bill.setLeaseId(context.leaseId);
        bill.setCompanyId(context.lease.getCompanyId());
        bill.setRemark("第" + context.sortOrder + "期，共 " + context.actualMonths + " 月");
        bill.setSortOrder(context.sortOrder);
        bill.setBillType(LeaseBillTypeEnum.RENT.getCode());
        bill.setStatus(LeaseBillStatusEnum.NORMAL.getCode());
        bill.setHistorical(false);
        bill.setBillStart(DateUtil.date(context.currentStart));
        bill.setBillEnd(DateUtil.date(context.currentEnd));
        bill.setTotalAmount(context.periodRentAmount.add(context.periodOtherFeeAmount));
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setUnpaidAmount(bill.getTotalAmount());
        bill.setDueDate(context.dueDate);
        bill.setPayStatus(PayStatusEnum.UNPAID.getCode());
        bill.setDeleted(false);
        bill.setCreateBy(context.lease.getCreateBy());
        bill.setCreateAt(DateUtil.date());
        return bill;
    }

    /**
     * 生成独立的其他费用账单
     *
     * @param leaseId   租约ID
     * @param tenantId  租客ID
     * @param lease     租约信息
     * @param otherFees 其他费用列表
     */
    private void addLeaseOtherFeeBills(Long leaseId, Long tenantId, LeaseDTO lease, List<OtherFeeDTO> otherFees) {
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

        List<LeaseBill> billList = new ArrayList<>();
        List<FeeWithBills> feeWithBillsList = new ArrayList<>();
        int baseSortOrder = 1; // 使用较大的序号，避免与租金账单冲突

        for (int i = 0; i < independentFees.size(); i++) {
            OtherFeeDTO fee = independentFees.get(i);
            Integer paymentMethod = fee.getPaymentMethod();

            // 根据付款方式生成账单
            if (PaymentMethodEnum.ALL.getCode().equals(paymentMethod)) {
                // 一次性全支付
                OtherFeeBillContext billContext = OtherFeeBillContext.builder()
                    .leaseId(leaseId)
                    .tenantId(tenantId)
                    .lease(lease)
                    .fee(fee)
                    .periodStart(lease.getLeaseStart())
                    .periodEnd(lease.getLeaseEnd())
                    .sortOrder(baseSortOrder + i)
                    .periodNumber(1)
                    .build();

                LeaseBill bill = createSingleOtherFeeBill(billContext);
                billList.add(bill);
                feeWithBillsList.add(new FeeWithBills(fee, List.of(bill)));
            } else {
                // 按周期付款（月付、季付等）
                List<LeaseBill> periodicBills = createPeriodicOtherFeeBills(
                    leaseId, tenantId, lease, fee, baseSortOrder + i * 100);
                billList.addAll(periodicBills);
                feeWithBillsList.add(new FeeWithBills(fee, periodicBills));
            }
        }

        if (!billList.isEmpty()) {
            // 批量保存账单
            leaseBillRepo.saveBatch(billList);

            // 保存独立费用的明细
            List<LeaseBillFee> feeDetails = new ArrayList<>();

            for (FeeWithBills feeWithBills : feeWithBillsList) {
                OtherFeeDTO fee = feeWithBills.fee;
                List<LeaseBill> bills = feeWithBills.bills;

                for (LeaseBill bill : bills) {
                    BigDecimal feeAmount = calculateBillFeeAmount(fee, lease, bill);
                    LeaseBillFee detail = new LeaseBillFee();
                    detail.setBillId(bill.getId());
                    detail.setFeeType(LeaseBillFeeTypeEnum.OTHER_FEE.getCode());
                    detail.setDictDataId(fee.getDictDataId());
                    detail.setFeeName(fee.getName());
                    detail.setAmount(feeAmount);
                    detail.setFeeStart(bill.getBillStart());
                    detail.setFeeEnd(bill.getBillEnd());
                    detail.setRemark(bill.getRemark());
                    detail.setDeleted(false);
                    detail.setCreateBy(lease.getCreateBy());
                    detail.setCreateAt(new Date());

                    feeDetails.add(detail);
                }
            }

            if (!feeDetails.isEmpty()) {
                leaseBillFeeRepo.saveBatch(feeDetails);
            }
        }
    }

    /**
     * 创建周期性其他费用账单
     *
     * @param leaseId       租约ID
     * @param tenantId      租客ID
     * @param lease         租约信息
     * @param fee           费用配置
     * @param baseSortOrder 基础排序号
     * @return 账单列表
     */
    private List<LeaseBill> createPeriodicOtherFeeBills(Long leaseId, Long tenantId, LeaseDTO lease,
                                                        OtherFeeDTO fee, int baseSortOrder) {
        List<LeaseBill> billList = new ArrayList<>();

        // 根据付款方式确定周期月数
        int periodMonths = getPaymentPeriodMonths(fee.getPaymentMethod());

        LocalDate currentStart = LocalDateTimeUtil.of(lease.getLeaseStart()).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(lease.getLeaseEnd()).toLocalDate();
        int sortOrder = 1;

        while (!currentStart.isAfter(endDate)) {
            LocalDate currentEnd = currentStart.plusMonths(periodMonths).minusDays(1);
            if (currentEnd.isAfter(endDate)) {
                currentEnd = endDate;
            }

            OtherFeeBillContext billContext = OtherFeeBillContext.builder()
                .leaseId(leaseId)
                .tenantId(tenantId)
                .lease(lease)
                .fee(fee)
                .periodStart(DateUtil.date(currentStart))
                .periodEnd(DateUtil.date(currentEnd))
                .sortOrder(baseSortOrder + sortOrder)
                .periodNumber(sortOrder)
                .build();

            LeaseBill bill = createSingleOtherFeeBill(billContext);
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
    private LeaseBill createSingleOtherFeeBill(OtherFeeBillContext context) {
        LocalDate startDate = LocalDateTimeUtil.of(context.periodStart).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(context.periodEnd).toLocalDate();
        int actualMonths = calculateMonths(startDate, endDate);

        // 计算费用金额
        BigDecimal feeAmount;

        // 判断是否为一次性全额支付
        boolean isOneTimePayment = PaymentMethodEnum.ALL.getCode().equals(context.fee.getPaymentMethod());

        if (isOneTimePayment) {
            // 一次性全额支付: 直接使用固定金额或租金总额的比例,不乘以月数
            feeAmount = calculateSingleFeeAmountForOneTime(context.fee, context.lease.getRentPrice()).setScale(2, RoundingMode.HALF_UP);
        } else {
            // 周期性支付: 按月数计算
            BigDecimal periodRentAmount = context.lease.getRentPrice()
                .multiply(BigDecimal.valueOf(actualMonths));
            feeAmount = calculateSingleFeeAmount(context.fee, periodRentAmount, actualMonths)
                .setScale(2, RoundingMode.HALF_UP);
        }

        // 创建账单配置
        BillConfig config = BillConfig.builder()
            .periodStart(startDate)
            .isFirstBill(context.periodNumber == 1)
            .firstBillDay(context.lease.getFirstBillDay())
            .rentDueType(context.lease.getRentDueType())
            .rentDueDay(context.lease.getRentDueDay())
            .rentDueOffsetDays(context.lease.getRentDueOffsetDays())
            .build();

        LeaseBill bill = new LeaseBill();
        bill.setTenantId(context.tenantId);
        bill.setLeaseId(context.leaseId);
        bill.setCompanyId(context.lease.getCompanyId());
        bill.setRemark(context.fee.getName() + " - 第" + context.periodNumber + "期，共 " + actualMonths + " 月");
        bill.setSortOrder(context.sortOrder);
        bill.setBillType(LeaseBillTypeEnum.OTHER_FEE.getCode());
        bill.setStatus(LeaseBillStatusEnum.NORMAL.getCode());
        bill.setHistorical(false);
        bill.setBillStart(context.periodStart);
        bill.setBillEnd(context.periodEnd);
        bill.setTotalAmount(feeAmount);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setUnpaidAmount(feeAmount);
        bill.setDueDate(calculateDueDate(config));
        bill.setPayStatus(PayStatusEnum.UNPAID.getCode());
        bill.setDeleted(false);
        bill.setCreateBy(context.lease.getCreateBy());
        bill.setCreateAt(new Date());

        return bill;
    }

    private BigDecimal calculateBillFeeAmount(OtherFeeDTO fee, LeaseDTO lease, LeaseBill bill) {
        LocalDate startDate = LocalDateTimeUtil.of(bill.getBillStart()).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(bill.getBillEnd()).toLocalDate();
        int actualMonths = calculateMonths(startDate, endDate);

        boolean isOneTimePayment = PaymentMethodEnum.ALL.getCode().equals(fee.getPaymentMethod());
        if (isOneTimePayment) {
            return calculateSingleFeeAmountForOneTime(fee, lease.getRentPrice()).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal periodRentAmount = lease.getRentPrice()
            .multiply(BigDecimal.valueOf(actualMonths));
        return calculateSingleFeeAmount(fee, periodRentAmount, actualMonths).setScale(2, RoundingMode.HALF_UP);
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
            config.firstBillDay.equals(LeaseFirstBillDayEnum.FOLLOW_CONTRACT_CREATE.getCode())) {
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
        if (Objects.equals(config.rentDueType, LeaseRentDueTypeEnum.EARLY.getCode())) {
            int offsetDays = config.rentDueOffsetDays != null ? config.rentDueOffsetDays : 0;
            return config.periodStart.minusDays(offsetDays);
        }

        // 固定日收租：当月的固定日期
        if (Objects.equals(config.rentDueType, LeaseRentDueTypeEnum.FIXED.getCode())) {
            return calculateFixedDueDate(config.periodStart, config.rentDueDay);
        }

        // 延后收租：账期开始日 + 偏移天数
        if (Objects.equals(config.rentDueType, LeaseRentDueTypeEnum.LATE.getCode())) {
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
     * @param leaseId  租约ID
     * @param tenantId 租客ID
     * @param lease    租约信息
     */
    private void addTenantDepositBill(Long leaseId, Long tenantId, LeaseDTO lease) {
        if (lease.getDepositMonths() == null || lease.getDepositMonths() <= 0) {
            return;
        }

        LeaseBill depositBill = new LeaseBill();
        depositBill.setTenantId(tenantId);
        depositBill.setLeaseId(leaseId);
        depositBill.setCompanyId(lease.getCompanyId());
        depositBill.setSortOrder(0);
        depositBill.setBillType(LeaseBillTypeEnum.DEPOSIT.getCode());
        depositBill.setStatus(LeaseBillStatusEnum.NORMAL.getCode());
        depositBill.setHistorical(false);
        depositBill.setBillStart(lease.getLeaseStart());
        depositBill.setBillEnd(lease.getLeaseEnd());

        // 计算押金金额 = 月租金 × 押金月数
        BigDecimal depositAmount = lease.getRentPrice()
            .multiply(BigDecimal.valueOf(lease.getDepositMonths()))
            .setScale(2, RoundingMode.HALF_UP);

        depositBill.setTotalAmount(depositAmount);
        depositBill.setPaidAmount(BigDecimal.ZERO);
        depositBill.setUnpaidAmount(depositAmount);

        // 根据收租规则计算押金应收日期
        Date dueDate = calculateDepositDueDate(lease);
        depositBill.setDueDate(dueDate);

        depositBill.setPayStatus(PayStatusEnum.UNPAID.getCode());
        depositBill.setRemark("押金账单");
        depositBill.setDeleted(false);
        depositBill.setCreateBy(lease.getCreateBy());
        depositBill.setCreateAt(new Date());

        leaseBillRepo.save(depositBill);

        LeaseBillFee fee = new LeaseBillFee();
        fee.setBillId(depositBill.getId());
        fee.setFeeType(LeaseBillFeeTypeEnum.DEPOSIT.getCode());
        fee.setFeeName("押金");
        fee.setAmount(depositAmount);
        fee.setPaidAmount(BigDecimal.ZERO);
        fee.setUnpaidAmount(depositAmount);
        fee.setPayStatus(PayStatusEnum.UNPAID.getCode());
        fee.setFeeStart(depositBill.getBillStart());
        fee.setFeeEnd(depositBill.getBillEnd());
        fee.setRemark(depositBill.getRemark());
        fee.setDeleted(false);
        fee.setCreateBy(lease.getCreateBy());
        fee.setCreateAt(new Date());
        leaseBillFeeRepo.save(fee);
    }

    /**
     * 计算押金应收日期
     *
     * @param lease 租约信息
     * @return 应收日期
     */
    private Date calculateDepositDueDate(LeaseDTO lease) {
        // 如果首期账单跟随合同创建日，则押金立即应收（当天）
        if (lease.getFirstBillDay() != null && lease.getFirstBillDay().equals(LeaseFirstBillDayEnum.FOLLOW_CONTRACT_CREATE.getCode())) {
            return new Date();
        }

        // 否则，根据收租类型和起租日计算押金应收日期
        LocalDate leaseStartDate = LocalDateTimeUtil.of(lease.getLeaseStart()).toLocalDate();

        BillConfig config = BillConfig.builder()
            .periodStart(leaseStartDate)
            .isFirstBill(true)
            .firstBillDay(lease.getFirstBillDay())
            .rentDueType(lease.getRentDueType())
            .rentDueDay(lease.getRentDueDay())
            .rentDueOffsetDays(lease.getRentDueOffsetDays())
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
    private record FeeWithBills(OtherFeeDTO fee, List<LeaseBill> bills) {
    }

    /**
     * 租金账单创建上下文
     */
    @Builder
    private record RentBillContext(
        Long leaseId,
        Long tenantId,
        LeaseDTO lease,
        int sortOrder,
        int actualMonths,
        LocalDate currentStart,
        LocalDate currentEnd,
        BigDecimal periodRentAmount,
        BigDecimal periodOtherFeeAmount,
        Date dueDate
    ) {
    }

    /**
     * 其他费用账单创建上下文
     */
    @Builder
    private record OtherFeeBillContext(
        Long leaseId,
        Long tenantId,
        LeaseDTO lease,
        OtherFeeDTO fee,
        Date periodStart,
        Date periodEnd,
        int sortOrder,
        int periodNumber
    ) {
    }

    /**
     * 无效化租客未支付的账单
     *
     * @param leaseId 租约ID
     * @return 是否成功
     */
    public boolean historicalUnpaidLeaseBill(Long leaseId) {
        return leaseBillRepo.lambdaUpdate()
            .eq(LeaseBill::getLeaseId, leaseId)
            .eq(LeaseBill::getPayStatus, PayStatusEnum.UNPAID.getCode())
            .set(LeaseBill::getHistorical, true)
            .update();
    }
}
