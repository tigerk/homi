package com.homi.service.service.checkout;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.annotation.BizOperateLog;
import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.FeeDirectionEnum;
import com.homi.common.lib.enums.biz.BizOperateBizTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateSourceTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateTypeEnum;
import com.homi.common.lib.enums.checkout.CheckoutPaymentStatusEnum;
import com.homi.common.lib.enums.checkout.CheckoutSettlementMethodEnum;
import com.homi.common.lib.enums.checkout.CheckoutStatusEnum;
import com.homi.common.lib.enums.checkout.CheckoutTypeEnum;
import com.homi.common.lib.enums.lease.LeaseBillTypeEnum;
import com.homi.common.lib.enums.lease.LeaseStatusEnum;
import com.homi.common.lib.enums.room.OccupancyStatusEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.approval.dto.ApprovalSubmitDTO;
import com.homi.model.checkout.dto.LeaseCheckoutDTO;
import com.homi.model.checkout.dto.LeaseCheckoutFeeDTO;
import com.homi.model.checkout.dto.LeaseCheckoutQueryDTO;
import com.homi.model.checkout.vo.LeaseCheckoutInitVO;
import com.homi.model.checkout.vo.LeaseCheckoutFeeVO;
import com.homi.model.checkout.vo.LeaseCheckoutVO;
import com.homi.model.common.dto.OperatorDTO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.tenant.vo.TenantDetailVO;
import com.homi.service.service.approval.ApprovalTemplate;
import com.homi.service.service.room.RoomService;
import com.homi.service.service.sys.UserService;
import com.homi.service.service.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 退租服务（退租并结账）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaseCheckoutService {

    private final LeaseCheckoutRepo leaseCheckoutRepo;
    private final LeaseCheckoutFeeRepo leaseCheckoutFeeRepo;
    private final TenantRepo tenantRepo;
    private final LeaseRepo leaseRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final LeaseBillRepo leaseBillRepo;
    private final RoomService roomService;
    private final UserService userService;
    private final ApprovalTemplate approvalTemplate;
    private final RoomRepo roomRepo;
    private final UserRepo userRepo;

    private final TenantService tenantService;

    /**
     * 获取退租初始化数据
     * 返回合同信息、未付账单、预填费用行
     */
    public LeaseCheckoutInitVO getCheckoutInitData(LeaseCheckoutQueryDTO query) {
        Lease lease = leaseRepo.getById(query.getLeaseId());
        if (lease == null) {
            throw new BizException("租约不存在");
        }

        // 检查是否有进行中的退租单
        if (leaseCheckoutRepo.hasActiveCheckoutByLeaseId(lease.getId())) {
            throw new BizException("该租客已有进行中的退租单");
        }

        // 获取房间信息
        List<Long> roomIds = leaseRoomRepo.getListByLeaseId(lease.getId()).stream().map(LeaseRoom::getRoomId).toList();
        String roomAddress = roomService.getRoomAddressByIds(roomIds);

        // 获取未付账单
        List<LeaseBill> unpaidBills = leaseBillRepo.getBillListByLeaseId(lease.getId(), Boolean.FALSE);
        List<LeaseCheckoutInitVO.UnpaidBillVO> unpaidBillVOs = new ArrayList<>();
        BigDecimal unpaidAmount = BigDecimal.ZERO;

        for (LeaseBill bill : unpaidBills) {
            BigDecimal payAmount = ObjectUtil.defaultIfNull(bill.getPaidAmount(), BigDecimal.ZERO);
            BigDecimal billTotal = ObjectUtil.defaultIfNull(bill.getTotalAmount(), BigDecimal.ZERO);
            BigDecimal unpaidMoney = billTotal.subtract(payAmount);
            unpaidAmount = unpaidAmount.add(unpaidMoney);

            unpaidBillVOs.add(LeaseCheckoutInitVO.UnpaidBillVO.builder()
                .billId(bill.getId())
                .billType(bill.getBillType())
                .billTypeName(getBillTypeName(bill.getBillType()))
                .periodStart(bill.getBillStart())
                .periodEnd(bill.getBillEnd())
                .billPeriod(bill.getBillStart() + " ~ " + bill.getBillEnd())
                .totalAmount(billTotal)
                .payAmount(payAmount)
                .unpaidAmount(unpaidMoney)
                .build());
        }

        // 计算押金总额
        BigDecimal depositAmount = lease.getRentPrice()
            .multiply(BigDecimal.valueOf(ObjectUtil.defaultIfNull(lease.getDepositMonths(), 0)));

        // 构建预填费用行（根据未付账单自动生成）
        List<LeaseCheckoutFeeVO> presetFees = buildPresetFees(lease, unpaidBills, depositAmount);

        TenantDetailVO tenant = tenantService.getTenantDetail(lease.getTenantId());
        if (tenant == null) {
            throw new BizException("租客不存在");
        }

        // 收款人信息
        LeaseCheckoutInitVO.PayeeInfoVO payeeInfo = LeaseCheckoutInitVO.PayeeInfoVO.builder()
            .payeeName(tenant.getTenantName())
            .payeePhone(tenant.getTenantPhone())
            .build();

        if (tenant.getTenantType().equals(TenantTypeEnum.PERSONAL.getCode())) {
            payeeInfo.setPayeeIdType(tenant.getTenantPersonal().getIdType());
            payeeInfo.setPayeeIdNo(tenant.getTenantPersonal().getIdNo());
        }

        return LeaseCheckoutInitVO.builder()
            .tenantId(lease.getTenantId())
            .leaseId(lease.getId())
            .roomAddress(roomAddress)
            .leaseStart(lease.getLeaseStart())
            .leaseEnd(lease.getLeaseEnd())
            .tenantName(tenant.getTenantName())
            .tenantPhone(tenant.getTenantPhone())
            .rentPrice(lease.getRentPrice())
            .depositAmount(depositAmount)
            .depositMonths(lease.getDepositMonths())
            .unpaidBills(unpaidBillVOs)
            .unpaidAmount(unpaidAmount)
            .presetFees(presetFees)
            .payeeInfo(payeeInfo)
            .build();
    }

    /**
     * 构建预填费用行
     * 自动根据押金、未付账单等生成费用清算表初始行
     */
    private List<LeaseCheckoutFeeVO> buildPresetFees(
        Lease lease,
        List<LeaseBill> unpaidBills,
        BigDecimal depositAmount
    ) {
        List<LeaseCheckoutFeeVO> presetFees = new ArrayList<>();
        Date today = DateUtil.date();

        // 1. 押金退还（支出方向）- 默认正常退时预填
        if (depositAmount != null && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            LeaseCheckoutFeeVO presetFee = new LeaseCheckoutFeeVO();
            presetFee.setFeeDirection(FeeDirectionEnum.OUT.getCode());
            presetFee.setFeeType(LeaseBillTypeEnum.DEPOSIT.getCode());
            presetFee.setFeeTypeName(LeaseBillTypeEnum.DEPOSIT.getName());
            presetFee.setFeeName("房屋押金");
            presetFee.setFeeAmount(depositAmount);
            presetFee.setFeeStartDate(lease.getLeaseStart());
            presetFee.setFeeEndDate(lease.getLeaseEnd());
            presetFee.setRemark(DateUtil.formatDate(today) + "退租清算\"预退押金" + depositAmount + "元\"");
            presetFees.add(presetFee);
        }

        // 2. 未付账单（收入方向）
        for (LeaseBill bill : unpaidBills) {
            BigDecimal unpaid = ObjectUtil.defaultIfNull(bill.getTotalAmount(), BigDecimal.ZERO)
                .subtract(ObjectUtil.defaultIfNull(bill.getPaidAmount(), BigDecimal.ZERO));
            if (unpaid.compareTo(BigDecimal.ZERO) > 0) {
                String typeName = getBillTypeName(bill.getBillType());
                LeaseCheckoutFeeVO presetFee = new LeaseCheckoutFeeVO();
                presetFee.setFeeDirection(FeeDirectionEnum.IN.getCode());
                presetFee.setFeeType(mapBillTypeToFeeType(bill.getBillType()));
                presetFee.setFeeName(typeName);
                presetFee.setFeeTypeName(LeaseBillTypeEnum.getNameByCode(presetFee.getFeeType()));
                presetFee.setFeeAmount(unpaid);
                presetFee.setFeeStartDate(bill.getBillStart());
                presetFee.setFeeEndDate(bill.getBillEnd());
                presetFee.setRemark(DateUtil.formatDate(today) + "退租清算\"应退" + unpaid + "元\"");
                presetFee.setLeaseBillId(bill.getId());
                presetFees.add(presetFee);
            }
        }

        return presetFees;
    }

    /**
     * 创建/保存退租单（退租并结账）
     */
    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.LEASE_CHECKOUT,
        operateType = BizOperateTypeEnum.SAVE,
        operateDesc = "保存退租单",
        bizIdExpr = "#result",
        remarkExpr = "#p0.remark",
        sourceType = BizOperateSourceTypeEnum.LEASE,
        sourceIdExpr = "#afterSnapshot != null ? #afterSnapshot.leaseId : #p0.leaseId",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "leaseCheckoutSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long saveCheckout(LeaseCheckoutDTO dto) {
        Lease lease = leaseRepo.getById(dto.getLeaseId());
        if (lease == null) {
            throw new BizException("租约不存在");
        }

        boolean isNew = dto.getId() == null;
        LeaseCheckout checkout = isNew ? createNewCheckout(dto, lease) : updateExistingCheckout(dto);
        boolean shouldSubmitAfterSave = shouldSubmitAfterSave(checkout);

        // 设置退租基本信息
        checkout.setCheckoutType(dto.getCheckoutType());
        checkout.setActualCheckoutDate(dto.getActualCheckoutDate());
        BigDecimal depositAmount = lease.getRentPrice().multiply(BigDecimal.valueOf(ObjectUtil.defaultIfNull(lease.getDepositMonths(), 0)));
        checkout.setDepositAmount(depositAmount);
        if (Boolean.TRUE.equals(dto.getAddCleaningFee())
            && ObjectUtil.defaultIfNull(dto.getCleaningFeeAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("加收房屋清洁费时，金额必须大于0");
        }
        checkout.setAddCleaningFee(Boolean.TRUE.equals(dto.getAddCleaningFee()));
        checkout.setCleaningFeeAmount(Boolean.TRUE.equals(dto.getAddCleaningFee()) ? dto.getCleaningFeeAmount() : null);
        checkout.setDueDate(dto.getDueDate());
        checkout.setSettlementMethod(dto.getSettlementMethod());
        // 坏账原因（标记坏账时必填）
        if (CheckoutSettlementMethodEnum.BAD_DEBT.getCode().equals(dto.getSettlementMethod())) {
            if (CharSequenceUtil.isBlank(dto.getBadDebtReason())) {
                throw new BizException("标记坏账时坏账原因不能为空");
            }
            checkout.setBadDebtReason(dto.getBadDebtReason());
        } else {
            checkout.setBadDebtReason(null);
        }
        checkout.setRemark(dto.getRemark());

        // 解约原因（违约退时保存）
        if (CheckoutTypeEnum.BREACH.getCode().equals(dto.getCheckoutType())) {
            checkout.setBreachReason(dto.getBreachReason());
        } else {
            checkout.setBreachReason(null);
        }

        // 退租凭证附件
        if (CollUtil.isNotEmpty(dto.getAttachmentIds())) {
            checkout.setAttachmentIds(JSONUtil.toJsonStr(dto.getAttachmentIds()));
        }

        // 收款人信息
        checkout.setPayeeName(dto.getPayeeName());
        checkout.setPayeePhone(dto.getPayeePhone());
        checkout.setPayeeIdType(dto.getPayeeIdType());
        checkout.setPayeeIdNo(dto.getPayeeIdNo());
        checkout.setBankType(dto.getBankType());
        checkout.setBankCardType(dto.getBankCardType());
        checkout.setBankAccount(dto.getBankAccount());
        checkout.setBankName(dto.getBankName());
        checkout.setBankBranch(dto.getBankBranch());

        // 发送确认单
        checkout.setSendConfirmation(dto.getSendConfirmation());
        checkout.setConfirmationTemplate(dto.getConfirmationTemplate());

        // 计算费用汇总
        calculateAndSetFees(checkout, dto.getFeeList());
        initializePaymentStatus(checkout);

        // 保存退租单和费用明细
        saveCheckoutAndFees(checkout, isNew, dto.getFeeList(), dto.getOperatorId());

        if (shouldSubmitAfterSave) {
            submitCheckout(
                checkout.getId(),
                OperatorDTO.builder()
                    .operatorId(dto.getOperatorId())
                    .operatorName(dto.getOperatorName())
                    .build()
            );
        }

        log.info("退租单保存成功: checkoutId={}, leaseId={}, type={}",
            checkout.getId(), dto.getTenantId(),
            CheckoutTypeEnum.getNameByCode(dto.getCheckoutType()));
        return checkout.getId();
    }

    /**
     * 创建新退租单
     */
    private LeaseCheckout createNewCheckout(LeaseCheckoutDTO dto, Lease lease) {
        if (leaseCheckoutRepo.hasActiveCheckoutByLeaseId(lease.getId())) {
            throw new BizException("该租客已有进行中的退租单");
        }

        LeaseCheckout checkout = new LeaseCheckout();
        checkout.setCheckoutCode(generateCheckoutCode());
        checkout.setCompanyId(dto.getCompanyId());
        checkout.setTenantId(dto.getTenantId());
        checkout.setLeaseId(lease.getId());
        checkout.setLeaseEnd(lease.getLeaseEnd());
        checkout.setStatus(CheckoutStatusEnum.DRAFT.getCode());
        checkout.setCreateBy(dto.getOperatorId());
        checkout.setCreateAt(DateUtil.date());
        return checkout;
    }

    /**
     * 更新现有退租单
     */
    private LeaseCheckout updateExistingCheckout(LeaseCheckoutDTO dto) {
        LeaseCheckout checkout = leaseCheckoutRepo.getById(dto.getId());
        if (checkout == null) {
            throw new BizException("退租单不存在");
        }

        if (!canModifyCheckout(checkout)) {
            throw new BizException("当前状态不允许修改");
        }

        assertRoomsCanRestore(checkout, "当前房间已被重新出租，不能修改退租单");

        checkout.setUpdateBy(dto.getOperatorId());
        checkout.setUpdateAt(DateUtil.date());
        return checkout;
    }

    private boolean canModifyCheckout(LeaseCheckout checkout) {
        if (CheckoutStatusEnum.CANCELLED.getCode().equals(checkout.getStatus())) {
            return false;
        }
        return !CheckoutPaymentStatusEnum.PAID.getCode().equals(checkout.getPaymentStatus());
    }

    private boolean shouldSubmitAfterSave(LeaseCheckout checkout) {
        return CheckoutStatusEnum.DRAFT.getCode().equals(checkout.getStatus())
            || BizApprovalStatusEnum.REJECTED.getCode().equals(checkout.getApprovalStatus());
    }

    /**
     * 计算并设置费用汇总
     * 收入 = 收方向费用合计
     * 支出 = 支方向费用合计
     * 最终结算 = 支出 - 收入（负数=应退租客）
     */
    private void calculateAndSetFees(LeaseCheckout checkout, List<LeaseCheckoutFeeDTO> feeList) {
        BigDecimal incomeAmount = BigDecimal.ZERO;   // 收（租客应付）
        BigDecimal expenseAmount = BigDecimal.ZERO;  // 支（退还租客）

        if (CollUtil.isNotEmpty(feeList)) {
            for (LeaseCheckoutFeeDTO fee : feeList) {
                BigDecimal amount = ObjectUtil.defaultIfNull(fee.getFeeAmount(), BigDecimal.ZERO);
                if (FeeDirectionEnum.IN.getCode().equals(fee.getFeeDirection())) {
                    incomeAmount = incomeAmount.add(amount);
                } else if (FeeDirectionEnum.OUT.getCode().equals(fee.getFeeDirection())) {
                    expenseAmount = expenseAmount.add(amount);
                }
            }
        }

        if (Boolean.TRUE.equals(checkout.getAddCleaningFee())) {
            BigDecimal cleaningFeeAmount = ObjectUtil.defaultIfNull(checkout.getCleaningFeeAmount(), BigDecimal.ZERO);
            if (cleaningFeeAmount.compareTo(BigDecimal.ZERO) > 0) {
                incomeAmount = incomeAmount.add(cleaningFeeAmount);
            }
        }

        checkout.setIncomeAmount(incomeAmount);
        checkout.setExpenseAmount(expenseAmount);
        // finalAmount = 收入 - 支出：正数=租客补缴，负数=应退租客
        checkout.setFinalAmount(incomeAmount.subtract(expenseAmount));
    }

    private void initializePaymentStatus(LeaseCheckout checkout) {
        BigDecimal finalAmount = ObjectUtil.defaultIfNull(checkout.getFinalAmount(), BigDecimal.ZERO);
        if (CheckoutSettlementMethodEnum.BAD_DEBT.getCode().equals(checkout.getSettlementMethod())
            || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            checkout.setPaymentStatus(CheckoutPaymentStatusEnum.NO_PAYMENT_REQUIRED.getCode());
            checkout.setPayAt(null);
            return;
        }
        checkout.setPaymentStatus(CheckoutPaymentStatusEnum.UNPAID.getCode());
        checkout.setPayAt(null);
    }

    /**
     * 保存退租单和费用明细
     */
    private void saveCheckoutAndFees(LeaseCheckout checkout, boolean isNew,
                                     List<LeaseCheckoutFeeDTO> feeList, Long operatorId) {
        if (isNew) {
            leaseCheckoutRepo.save(checkout);
        } else {
            leaseCheckoutRepo.updateById(checkout);
            leaseCheckoutFeeRepo.deleteByCheckoutId(checkout.getId());
        }

        if (CollUtil.isNotEmpty(feeList)) {
            List<LeaseCheckoutFee> fees = new ArrayList<>();
            for (LeaseCheckoutFeeDTO feeDTO : feeList) {
                LeaseCheckoutFee fee = new LeaseCheckoutFee();
                fee.setCheckoutId(checkout.getId());
                fee.setFeeDirection(feeDTO.getFeeDirection());
                fee.setFeeType(feeDTO.getFeeType());
                fee.setDictDataId(feeDTO.getDictDataId());
                fee.setFeeName(feeDTO.getFeeName());
                fee.setFeeAmount(feeDTO.getFeeAmount());
                fee.setFeeStartDate(feeDTO.getFeeStartDate());
                fee.setFeeEndDate(feeDTO.getFeeEndDate());
                fee.setRemark(feeDTO.getRemark());
                fee.setLeaseBillId(feeDTO.getLeaseBillId());
                fee.setCreateBy(operatorId);
                fee.setCreateAt(DateUtil.date());
                fees.add(fee);
            }
            leaseCheckoutFeeRepo.saveBatch(fees);
        }
    }

    /**
     * 提交退租审批
     */
    public void submitCheckout(Long checkoutId, OperatorDTO operatorDTO) {
        LeaseCheckout checkout = leaseCheckoutRepo.getById(checkoutId);
        if (checkout == null) {
            throw new BizException("退租单不存在");
        }

        if (!CheckoutStatusEnum.DRAFT.getCode().equals(checkout.getStatus())
            && !BizApprovalStatusEnum.REJECTED.getCode().equals(checkout.getApprovalStatus())) {
            throw new BizException("当前状态不允许提交");
        }

        Tenant tenant = tenantRepo.getById(checkout.getTenantId());

        // 更新状态为待确认
        checkout.setStatus(CheckoutStatusEnum.PENDING.getCode());
        leaseCheckoutRepo.updateById(checkout);

        // 提交审批
        approvalTemplate.submitIfNeed(
            ApprovalSubmitDTO.builder()
                .companyId(checkout.getCompanyId())
                .bizType(ApprovalBizTypeEnum.TENANT_CHECKOUT.getCode())
                .bizId(checkout.getId())
                .title("退租并结账 - " + (tenant != null ? tenant.getTenantName() : ""))
                .applicantId(operatorDTO.getOperatorId())
                .build(),
            // 需要审批
            bizId -> leaseCheckoutRepo.updateApprovalStatus(bizId, BizApprovalStatusEnum.PENDING.getCode()),
            // 无需审批：直接完成
            bizId -> {
                leaseCheckoutRepo.updateApprovalStatus(bizId, BizApprovalStatusEnum.APPROVED.getCode());
                completeCheckout(bizId, operatorDTO.getOperatorId());
            }
        );

        log.info("退租单已提交: checkoutId={}", checkoutId);
    }

    /**
     * 完成退租（审批通过后调用）
     */
    public void completeCheckout(Long checkoutId, Long operatorId) {
        LeaseCheckout checkout = leaseCheckoutRepo.getById(checkoutId);
        if (checkout == null) {
            return;
        }

        // 更新退租单状态
        checkout.setStatus(CheckoutStatusEnum.COMPLETED.getCode());
        checkout.setSettlementAt(DateUtil.date());
        if (CheckoutSettlementMethodEnum.OFFLINE_PAYMENT.getCode().equals(checkout.getSettlementMethod())
            && ObjectUtil.defaultIfNull(checkout.getFinalAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
            checkout.setPaymentStatus(CheckoutPaymentStatusEnum.PAID.getCode());
            checkout.setPayAt(DateUtil.date());
        } else if (CheckoutSettlementMethodEnum.BAD_DEBT.getCode().equals(checkout.getSettlementMethod())
            || ObjectUtil.defaultIfNull(checkout.getFinalAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) <= 0) {
            checkout.setPaymentStatus(CheckoutPaymentStatusEnum.NO_PAYMENT_REQUIRED.getCode());
            checkout.setPayAt(null);
        }
        checkout.setUpdateBy(operatorId);
        checkout.setUpdateAt(DateUtil.date());
        leaseCheckoutRepo.updateById(checkout);

        // 更新租客状态为已退租
        Lease lease = checkout.getLeaseId() != null ? leaseRepo.getById(checkout.getLeaseId()) : null;
        if (lease != null) {
            leaseRepo.updateStatusById(lease.getId(), LeaseStatusEnum.TERMINATED.getCode());
        }

        // 释放房间
        if (lease != null) {
            List<Long> roomIds = JSONUtil.toList(lease.getRoomIds(), Long.class);
            roomRepo.updateOccupancyStatusByRoomIds(roomIds, OccupancyStatusEnum.AVAILABLE.getCode());
        }

        log.info("退租完成: checkoutId={}, leaseId={}", checkoutId, checkout.getTenantId());
    }

    /**
     * 取消退租单
     */
    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.LEASE_CHECKOUT,
        operateType = BizOperateTypeEnum.CANCEL,
        operateDesc = "取消退租单",
        bizIdExpr = "#p0",
        remarkExpr = "#p1",
        sourceType = BizOperateSourceTypeEnum.LEASE,
        sourceIdExpr = "#afterSnapshot != null ? #afterSnapshot.leaseId : null",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "leaseCheckoutSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public void cancelCheckout(Long checkoutId, String cancelReason, OperatorDTO operatorDTO) {
        LeaseCheckout checkout = leaseCheckoutRepo.getById(checkoutId);
        if (checkout == null) {
            throw new BizException("退租单不存在");
        }

        if (CheckoutStatusEnum.CANCELLED.getCode().equals(checkout.getStatus())) {
            throw new BizException("退租单已取消");
        }

        if (CheckoutPaymentStatusEnum.PAID.getCode().equals(checkout.getPaymentStatus())) {
            throw new BizException("退租单已支付，不能取消");
        }

        if (CheckoutStatusEnum.COMPLETED.getCode().equals(checkout.getStatus())) {
            restoreCompletedCheckout(checkout);
        }

        checkout.setStatus(CheckoutStatusEnum.CANCELLED.getCode());
        checkout.setSettlementAt(null);
        checkout.setCancelReason(CharSequenceUtil.blankToDefault(CharSequenceUtil.trim(cancelReason), "取消退租单"));
        checkout.setCancelBy(operatorDTO.getOperatorId());
        checkout.setCancelByName(operatorDTO.getOperatorName());
        checkout.setCancelAt(DateUtil.date());
        checkout.setUpdateBy(operatorDTO.getOperatorId());
        checkout.setUpdateAt(DateUtil.date());
        leaseCheckoutRepo.updateById(checkout);

        log.info("退租单已取消: checkoutId={}", checkoutId);
    }

    /**
     * 获取退租单详情
     */
    public LeaseCheckoutVO getCheckoutDetail(Long checkoutId) {
        LeaseCheckout checkout = leaseCheckoutRepo.getById(checkoutId);
        if (checkout == null) {
            return null;
        }
        return convertToVO(checkout);
    }

    /**
     * 根据租约ID获取退租单
     */
    public LeaseCheckoutVO getCheckoutByLeaseId(Long leaseId) {
        LeaseCheckout checkout = leaseCheckoutRepo.getByLeaseId(leaseId);
        if (checkout == null) {
            return null;
        }
        return convertToVO(checkout);
    }

    /**
     * 查询退租单列表
     */
    public PageVO<LeaseCheckoutVO> queryCheckoutList(LeaseCheckoutQueryDTO query) {
        Page<LeaseCheckout> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        LambdaQueryWrapper<LeaseCheckout> wrapper = new LambdaQueryWrapper<>();
        if (query.getCompanyId() != null) {
            wrapper.eq(LeaseCheckout::getCompanyId, query.getCompanyId());
        }
        if (query.getTenantId() != null) {
            wrapper.eq(LeaseCheckout::getTenantId, query.getTenantId());
        }
        if (query.getLeaseId() != null) {
            wrapper.eq(LeaseCheckout::getLeaseId, query.getLeaseId());
        }
        if (CharSequenceUtil.isNotBlank(query.getCheckoutCode())) {
            wrapper.like(LeaseCheckout::getCheckoutCode, query.getCheckoutCode());
        }
        if (query.getCheckoutType() != null) {
            wrapper.eq(LeaseCheckout::getCheckoutType, query.getCheckoutType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(LeaseCheckout::getStatus, query.getStatus());
        }
        if (query.getApprovalStatus() != null) {
            wrapper.eq(LeaseCheckout::getApprovalStatus, query.getApprovalStatus());
        }

        if (CharSequenceUtil.isNotBlank(query.getTenantName())) {
            List<Long> tenantIds = tenantRepo.lambdaQuery()
                .like(Tenant::getTenantName, query.getTenantName())
                .list()
                .stream()
                .map(Tenant::getId)
                .toList();
            if (tenantIds.isEmpty()) {
                return new PageVO<>();
            }
            wrapper.in(LeaseCheckout::getTenantId, tenantIds);
        }

        wrapper.orderByDesc(LeaseCheckout::getId);

        IPage<LeaseCheckout> result = leaseCheckoutRepo.page(page, wrapper);

        PageVO<LeaseCheckoutVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setCurrentPage(result.getCurrent());
        pageVO.setPageSize(result.getSize());
        pageVO.setPages(result.getPages());
        pageVO.setList(result.getRecords().stream().map(this::convertToVO).toList());
        return pageVO;
    }

    /**
     * 转换为 VO
     */
    private LeaseCheckoutVO convertToVO(LeaseCheckout checkout) {
        LeaseCheckoutVO vo = BeanCopyUtils.copyBean(checkout, LeaseCheckoutVO.class);
        assert vo != null;
        vo.setLeaseId(checkout.getLeaseId());

        // 解约原因
        vo.setBreachReason(checkout.getBreachReason());

        // 租客信息 & 房间信息
        Tenant tenant = tenantRepo.getById(checkout.getTenantId());
        if (tenant != null) {
            vo.setTenantName(tenant.getTenantName());
            vo.setTenantPhone(tenant.getTenantPhone());
            Lease lease = checkout.getLeaseId() != null ? leaseRepo.getById(checkout.getLeaseId()) : null;
            if (lease != null) {
                vo.setLeaseId(lease.getId());
                vo.setRentPrice(lease.getRentPrice());
                List<Long> roomIds = JSONUtil.toList(lease.getRoomIds(), Long.class);
                vo.setRoomAddress(roomService.getRoomAddressByIds(roomIds));
            }
        }

        // 枚举名称
        vo.setCheckoutTypeName(CheckoutTypeEnum.getNameByCode(checkout.getCheckoutType()));
        vo.setStatusName(CheckoutStatusEnum.getNameByCode(checkout.getStatus()));
        vo.setSettlementMethodName(CheckoutSettlementMethodEnum.getNameByCode(checkout.getSettlementMethod()));
        CheckoutPaymentStatusEnum paymentStatusEnum = getCheckoutPaymentStatusByCode(checkout.getPaymentStatus());
        vo.setPaymentStatusName(paymentStatusEnum != null ? paymentStatusEnum.getName() : "");
        BizApprovalStatusEnum approvalEnum = BizApprovalStatusEnum.getByCode(checkout.getApprovalStatus());
        vo.setApprovalStatusName(approvalEnum != null ? approvalEnum.getName() : "");

        // 创建人
        if (checkout.getCreateBy() != null) {
            vo.setCreateByName(userRepo.getUserNicknameById(checkout.getCreateBy()));
        }

        if (checkout.getCancelBy() != null && CharSequenceUtil.isBlank(checkout.getCancelByName())) {
            vo.setCancelByName(userRepo.getUserNicknameById(checkout.getCancelBy()));
        }

        // 附件
        if (CharSequenceUtil.isNotBlank(checkout.getAttachmentIds())) {
            vo.setAttachmentUrls(JSONUtil.toList(checkout.getAttachmentIds(), String.class));
        }

        // 费用明细
        List<LeaseCheckoutFee> feeList = leaseCheckoutFeeRepo.listByCheckoutId(checkout.getId());
        vo.setFeeList(convertFeeListToVO(feeList));

        return vo;
    }

    /**
     * 转换费用列表为 VO
     */
    private List<LeaseCheckoutFeeVO> convertFeeListToVO(List<LeaseCheckoutFee> feeList) {
        if (CollUtil.isEmpty(feeList)) {
            return new ArrayList<>();
        }

        List<LeaseCheckoutFeeVO> voList = new ArrayList<>();
        for (LeaseCheckoutFee fee : feeList) {
            LeaseCheckoutFeeVO vo = new LeaseCheckoutFeeVO();
            BeanUtil.copyProperties(fee, vo);
            String feeTypeName = LeaseBillTypeEnum.getNameByCode(fee.getFeeType());
            vo.setFeeTypeName(CharSequenceUtil.isNotBlank(feeTypeName) ? feeTypeName : "其他费用");
            vo.setFeeDirectionName(FeeDirectionEnum.getLabelByCode(fee.getFeeDirection()));
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 账单类型转费用类型
     */
    private Integer mapBillTypeToFeeType(Integer billType) {
        if (billType == null) return LeaseBillTypeEnum.OTHER_FEE.getCode();
        return switch (billType) {
            case 1 -> LeaseBillTypeEnum.RENT.getCode();
            case 2 -> LeaseBillTypeEnum.DEPOSIT.getCode();
            default -> LeaseBillTypeEnum.OTHER_FEE.getCode();
        };
    }

    private CheckoutPaymentStatusEnum getCheckoutPaymentStatusByCode(String code) {
        if (CharSequenceUtil.isBlank(code)) {
            return null;
        }
        for (CheckoutPaymentStatusEnum item : CheckoutPaymentStatusEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    private void restoreCompletedCheckout(LeaseCheckout checkout) {
        assertRoomsCanRestore(checkout, "当前房间已被重新出租，不能取消退租单");

        Lease lease = checkout.getLeaseId() == null ? null : leaseRepo.getById(checkout.getLeaseId());
        if (lease == null) {
            throw new BizException("关联租约不存在，不能取消退租单");
        }

        lease.setStatus(LeaseStatusEnum.EFFECTIVE.getCode());
        lease.setCheckOutStatus(0);
        lease.setUpdateAt(DateUtil.date());
        leaseRepo.updateById(lease);

        List<Long> roomIds = JSONUtil.toList(lease.getRoomIds(), Long.class);
        if (CollUtil.isNotEmpty(roomIds)) {
            roomRepo.updateOccupancyStatusByRoomIds(roomIds, OccupancyStatusEnum.LEASED.getCode());
        }
    }

    private void assertRoomsCanRestore(LeaseCheckout checkout, String message) {
        Lease lease = checkout.getLeaseId() == null ? null : leaseRepo.getById(checkout.getLeaseId());
        if (lease == null) {
            return;
        }
        List<Long> roomIds = JSONUtil.toList(lease.getRoomIds(), Long.class);
        if (CollUtil.isEmpty(roomIds)) {
            return;
        }
        if (leaseRepo.existsConflict(roomIds, lease.getLeaseStart(), lease.getLeaseEnd(), lease.getId())) {
            throw new BizException(message);
        }
    }

    /**
     * 账单类型名称
     */
    private String getBillTypeName(Integer billType) {
        if (billType == null) return "其他费用";
        return switch (billType) {
            case 1 -> "租金";
            case 2 -> "押金";
            case 3 -> "其他费用";
            case 4 -> "水费";
            case 5 -> "电费";
            case 6 -> "燃气费";
            case 7 -> "物业费";
            default -> "其他费用";
        };
    }

    /**
     * 生成退租单编号
     */
    private String generateCheckoutCode() {
        return "CK" + IdUtil.getSnowflakeNextIdStr();
    }
}
