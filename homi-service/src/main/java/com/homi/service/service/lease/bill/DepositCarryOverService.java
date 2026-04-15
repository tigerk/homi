package com.homi.service.service.lease.bill;

import com.homi.common.lib.enums.pay.PayStatusEnum;
import com.homi.common.lib.enums.lease.LeaseBillFeeTypeEnum;
import com.homi.common.lib.enums.lease.LeaseBillStatusEnum;
import com.homi.common.lib.enums.lease.LeaseBillTypeEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.repo.LeaseBillFeeRepo;
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
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositCarryOverService {
    private final LeaseBillRepo tenantBillRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;

    /**
     * 押金结转：从旧租约结转押金到新租约
     */
    @Transactional(rollbackFor = Exception.class)
    public void carryOverDeposit(Long oldLeaseId, Long newLeaseId, Long tenantId, LeaseDTO newLease) {
        List<LeaseBill> oldDepositBills = tenantBillRepo.lambdaQuery()
            .eq(LeaseBill::getLeaseId, oldLeaseId)
            .eq(LeaseBill::getBillType, LeaseBillTypeEnum.DEPOSIT.getCode())
            .eq(LeaseBill::getPayStatus, PayStatusEnum.PAID.getCode())
            .eq(LeaseBill::getHistorical, false)
            .eq(LeaseBill::getStatus, LeaseBillStatusEnum.NORMAL.getCode())
            .list();

        if (oldDepositBills.isEmpty()) {
            log.info("旧租约 {} 无已支付押金，跳过结转", oldLeaseId);
            return;
        }

        BigDecimal oldDepositTotal = oldDepositBills.stream()
            .map(LeaseBill::getTotalAmount)
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
        carryOutBill.setBillStart(newLease.getLeaseStart());
        carryOutBill.setBillEnd(newLease.getLeaseEnd());
        carryOutBill.setTotalAmount(oldDepositTotal.negate());
        carryOutBill.setPaidAmount(oldDepositTotal.negate());
        carryOutBill.setUnpaidAmount(BigDecimal.ZERO);
        carryOutBill.setDueDate(new Date());
        carryOutBill.setPayStatus(PayStatusEnum.PAID.getCode());
        carryOutBill.setRemark("押金结转至新租约");
        carryOutBill.setStatus(LeaseBillStatusEnum.NORMAL.getCode());
        carryOutBill.setHistorical(false);
        carryOutBill.setDeleted(false);
        carryOutBill.setCreateBy(newLease.getCreateBy());
        carryOutBill.setCreateAt(new Date());
        tenantBillRepo.save(carryOutBill);
        saveDepositFee(carryOutBill, oldDepositTotal.negate(), newLease.getCreateBy());

        LeaseBill carryInBill = new LeaseBill();
        carryInBill.setTenantId(tenantId);
        carryInBill.setLeaseId(newLeaseId);
        carryInBill.setCompanyId(newLease.getCompanyId());
        carryInBill.setSortOrder(0);
        carryInBill.setBillType(LeaseBillTypeEnum.DEPOSIT_CARRY_IN.getCode());
        carryInBill.setBillStart(newLease.getLeaseStart());
        carryInBill.setBillEnd(newLease.getLeaseEnd());
        carryInBill.setTotalAmount(oldDepositTotal);
        carryInBill.setPaidAmount(oldDepositTotal);
        carryInBill.setUnpaidAmount(BigDecimal.ZERO);
        carryInBill.setDueDate(new Date());
        carryInBill.setPayStatus(PayStatusEnum.PAID.getCode());
        carryInBill.setCarryOverFromBillId(oldDepositBills.get(0).getId());
        carryInBill.setRemark("从旧租约结转押金");
        carryInBill.setStatus(LeaseBillStatusEnum.NORMAL.getCode());
        carryInBill.setHistorical(false);
        carryInBill.setDeleted(false);
        carryInBill.setCreateBy(newLease.getCreateBy());
        carryInBill.setCreateAt(new Date());
        tenantBillRepo.save(carryInBill);
        saveDepositFee(carryInBill, oldDepositTotal, newLease.getCreateBy());

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
            supplementBill.setBillStart(newLease.getLeaseStart());
            supplementBill.setBillEnd(newLease.getLeaseEnd());
            supplementBill.setTotalAmount(diff);
            supplementBill.setPaidAmount(BigDecimal.ZERO);
            supplementBill.setUnpaidAmount(diff);
            supplementBill.setDueDate(new Date());
            supplementBill.setPayStatus(PayStatusEnum.UNPAID.getCode());
            supplementBill.setRemark("续签押金补缴（差额）");
            supplementBill.setStatus(LeaseBillStatusEnum.NORMAL.getCode());
            supplementBill.setHistorical(false);
            supplementBill.setDeleted(false);
            supplementBill.setCreateBy(newLease.getCreateBy());
            supplementBill.setCreateAt(new Date());
            tenantBillRepo.save(supplementBill);
            saveDepositFee(supplementBill, diff, newLease.getCreateBy());

            log.info("新租约 {} 需补缴押金差额：{}", newLeaseId, diff);
        } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
            log.info("新租约 {} 押金减少 {}，将在退租时退还", newLeaseId, diff.abs());
        }

        log.info("押金结转完成：旧租约={}, 新租约={}, 结转金额={}",
            oldLeaseId, newLeaseId, oldDepositTotal);
    }

    private void saveDepositFee(LeaseBill bill, BigDecimal amount, Long operatorId) {
        if (bill == null) {
            return;
        }
        LeaseBillFee fee = new LeaseBillFee();
        fee.setBillId(bill.getId());
        fee.setFeeType(LeaseBillFeeTypeEnum.DEPOSIT.getCode());
        fee.setFeeName("押金");
        fee.setAmount(amount);
        fee.setPaidAmount(Objects.equals(bill.getPayStatus(), PayStatusEnum.PAID.getCode()) ? amount : BigDecimal.ZERO);
        fee.setUnpaidAmount(Objects.equals(bill.getPayStatus(), PayStatusEnum.PAID.getCode()) ? BigDecimal.ZERO : amount);
        fee.setPayStatus(bill.getPayStatus());
        fee.setFeeStart(bill.getBillStart());
        fee.setFeeEnd(bill.getBillEnd());
        fee.setRemark(bill.getRemark());
        fee.setDeleted(false);
        fee.setCreateBy(operatorId);
        fee.setCreateAt(new Date());
        fee.setUpdateBy(operatorId);
        fee.setUpdateAt(new Date());
        leaseBillFeeRepo.save(fee);
    }

}
