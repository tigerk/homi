package com.homi.service.service.tenant;

import com.homi.common.lib.enums.payment.PayStatusEnum;
import com.homi.common.lib.enums.tenant.LeaseBillTypeEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.tenant.dto.LeaseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositCarryOverService {
    private final LeaseBillRepo tenantBillRepo;

    /**
     * 押金结转：从旧租约结转押金到新租约
     */
    @Transactional(rollbackFor = Exception.class)
    public void carryOverDeposit(Long oldLeaseId, Long newLeaseId, Long tenantId, LeaseDTO newLease) {
        List<LeaseBill> oldDepositBills = tenantBillRepo.lambdaQuery()
            .eq(LeaseBill::getLeaseId, oldLeaseId)
            .eq(LeaseBill::getBillType, LeaseBillTypeEnum.DEPOSIT.getCode())
            .eq(LeaseBill::getPayStatus, PayStatusEnum.PAID.getCode())
            .eq(LeaseBill::getValid, true)
            .list();

        if (oldDepositBills.isEmpty()) {
            log.info("旧租约 {} 无已支付押金，跳过结转", oldLeaseId);
            return;
        }

        BigDecimal oldDepositTotal = oldDepositBills.stream()
            .map(LeaseBill::getDepositAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newDepositTotal = newLease.getRentPrice()
            .multiply(BigDecimal.valueOf(newLease.getDepositMonths()))
            .setScale(2, RoundingMode.HALF_UP);

        LeaseBill carryOutBill = new LeaseBill();
        carryOutBill.setTenantId(tenantId);
        carryOutBill.setLeaseId(oldLeaseId);
        carryOutBill.setCompanyId(newLease.getCompanyId());
        carryOutBill.setSortOrder(999);
        carryOutBill.setBillType(LeaseBillTypeEnum.DEPOSIT_CARRY_OUT.getCode());
        carryOutBill.setRentPeriodStart(newLease.getLeaseStart());
        carryOutBill.setRentPeriodEnd(newLease.getLeaseEnd());
        carryOutBill.setDepositAmount(oldDepositTotal.negate());
        carryOutBill.setRentalAmount(BigDecimal.ZERO);
        carryOutBill.setOtherFeeAmount(BigDecimal.ZERO);
        carryOutBill.setTotalAmount(oldDepositTotal.negate());
        carryOutBill.setDueDate(new Date());
        carryOutBill.setPayStatus(PayStatusEnum.PAID.getCode());
        carryOutBill.setPayTime(new Date());
        carryOutBill.setPayAmount(oldDepositTotal.negate());
        carryOutBill.setRemark("押金结转至新租约");
        carryOutBill.setValid(true);
        carryOutBill.setDeleted(false);
        carryOutBill.setCreateBy(newLease.getCreateBy());
        carryOutBill.setCreateTime(new Date());
        tenantBillRepo.save(carryOutBill);

        LeaseBill carryInBill = new LeaseBill();
        carryInBill.setTenantId(tenantId);
        carryInBill.setLeaseId(newLeaseId);
        carryInBill.setCompanyId(newLease.getCompanyId());
        carryInBill.setSortOrder(0);
        carryInBill.setBillType(LeaseBillTypeEnum.DEPOSIT_CARRY_IN.getCode());
        carryInBill.setRentPeriodStart(newLease.getLeaseStart());
        carryInBill.setRentPeriodEnd(newLease.getLeaseEnd());
        carryInBill.setDepositAmount(oldDepositTotal);
        carryInBill.setRentalAmount(BigDecimal.ZERO);
        carryInBill.setOtherFeeAmount(BigDecimal.ZERO);
        carryInBill.setTotalAmount(oldDepositTotal);
        carryInBill.setDueDate(new Date());
        carryInBill.setPayStatus(PayStatusEnum.PAID.getCode());
        carryInBill.setPayTime(new Date());
        carryInBill.setPayAmount(oldDepositTotal);
        carryInBill.setCarryOverFromBillId(oldDepositBills.get(0).getId());
        carryInBill.setRemark("从旧租约结转押金");
        carryInBill.setValid(true);
        carryInBill.setDeleted(false);
        carryInBill.setCreateBy(newLease.getCreateBy());
        carryInBill.setCreateTime(new Date());
        tenantBillRepo.save(carryInBill);

        carryOutBill.setCarryOverToBillId(carryInBill.getId());
        tenantBillRepo.updateById(carryOutBill);
        carryInBill.setCarryOverFromBillId(carryOutBill.getId());
        tenantBillRepo.updateById(carryInBill);

        BigDecimal diff = newDepositTotal.subtract(oldDepositTotal);
        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            LeaseBill supplementBill = new LeaseBill();
            supplementBill.setTenantId(tenantId);
            supplementBill.setLeaseId(newLeaseId);
            supplementBill.setCompanyId(newLease.getCompanyId());
            supplementBill.setSortOrder(0);
            supplementBill.setBillType(LeaseBillTypeEnum.DEPOSIT.getCode());
            supplementBill.setRentPeriodStart(newLease.getLeaseStart());
            supplementBill.setRentPeriodEnd(newLease.getLeaseEnd());
            supplementBill.setDepositAmount(diff);
            supplementBill.setRentalAmount(BigDecimal.ZERO);
            supplementBill.setOtherFeeAmount(BigDecimal.ZERO);
            supplementBill.setTotalAmount(diff);
            supplementBill.setDueDate(new Date());
            supplementBill.setPayStatus(PayStatusEnum.UNPAID.getCode());
            supplementBill.setRemark("续签押金补缴（差额）");
            supplementBill.setValid(true);
            supplementBill.setDeleted(false);
            supplementBill.setCreateBy(newLease.getCreateBy());
            supplementBill.setCreateTime(new Date());
            tenantBillRepo.save(supplementBill);

            log.info("新租约 {} 需补缴押金差额：{}", newLeaseId, diff);
        } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
            log.info("新租约 {} 押金减少 {}，将在退租时退还", newLeaseId, diff.abs());
        }

        log.info("押金结转完成：旧租约={}, 新租约={}, 结转金额={}",
            oldLeaseId, newLeaseId, oldDepositTotal);
    }
}
