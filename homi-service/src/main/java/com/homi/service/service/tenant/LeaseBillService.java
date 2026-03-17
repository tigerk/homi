package com.homi.service.service.tenant;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.IdTypeEnum;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.enums.pay.PayStatusEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.Lease;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.entity.LeaseRoom;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantCompany;
import com.homi.model.dao.entity.TenantPersonal;
import com.homi.model.dao.repo.LeaseBillFeeRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.dao.repo.LeaseRepo;
import com.homi.model.dao.repo.LeaseRoomRepo;
import com.homi.model.dao.repo.TenantCompanyRepo;
import com.homi.model.dao.repo.TenantPersonalRepo;
import com.homi.model.dao.repo.TenantRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import com.homi.model.tenant.dto.LeaseBillFeeDTO;
import com.homi.model.tenant.dto.LeaseBillUpdateDTO;
import com.homi.model.tenant.vo.bill.FinanceFlowVO;
import com.homi.model.tenant.vo.bill.LeaseBillListVO;
import com.homi.model.tenant.vo.bill.LeaseBillFeeVO;
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

    /**
     * 根据租约ID查询账单列表
     *
     * @param leaseId 租约ID
     * @param valid   是否有效
     * @return 账单列表VO
     */
    public List<LeaseBillListVO> getBillListByLeaseId(Long leaseId, Boolean valid) {
        List<LeaseBill> leaseBillList = leaseBillRepo.getBillListByLeaseId(leaseId, valid);

        if (leaseBillList.isEmpty()) {
            return List.of();
        }

        // 1. 收集所有 billId
        List<Long> billIds = leaseBillList.stream().map(LeaseBill::getId).toList();

        // 2. 一次性查询所有费用明细
        List<LeaseBillFee> allFees = leaseBillFeeRepo.getFeesByBillIds(billIds);

        // 3. 按 billId 分组
        Map<Long, List<LeaseBillFeeVO>> feeMap = allFees.stream()
            .collect(Collectors.groupingBy(
                LeaseBillFee::getBillId,
                Collectors.mapping(of -> BeanCopyUtils.copyBean(of, LeaseBillFeeVO.class), Collectors.toList())
            ));

        // 4. 组装结果
        return leaseBillList.stream().map(tb -> {
            LeaseBillListVO vo = BeanCopyUtils.copyBean(tb, LeaseBillListVO.class);
            assert vo != null;
            vo.setFeeList(feeMap.getOrDefault(tb.getId(), List.of()));
            return vo;
        }).toList();
    }

    /**
     * 根据账单ID查询账单详情（包含其他费用）
     */
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
            .map(of -> BeanCopyUtils.copyBean(of, LeaseBillFeeVO.class))
            .toList();
        vo.setFeeList(feeVos);
        attachBillContext(vo, bill);
        attachFinanceFlow(vo, bill);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateBill(LeaseBillUpdateDTO dto, Long operatorId) {
        if (dto == null || dto.getId() == null) {
            return false;
        }
        LeaseBill bill = leaseBillRepo.getById(dto.getId());
        if (bill == null) {
            return false;
        }
        if (Objects.equals(bill.getPayStatus(), PayStatusEnum.PAID.getCode())) {
            return false;
        }

        DateTime now = DateUtil.date();

        // 忽略 null，不覆盖已有字段
        BeanUtil.copyProperties(dto, bill, CopyOptions.create().setIgnoreNullValue(true));
        if (dto.getValid() != null) {
            bill.setValid(dto.getValid());
        }
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        leaseBillRepo.updateById(bill);

        if (dto.getFeeList() != null) {
            leaseBillFeeRepo.removeByBillId(bill.getId());
            List<LeaseBillFee> toSave = new ArrayList<>();
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (LeaseBillFeeDTO fee : dto.getFeeList()) {
                LeaseBillFee entity = BeanCopyUtils.copyBean(fee, LeaseBillFee.class);
                assert entity != null;
                entity.setBillId(bill.getId());
                entity.setCreateBy(operatorId);
                entity.setUpdateBy(operatorId);
                entity.setCreateTime(now);
                entity.setUpdateTime(now);
                toSave.add(entity);
                if (fee.getAmount() != null) {
                    total = total.add(fee.getAmount());
                }
            }
            if (!toSave.isEmpty()) {
                leaseBillFeeRepo.saveBatch(toSave);
            }
            bill.setTotalAmount(total);
            leaseBillRepo.updateById(bill);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean collectBill(LeaseBillCollectDTO dto) {
        if (dto == null || dto.getId() == null || dto.getUpdateBy() == null) {
            return false;
        }
        LeaseBill bill = leaseBillRepo.getById(dto.getId());
        if (bill == null) {
            return false;
        }

        DateTime now = DateUtil.date();
        Integer targetPayStatus = dto.getPayStatus();
        BigDecimal payAmount = dto.getPayAmount();

        if (targetPayStatus == null) {
            targetPayStatus = bill.getPayStatus();
        }
        if (!isAllowedPayStatus(targetPayStatus)) {
            return false;
        }

        Integer finalPayStatus = resolveFinalPayStatus(payAmount, bill.getTotalAmount());

        bill.setPayStatus(finalPayStatus);
        bill.setPayAmount(payAmount);
        bill.setPayChannel(dto.getPayChannel());
        bill.setPayTime(dto.getPayTime());
        bill.setUpdateBy(dto.getUpdateBy());
        bill.setUpdateTime(now);
        leaseBillRepo.updateById(bill);

        if (!Objects.equals(finalPayStatus, PayStatusEnum.PAID.getCode()) || payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }

        if (paymentFlowService.getLatestByBiz(PaymentFlowBizTypeEnum.LEASE_BILL.getCode(), bill.getId()) != null) {
            return true;
        }

        Tenant tenant = bill.getTenantId() == null ? null : tenantRepo.getById(bill.getTenantId());
        String payerName = resolvePayerName(tenant);
        String payerPhone = resolvePayerPhone(tenant);
        String operatorName = userRepo.getUserNicknameById(dto.getUpdateBy());
        Date payTime = dto.getPayTime() != null ? dto.getPayTime() : now;

        String billSummary = buildBillSummary(bill);
        var paymentFlow = paymentFlowService.createLeaseBillPaymentFlow(
            PaymentFlowService.CreateCommand.builder()
                .bill(bill)
                .amount(payAmount)
                .payChannel(bill.getPayChannel())
                .payTime(payTime)
                .operatorId(dto.getUpdateBy())
                .operatorName(operatorName)
                .payerName(payerName)
                .payerPhone(payerPhone)
                .remark(billSummary)
                .now(now)
                .build()
        );

        List<LeaseBillFee> feeList = leaseBillFeeRepo.getFeesByBillId(bill.getId());
        financeFlowService.createLeaseBillReceiveFlows(
            FinanceFlowService.CreateCommand.builder()
                .bill(bill)
                .paymentFlow(paymentFlow)
                .feeList(feeList)
                .amount(payAmount)
                .payTime(payTime)
                .operatorId(dto.getUpdateBy())
                .operatorName(operatorName)
                .payerName(payerName)
                .payerPhone(payerPhone)
                .remark(billSummary)
                .now(now)
                .build()
        );
        return true;
    }

    /**
     * 关联账单的财务流（包括支付流）
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/15 16:55
     *
     * @param vo   参数说明
     * @param bill 参数说明
     */
    private void attachFinanceFlow(LeaseBillListVO vo, LeaseBill bill) {
        if (vo == null || bill == null || bill.getCompanyId() == null) {
            return;
        }

        List<FinanceFlow> financeFlows = financeFlowService.getListByBiz(
            FinanceBizTypeEnum.LEASE_BILL.getCode(),
            bill.getId()
        );
        List<FinanceFlowVO> flowVos = financeFlows.stream()
            .map(flow -> {
                FinanceFlowVO flowVo = new FinanceFlowVO();
                BeanUtils.copyProperties(flow, flowVo);
                return flowVo;
            })
            .toList();
        vo.setFinanceFlowList(flowVos);

        var paymentFlow = paymentFlowService.getLatestByBiz(PaymentFlowBizTypeEnum.LEASE_BILL.getCode(), bill.getId());
        if (paymentFlow == null) {
            return;
        }
        PaymentFlowVO paymentFlowVO = new PaymentFlowVO();
        BeanUtils.copyProperties(paymentFlow, paymentFlowVO);
        vo.setPaymentFlow(paymentFlowVO);
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
        vo.setPayerName(tenant.getTenantName());
        vo.setPayerPhone(tenant.getTenantPhone());

        if (TenantTypeEnum.PERSONAL.getCode().equals(tenant.getTenantType())) {
            TenantPersonal personal = tenant.getTenantTypeId() == null ? null : tenantPersonalRepo.getById(tenant.getTenantTypeId());
            if (personal == null) {
                return;
            }
            vo.setPayerName(personal.getName());
            vo.setPayerPhone(personal.getPhone());
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
            vo.setPayerName(company.getContactName() != null ? company.getContactName() : company.getCompanyName());
            vo.setPayerPhone(company.getContactPhone());
            vo.setPayerIdType(company.getLegalPersonIdType());
            vo.setPayerIdTypeName(getIdTypeName(company.getLegalPersonIdType()));
            vo.setPayerIdNo(company.getLegalPersonIdNo());
        }
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

    private String resolvePayerName(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        if (TenantTypeEnum.PERSONAL.getCode().equals(tenant.getTenantType())) {
            TenantPersonal personal = tenant.getTenantTypeId() == null ? null : tenantPersonalRepo.getById(tenant.getTenantTypeId());
            return personal != null ? personal.getName() : tenant.getTenantName();
        }
        if (TenantTypeEnum.ENTERPRISE.getCode().equals(tenant.getTenantType())) {
            TenantCompany company = tenant.getTenantTypeId() == null ? null : tenantCompanyRepo.getById(tenant.getTenantTypeId());
            if (company == null) {
                return tenant.getTenantName();
            }
            return company.getContactName() != null ? company.getContactName() : company.getCompanyName();
        }
        return tenant.getTenantName();
    }

    private String resolvePayerPhone(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        if (TenantTypeEnum.PERSONAL.getCode().equals(tenant.getTenantType())) {
            TenantPersonal personal = tenant.getTenantTypeId() == null ? null : tenantPersonalRepo.getById(tenant.getTenantTypeId());
            return personal != null ? personal.getPhone() : tenant.getTenantPhone();
        }
        if (TenantTypeEnum.ENTERPRISE.getCode().equals(tenant.getTenantType())) {
            TenantCompany company = tenant.getTenantTypeId() == null ? null : tenantCompanyRepo.getById(tenant.getTenantTypeId());
            return company != null ? company.getContactPhone() : tenant.getTenantPhone();
        }
        return tenant.getTenantPhone();
    }

    private boolean isAllowedPayStatus(Integer payStatus) {
        return Objects.equals(payStatus, PayStatusEnum.UNPAID.getCode())
            || Objects.equals(payStatus, PayStatusEnum.PARTIALLY_PAID.getCode());
    }

    private Integer resolveFinalPayStatus(BigDecimal payAmount, BigDecimal totalAmount) {
        if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return PayStatusEnum.UNPAID.getCode();
        }
        if (totalAmount != null && payAmount.compareTo(totalAmount) >= 0) {
            return PayStatusEnum.PAID.getCode();
        }
        return PayStatusEnum.PARTIALLY_PAID.getCode();
    }
}
