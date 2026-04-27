package com.homi.service.bizlog.provider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.model.dao.entity.LeaseCheckout;
import com.homi.model.dao.entity.LeaseCheckoutFee;
import com.homi.model.dao.repo.LeaseCheckoutFeeRepo;
import com.homi.model.dao.repo.LeaseCheckoutRepo;
import com.homi.service.bizlog.BizOperateLogSnapshotProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component("leaseCheckoutSnapshotProvider")
@RequiredArgsConstructor
public class LeaseCheckoutSnapshotProvider implements BizOperateLogSnapshotProvider {
    private final LeaseCheckoutRepo leaseCheckoutRepo;
    private final LeaseCheckoutFeeRepo leaseCheckoutFeeRepo;

    @Override
    public Object getBeforeSnapshot(Object[] args) {
        return buildSnapshot(resolveCheckoutId(args, null));
    }

    @Override
    public Object getAfterSnapshot(Object[] args, Object result) {
        return buildSnapshot(resolveCheckoutId(args, result));
    }

    private Long resolveCheckoutId(Object[] args, Object result) {
        if (result instanceof Long id) {
            return id;
        }
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            Long id = extractId(arg);
            if (id != null) {
                return id;
            }
        }
        return null;
    }

    private Long extractId(Object arg) {
        if (arg instanceof Long id) {
            return id;
        }
        if (arg == null) {
            return null;
        }
        try {
            Object value = arg.getClass().getMethod("getId").invoke(arg);
            return value instanceof Long id ? id : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private LeaseCheckoutLogSnapshot buildSnapshot(Long checkoutId) {
        if (checkoutId == null) {
            return null;
        }
        LeaseCheckout checkout = leaseCheckoutRepo.getById(checkoutId);
        if (checkout == null) {
            return null;
        }
        List<LeaseCheckoutFeeSnapshot> feeList = leaseCheckoutFeeRepo.list(new LambdaQueryWrapper<LeaseCheckoutFee>()
                .eq(LeaseCheckoutFee::getCheckoutId, checkoutId)
                .orderByAsc(LeaseCheckoutFee::getId))
            .stream()
            .map(item -> new LeaseCheckoutFeeSnapshot(
                item.getId(),
                item.getFeeDirection(),
                item.getFeeType(),
                item.getDictDataId(),
                item.getFeeName(),
                item.getFeeAmount(),
                item.getFeeStartDate(),
                item.getFeeEndDate(),
                item.getRemark(),
                item.getLeaseBillId()
            ))
            .toList();
        return new LeaseCheckoutLogSnapshot(
            checkout.getId(),
            checkout.getCompanyId(),
            checkout.getTenantId(),
            checkout.getLeaseId(),
            checkout.getCheckoutCode(),
            checkout.getCheckoutType(),
            checkout.getActualCheckoutDate(),
            checkout.getBreachReason(),
            checkout.getDepositAmount(),
            checkout.getIncomeAmount(),
            checkout.getExpenseAmount(),
            checkout.getFinalAmount(),
            checkout.getDueDate(),
            checkout.getSettlementMethod(),
            checkout.getPaymentStatus(),
            checkout.getStatus(),
            checkout.getApprovalStatus(),
            checkout.getCancelReason(),
            checkout.getCancelBy(),
            checkout.getCancelByName(),
            checkout.getCancelAt(),
            checkout.getRemark(),
            feeList
        );
    }

    @Data
    @AllArgsConstructor
    public static class LeaseCheckoutLogSnapshot {
        private Long id;
        private Long companyId;
        private Long tenantId;
        private Long leaseId;
        private String checkoutCode;
        private Integer checkoutType;
        private Date actualCheckoutDate;
        private String breachReason;
        private BigDecimal depositAmount;
        private BigDecimal incomeAmount;
        private BigDecimal expenseAmount;
        private BigDecimal finalAmount;
        private Date dueDate;
        private Integer settlementMethod;
        private String paymentStatus;
        private Integer status;
        private Integer approvalStatus;
        private String cancelReason;
        private Long cancelBy;
        private String cancelByName;
        private Date cancelAt;
        private String remark;
        private List<LeaseCheckoutFeeSnapshot> feeList;
    }

    @Data
    @AllArgsConstructor
    public static class LeaseCheckoutFeeSnapshot {
        private Long id;
        private String feeDirection;
        private Integer feeType;
        private Long dictDataId;
        private String feeName;
        private BigDecimal feeAmount;
        private Date feeStartDate;
        private Date feeEndDate;
        private String remark;
        private Long leaseBillId;
    }
}
