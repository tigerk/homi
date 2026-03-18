package com.homi.service.service.tenant;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.IdTypeEnum;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.enums.pay.PayStatusEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import com.homi.model.tenant.dto.LeaseBillFeeDTO;
import com.homi.model.tenant.dto.LeaseBillUpdateDTO;
import com.homi.model.tenant.vo.bill.FinanceFlowVO;
import com.homi.model.tenant.vo.bill.LeaseBillFeeVO;
import com.homi.model.tenant.vo.bill.LeaseBillListVO;
import com.homi.model.tenant.vo.bill.PaymentFlowVO;
import com.homi.service.service.finance.FinanceFlowService;
import com.homi.service.service.finance.PaymentFlowService;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaseBillService {
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;
    private final LeaseRepo leaseRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final TenantRepo tenantRepo;
    private final TenantPersonalRepo tenantPersonalRepo;
    private final TenantCompanyRepo tenantCompanyRepo;
    private final UserRepo userRepo;
    private final FinanceFlowService financeFlowService;
    private final PaymentFlowService paymentFlowService;
    private final RoomService roomService;

    public List<LeaseBillListVO> getBillListByLeaseId(Long leaseId, Boolean valid) {
        List<LeaseBill> leaseBillList = leaseBillRepo.getBillListByLeaseId(leaseId, valid);
        if (leaseBillList.isEmpty()) {
            return List.of();
        }

        List<Long> billIds = leaseBillList.stream().map(LeaseBill::getId).toList();
        List<LeaseBillFee> allFees = leaseBillFeeRepo.getFeesByBillIds(billIds);

        Map<Long, List<LeaseBillFeeVO>> feeMap = allFees.stream()
            .collect(Collectors.groupingBy(
                LeaseBillFee::getBillId,
                Collectors.mapping(item -> BeanCopyUtils.copyBean(item, LeaseBillFeeVO.class), Collectors.toList())
            ));

        return leaseBillList.stream().map(tb -> {
            LeaseBillListVO vo = BeanCopyUtils.copyBean(tb, LeaseBillListVO.class);
            assert vo != null;
            vo.setFeeList(feeMap.getOrDefault(tb.getId(), List.of()));
            return vo;
        }).toList();
    }

    public LeaseBillListVO getBillDetailById(Long billId) {
        if (billId == null) {
            return null;
        }

        LeaseBill bill = leaseBillRepo.getById(billId);
        if (bill == null) {
            return null;
        }
        LeaseBillListVO vo = BeanCopyUtils.copyBean(bill, LeaseBillListVO.class);
        if (vo == null) {
            return null;
        }

        List<LeaseBillFee> fees = leaseBillFeeRepo.getFeesByBillId(billId);
        List<LeaseBillFeeVO> feeVos = fees.stream()
            .map(item -> BeanCopyUtils.copyBean(item, LeaseBillFeeVO.class))
            .toList();
        vo.setFeeList(feeVos);
        attachBillContext(vo, bill);
        attachFinanceFlow(vo);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateBill(LeaseBillUpdateDTO dto, Long operatorId) {
        if (dto == null || dto.getId() == null) {
            return false;
        }
        LeaseBill bill = leaseBillRepo.getByIdForUpdate(dto.getId());
        if (bill == null || Objects.equals(bill.getPayStatus(), PayStatusEnum.PAID.getCode())) {
            return false;
        }

        DateTime now = DateUtil.date();
        BeanUtil.copyProperties(dto, bill, CopyOptions.create().setIgnoreNullValue(true));
        boolean billChanged = true;
        if (dto.getValid() != null) {
            bill.setValid(dto.getValid());
        }

        if (dto.getFeeList() != null) {
            List<LeaseBillFee> existFees = leaseBillFeeRepo.getFeesByBillIdForUpdate(bill.getId());
            Map<Long, LeaseBillFee> existFeeMap = existFees.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(LeaseBillFee::getId, item -> item));
            Map<Long, LeaseBillFeeDTO> incomingFeeMap = dto.getFeeList().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(LeaseBillFeeDTO::getId, item -> item, (left, right) -> right));

            List<Long> removedIds = existFees.stream()
                .map(LeaseBillFee::getId)
                .filter(Objects::nonNull)
                .filter(id -> !incomingFeeMap.containsKey(id))
                .toList();
            validateRemovableFees(removedIds, existFeeMap);

            List<LeaseBillFee> toCreate = new ArrayList<>();
            List<LeaseBillFee> toUpdate = new ArrayList<>();
            for (LeaseBillFeeDTO fee : dto.getFeeList()) {
                LeaseBillFee entity = fee.getId() == null ? new LeaseBillFee() : existFeeMap.get(fee.getId());
                if (entity == null) {
                    return false;
                }
                boolean isNew = entity.getId() == null;
                BigDecimal amount = ObjectUtil.defaultIfNull(fee.getAmount(), BigDecimal.ZERO);
                BigDecimal currentPaidAmount = isNew ? BigDecimal.ZERO : ObjectUtil.defaultIfNull(entity.getPaidAmount(), BigDecimal.ZERO);
                if (currentPaidAmount.compareTo(amount) > 0) {
                    return false;
                }
                entity.setBillId(bill.getId());
                entity.setFeeType(fee.getFeeType());
                entity.setDictDataId(fee.getDictDataId());
                entity.setFeeName(fee.getFeeName());
                entity.setAmount(amount);
                entity.setPaidAmount(currentPaidAmount);
                entity.setUnpaidAmount(amount.subtract(currentPaidAmount));
                entity.setPayStatus(resolvePayStatus(currentPaidAmount, amount));
                entity.setFeeStart(fee.getFeeStart());
                entity.setFeeEnd(fee.getFeeEnd());
                entity.setRemark(fee.getRemark());
                entity.setUpdateBy(operatorId);
                entity.setUpdateTime(now);
                if (isNew) {
                    entity.setCreateBy(operatorId);
                    entity.setCreateTime(now);
                    toCreate.add(entity);
                } else {
                    toUpdate.add(entity);
                }
            }
            if (!removedIds.isEmpty()) {
                leaseBillFeeRepo.removeByIds(removedIds);
            }
            if (!toUpdate.isEmpty()) {
                leaseBillFeeRepo.updateBatchById(toUpdate);
            }
            if (!toCreate.isEmpty()) {
                leaseBillFeeRepo.saveBatch(toCreate);
            }
            recalculateBillAmounts(bill, operatorId, now);
            billChanged = false;
        }

        if (billChanged) {
            bill.setUpdateBy(operatorId);
            bill.setUpdateTime(now);
            leaseBillRepo.updateById(bill);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean collectBill(LeaseBillCollectDTO dto) {
        if (dto == null || dto.getId() == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            return false;
        }
        LeaseBill bill = leaseBillRepo.getByIdForUpdate(dto.getId());
        if (bill == null) {
            return false;
        }

        List<Long> feeIds = dto.getItems().stream()
            .map(LeaseBillCollectDTO.Item::getLeaseBillFeeId)
            .filter(Objects::nonNull)
            .toList();
        if (feeIds.isEmpty()) {
            return false;
        }

        List<LeaseBillFee> feeList = leaseBillFeeRepo.getByIdsForUpdate(feeIds);
        Map<Long, LeaseBillFee> feeMap = feeList.stream().collect(Collectors.toMap(LeaseBillFee::getId, item -> item));
        if (!validateCollectItems(dto, bill, feeMap)) {
            return false;
        }

        DateTime now = DateUtil.date();
        Tenant tenant = tenantRepo.getById(bill.getTenantId());
        PayerContext payerContext = buildPayerContext(tenant);
        String operatorName = userRepo.getUserNicknameById(dto.getUpdateBy());
        Date payTime = dto.getPayTime() != null ? dto.getPayTime() : now;
        String billSummary = buildBillSummary(bill);

        PaymentFlow paymentFlow = paymentFlowService.createLeaseBillPaymentFlow(
            PaymentFlowService.CreateCommand.builder()
                .bill(bill)
                .totalAmount(dto.getTotalAmount())
                .payChannel(dto.getPayChannel())
                .thirdTradeNo(dto.getThirdTradeNo())
                .paymentVoucherUrl(dto.getPaymentVoucherUrl())
                .payTime(payTime)
                .operatorId(dto.getUpdateBy())
                .operatorName(operatorName)
                .payerName(payerContext.payerName())
                .payerPhone(payerContext.payerPhone())
                .remark(CharSequenceUtil.blankToDefault(dto.getPayRemark(), billSummary))
                .now(now)
                .build()
        );

        financeFlowService.createLeaseBillReceiveFlows(
            FinanceFlowService.CreateCommand.builder()
                .paymentFlow(paymentFlow)
                .feeMap(feeMap)
                .items(dto.getItems())
                .payTime(payTime)
                .operatorId(dto.getUpdateBy())
                .operatorName(operatorName)
                .payerName(payerContext.payerName())
                .payerPhone(payerContext.payerPhone())
                .remark(billSummary)
                .now(now)
                .build()
        );

        applyCollectToFees(feeMap, dto.getItems(), dto.getUpdateBy(), now);
        recalculateBillAmounts(bill, dto.getUpdateBy(), now);
        return true;
    }

    private void attachFinanceFlow(LeaseBillListVO vo) {
        if (vo == null || vo.getFeeList() == null || vo.getFeeList().isEmpty()) {
            vo.setFinanceFlowList(List.of());
            vo.setPaymentFlowList(List.of());
            return;
        }

        List<Long> feeIds = vo.getFeeList().stream().map(LeaseBillFeeVO::getId).filter(Objects::nonNull).toList();
        List<FinanceFlow> financeFlows = financeFlowService.getListByBizIds(FinanceBizTypeEnum.LEASE_BILL_FEE.getCode(), feeIds);
        vo.setFinanceFlowList(financeFlows.stream().map(flow -> {
            FinanceFlowVO flowVo = new FinanceFlowVO();
            BeanUtils.copyProperties(flow, flowVo);
            return flowVo;
        }).toList());

        vo.setPaymentFlowList(paymentFlowService.listByBiz(PaymentFlowBizTypeEnum.LEASE_BILL.getCode(), vo.getId()).stream().map(item -> {
            PaymentFlowVO paymentFlowVO = new PaymentFlowVO();
            BeanUtils.copyProperties(item, paymentFlowVO);
            return paymentFlowVO;
        }).toList());
    }

    private void attachBillContext(LeaseBillListVO vo, LeaseBill bill) {
        if (vo == null || bill == null) {
            return;
        }

        Lease lease = bill.getLeaseId() == null ? null : leaseRepo.getById(bill.getLeaseId());
        if (lease != null) {
            List<Long> roomIds = leaseRoomRepo.getListByLeaseId(lease.getId()).stream()
                .map(LeaseRoom::getRoomId)
                .filter(Objects::nonNull)
                .toList();
            if (!roomIds.isEmpty()) {
                vo.setRoomAddress(roomService.getRoomAddressByIds(roomIds));
            } else if (lease.getRoomIds() != null) {
                List<Long> leaseRoomIds = JSONUtil.toList(lease.getRoomIds(), Long.class);
                if (!leaseRoomIds.isEmpty()) {
                    vo.setRoomAddress(roomService.getRoomAddressByIds(leaseRoomIds));
                }
            }
        }

        Tenant tenant = bill.getTenantId() == null ? null : tenantRepo.getById(bill.getTenantId());
        if (tenant == null) {
            return;
        }
        PayerContext payerContext = buildPayerContext(tenant);
        vo.setPayerName(payerContext.payerName());
        vo.setPayerPhone(payerContext.payerPhone());

        if (TenantTypeEnum.PERSONAL.getCode().equals(tenant.getTenantType())) {
            TenantPersonal personal = tenant.getTenantTypeId() == null ? null : tenantPersonalRepo.getById(tenant.getTenantTypeId());
            if (personal == null) {
                return;
            }
            vo.setPayerIdType(personal.getIdType());
            vo.setPayerIdTypeName(getIdTypeName(personal.getIdType()));
            vo.setPayerIdNo(personal.getIdNo());
            return;
        }

        if (TenantTypeEnum.ENTERPRISE.getCode().equals(tenant.getTenantType())) {
            TenantCompany company = tenant.getTenantTypeId() == null ? null : tenantCompanyRepo.getById(tenant.getTenantTypeId());
            if (company == null) {
                return;
            }
            vo.setPayerIdType(company.getLegalPersonIdType());
            vo.setPayerIdTypeName(getIdTypeName(company.getLegalPersonIdType()));
            vo.setPayerIdNo(company.getLegalPersonIdNo());
        }
    }

    private boolean validateCollectItems(LeaseBillCollectDTO dto, LeaseBill bill, Map<Long, LeaseBillFee> feeMap) {
        BigDecimal totalAmount = ObjectUtil.defaultIfNull(dto.getTotalAmount(), BigDecimal.ZERO);
        BigDecimal allocatedAmount = BigDecimal.ZERO;
        Set<Long> duplicateGuard = new HashSet<>();
        for (LeaseBillCollectDTO.Item item : dto.getItems()) {
            if (item == null || item.getLeaseBillFeeId() == null) {
                return false;
            }
            if (!duplicateGuard.add(item.getLeaseBillFeeId())) {
                return false;
            }
            LeaseBillFee fee = feeMap.get(item.getLeaseBillFeeId());
            if (fee == null || !Objects.equals(fee.getBillId(), bill.getId())) {
                return false;
            }
            BigDecimal amount = ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO);
            if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(ObjectUtil.defaultIfNull(fee.getUnpaidAmount(), BigDecimal.ZERO)) > 0) {
                return false;
            }
            allocatedAmount = allocatedAmount.add(amount);
        }
        return allocatedAmount.compareTo(totalAmount) == 0;
    }

    private void applyCollectToFees(Map<Long, LeaseBillFee> feeMap, List<LeaseBillCollectDTO.Item> items, Long operatorId, DateTime now) {
        for (LeaseBillCollectDTO.Item item : items) {
            LeaseBillFee fee = feeMap.get(item.getLeaseBillFeeId());
            BigDecimal nextPaidAmount = ObjectUtil.defaultIfNull(fee.getPaidAmount(), BigDecimal.ZERO).add(ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO));
            BigDecimal totalAmount = ObjectUtil.defaultIfNull(fee.getAmount(), BigDecimal.ZERO);
            if (nextPaidAmount.compareTo(totalAmount) > 0) {
                nextPaidAmount = totalAmount;
            }
            fee.setPaidAmount(nextPaidAmount);
            fee.setUnpaidAmount(totalAmount.subtract(nextPaidAmount));
            fee.setPayStatus(resolvePayStatus(nextPaidAmount, totalAmount));
            fee.setUpdateBy(operatorId);
            fee.setUpdateTime(now);
        }
        leaseBillFeeRepo.updateBatchById(new ArrayList<>(feeMap.values()));
    }

    private void recalculateBillAmounts(LeaseBill bill, Long operatorId, DateTime now) {
        List<LeaseBillFee> allFees = leaseBillFeeRepo.getFeesByBillIdForUpdate(bill.getId());
        BigDecimal billTotalAmount = allFees.stream()
            .map(item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal billPaidAmount = allFees.stream()
            .map(item -> ObjectUtil.defaultIfNull(item.getPaidAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        bill.setTotalAmount(billTotalAmount);
        bill.setPaidAmount(billPaidAmount);
        bill.setUnpaidAmount(billTotalAmount.subtract(billPaidAmount));
        bill.setPayStatus(resolvePayStatus(billPaidAmount, billTotalAmount));
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        leaseBillRepo.updateById(bill);
    }

    private void validateRemovableFees(List<Long> removedIds, Map<Long, LeaseBillFee> existFeeMap) {
        if (removedIds.isEmpty()) {
            return;
        }
        boolean hasPaidFee = removedIds.stream()
            .map(existFeeMap::get)
            .filter(Objects::nonNull)
            .anyMatch(item -> ObjectUtil.defaultIfNull(item.getPaidAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0);
        if (hasPaidFee) {
            throw new BizException("已收款的费用项不允许删除");
        }
        if (financeFlowService.existsByBizIds(FinanceBizTypeEnum.LEASE_BILL_FEE.getCode(), removedIds)) {
            throw new BizException("已有财务流水的费用项不允许删除");
        }
    }

    private Integer resolvePayStatus(BigDecimal paidAmount, BigDecimal totalAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return PayStatusEnum.UNPAID.getCode();
        }
        if (totalAmount != null && paidAmount.compareTo(totalAmount) >= 0) {
            return PayStatusEnum.PAID.getCode();
        }
        return PayStatusEnum.PARTIALLY_PAID.getCode();
    }

    private String getIdTypeName(Integer idType) {
        if (idType == null) {
            return null;
        }
        return java.util.Arrays.stream(IdTypeEnum.values())
            .filter(item -> item.getCode().equals(idType))
            .map(IdTypeEnum::getName)
            .findFirst()
            .orElse(null);
    }

    private String buildBillSummary(LeaseBill bill) {
        if (bill == null) {
            return "租客账单收款";
        }
        return "租客账单#" + bill.getId();
    }

    private PayerContext buildPayerContext(Tenant tenant) {
        if (tenant == null) {
            return new PayerContext(null, null);
        }
        if (TenantTypeEnum.PERSONAL.getCode().equals(tenant.getTenantType())) {
            TenantPersonal personal = tenant.getTenantTypeId() == null ? null : tenantPersonalRepo.getById(tenant.getTenantTypeId());
            return new PayerContext(
                personal != null ? personal.getName() : tenant.getTenantName(),
                personal != null ? personal.getPhone() : tenant.getTenantPhone()
            );
        }
        if (TenantTypeEnum.ENTERPRISE.getCode().equals(tenant.getTenantType())) {
            TenantCompany company = tenant.getTenantTypeId() == null ? null : tenantCompanyRepo.getById(tenant.getTenantTypeId());
            if (company == null) {
                return new PayerContext(tenant.getTenantName(), tenant.getTenantPhone());
            }
            return new PayerContext(
                company.getContactName() != null ? company.getContactName() : company.getCompanyName(),
                company.getContactPhone() != null ? company.getContactPhone() : tenant.getTenantPhone()
            );
        }
        return new PayerContext(tenant.getTenantName(), tenant.getTenantPhone());
    }

    private record PayerContext(String payerName, String payerPhone) {
    }
}
