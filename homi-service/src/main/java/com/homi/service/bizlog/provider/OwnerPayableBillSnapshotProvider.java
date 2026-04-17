package com.homi.service.bizlog.provider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.model.dao.entity.OwnerPayableBill;
import com.homi.model.dao.entity.OwnerPayableBillFee;
import com.homi.model.dao.entity.OwnerPayableBillPayment;
import com.homi.model.dao.repo.OwnerPayableBillFeeRepo;
import com.homi.model.dao.repo.OwnerPayableBillPaymentRepo;
import com.homi.model.dao.repo.OwnerPayableBillRepo;
import com.homi.service.bizlog.BizOperateLogSnapshotProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component("ownerPayableBillSnapshotProvider")
@RequiredArgsConstructor
public class OwnerPayableBillSnapshotProvider implements BizOperateLogSnapshotProvider {
    private final OwnerPayableBillRepo ownerPayableBillRepo;
    private final OwnerPayableBillFeeRepo ownerPayableBillFeeRepo;
    private final OwnerPayableBillPaymentRepo ownerPayableBillPaymentRepo;

    @Override
    public Object getBeforeSnapshot(Object[] args) {
        return buildSnapshot(resolveBillId(args, null));
    }

    @Override
    public Object getAfterSnapshot(Object[] args, Object result) {
        return buildSnapshot(resolveBillId(args, result));
    }

    private Long resolveBillId(Object[] args, Object result) {
        if (args != null) {
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                Long billId = extractBillId(arg);
                if (billId != null) {
                    return billId;
                }
            }
        }
        if (result instanceof Long id) {
            return id;
        }
        return null;
    }

    private Long extractBillId(Object arg) {
        try {
            Object value = arg.getClass().getMethod("getBillId").invoke(arg);
            return value instanceof Long id ? id : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private OwnerPayableBillLogSnapshot buildSnapshot(Long billId) {
        if (billId == null) {
            return null;
        }
        OwnerPayableBill bill = ownerPayableBillRepo.getById(billId);
        if (bill == null) {
            return null;
        }
        List<OwnerPayableBillFeeSnapshot> feeList = ownerPayableBillFeeRepo.list(new LambdaQueryWrapper<OwnerPayableBillFee>()
                .eq(OwnerPayableBillFee::getBillId, billId)
                .orderByAsc(OwnerPayableBillFee::getId))
            .stream()
            .map(item -> new OwnerPayableBillFeeSnapshot(
                item.getId(),
                item.getDictDataId(),
                item.getFeeType(),
                item.getFeeName(),
                item.getDirection(),
                item.getAmount(),
                item.getBizDate(),
                item.getRemark()
            ))
            .toList();
        List<OwnerPayableBillPaymentSnapshot> paymentList = ownerPayableBillPaymentRepo.list(new LambdaQueryWrapper<OwnerPayableBillPayment>()
                .eq(OwnerPayableBillPayment::getBillId, billId)
                .orderByAsc(OwnerPayableBillPayment::getId))
            .stream()
            .map(item -> new OwnerPayableBillPaymentSnapshot(
                item.getId(),
                item.getPaymentNo(),
                item.getPayAmount(),
                item.getPayAt(),
                item.getPayChannel(),
                item.getThirdTradeNo(),
                item.getRemark()
            ))
            .toList();
        return new OwnerPayableBillLogSnapshot(
            bill.getId(),
            bill.getCompanyId(),
            bill.getOwnerId(),
            bill.getContractId(),
            bill.getBillNo(),
            bill.getSubjectNameSnapshot(),
            bill.getBillStartDate(),
            bill.getBillEndDate(),
            bill.getDueDate(),
            bill.getPayableAmount(),
            bill.getPaidAmount(),
            bill.getUnpaidAmount(),
            bill.getAdjustAmount(),
            bill.getPaymentStatus(),
            bill.getBillStatus(),
            bill.getCancelReason(),
            bill.getCancelBy(),
            bill.getCancelByName(),
            bill.getCancelAt(),
            bill.getGeneratedAt(),
            bill.getRemark(),
            feeList,
            paymentList
        );
    }

    @Data
    @AllArgsConstructor
    public static class OwnerPayableBillLogSnapshot {
        private Long id;
        private Long companyId;
        private Long ownerId;
        private Long contractId;
        private String billNo;
        private String subjectNameSnapshot;
        private Date billStartDate;
        private Date billEndDate;
        private Date dueDate;
        private BigDecimal payableAmount;
        private BigDecimal paidAmount;
        private BigDecimal unpaidAmount;
        private BigDecimal adjustAmount;
        private Integer paymentStatus;
        private Integer billStatus;
        private String cancelReason;
        private Long cancelBy;
        private String cancelByName;
        private Date cancelAt;
        private Date generatedAt;
        private String remark;
        private List<OwnerPayableBillFeeSnapshot> feeList = Collections.emptyList();
        private List<OwnerPayableBillPaymentSnapshot> paymentList = Collections.emptyList();
    }

    @Data
    @AllArgsConstructor
    public static class OwnerPayableBillFeeSnapshot {
        private Long id;
        private Long dictDataId;
        private String feeType;
        private String feeName;
        private String direction;
        private BigDecimal amount;
        private Date bizDate;
        private String remark;
    }

    @Data
    @AllArgsConstructor
    public static class OwnerPayableBillPaymentSnapshot {
        private Long id;
        private String paymentNo;
        private BigDecimal payAmount;
        private Date payAt;
        private String payChannel;
        private String thirdTradeNo;
        private String remark;
    }
}
