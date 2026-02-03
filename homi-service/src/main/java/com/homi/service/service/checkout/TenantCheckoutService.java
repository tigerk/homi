package com.homi.service.service.checkout;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.checkout.CheckoutFeeTypeEnum;
import com.homi.common.lib.enums.checkout.CheckoutStatusEnum;
import com.homi.common.lib.enums.checkout.CheckoutTypeEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.enums.tenant.TenantStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.approval.dto.ApprovalSubmitDTO;
import com.homi.model.checkout.dto.TenantCheckoutDTO;
import com.homi.model.checkout.dto.TenantCheckoutFeeDTO;
import com.homi.model.checkout.dto.TenantCheckoutQueryDTO;
import com.homi.model.checkout.vo.CheckoutInitVO;
import com.homi.model.checkout.vo.TenantCheckoutFeeVO;
import com.homi.model.checkout.vo.TenantCheckoutVO;
import com.homi.model.common.dto.OperatorDTO;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantBill;
import com.homi.model.dao.entity.TenantCheckout;
import com.homi.model.dao.entity.TenantCheckoutFee;
import com.homi.model.dao.repo.*;
import com.homi.service.service.approval.ApprovalTemplate;
import com.homi.service.service.room.RoomService;
import com.homi.service.service.sys.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 退租服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantCheckoutService {

    private final TenantCheckoutRepo tenantCheckoutRepo;
    private final TenantCheckoutFeeRepo tenantCheckoutFeeRepo;
    private final TenantRepo tenantRepo;
    private final TenantBillRepo tenantBillRepo;
    private final RoomService roomService;
    private final UserService userService;
    private final ApprovalTemplate approvalTemplate;
    private final RoomRepo roomRepo;
    private final UserRepo userRepo;

    /**
     * 获取退租初始化数据
     */
    public CheckoutInitVO getCheckoutInitData(Long tenantId) {
        Tenant tenant = tenantRepo.getById(tenantId);
        if (tenant == null) {
            throw new BizException("租客不存在");
        }

        // 检查是否有进行中的退租单
        if (tenantCheckoutRepo.hasActiveCheckout(tenantId)) {
            throw new BizException("该租客已有进行中的退租单");
        }

        // 获取房间信息
        List<Long> roomIds = JSONUtil.toList(tenant.getRoomIds(), Long.class);
//        String roomInfo = roomService.getRoomInfoByIds(roomIds);

        // 获取未付账单
        List<TenantBill> unpaidBills = tenantBillRepo.getBillListByTenantId(tenantId, Boolean.TRUE);
        List<CheckoutInitVO.UnpaidBillVO> unpaidBillVOs = new ArrayList<>();
        BigDecimal unpaidAmount = BigDecimal.ZERO;

        for (TenantBill bill : unpaidBills) {
            BigDecimal payAmount = bill.getPayAmount() != null ? bill.getPayAmount() : BigDecimal.ZERO;
            BigDecimal billTotal = bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal unpaidMoney = billTotal.subtract(payAmount);
            unpaidAmount = unpaidAmount.add(unpaidMoney);

            unpaidBillVOs.add(CheckoutInitVO.UnpaidBillVO.builder()
                .billId(bill.getId())
                .billType(bill.getBillType())
                .billPeriod(bill.getRentPeriodStart().toString() + " ~ " + bill.getRentPeriodEnd().toString())
                .totalAmount(billTotal)
                .payAmount(payAmount)
                .unpaidAmount(unpaidMoney)
                .build());
        }

        // 计算押金总额
        BigDecimal depositAmount = tenant.getRentPrice().multiply(BigDecimal.valueOf(tenant.getDepositMonths()));

        return CheckoutInitVO.builder()
            .tenantId(tenantId)
            .tenantName(tenant.getTenantName())
            .tenantPhone(tenant.getTenantPhone())
//            .roomInfo(roomInfo)
            .leaseStart(tenant.getLeaseStart())
            .leaseEnd(tenant.getLeaseEnd())
            .rentPrice(tenant.getRentPrice())
            .depositAmount(depositAmount)
            .unpaidBills(unpaidBillVOs)
            .unpaidAmount(unpaidAmount)
            .build();
    }

    /**
     * 创建/保存退租单
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveCheckout(TenantCheckoutDTO dto) {
        Tenant tenant = tenantRepo.getById(dto.getTenantId());
        if (tenant == null) {
            throw new BizException("租客不存在");
        }

        boolean isNew = dto.getId() == null;
        TenantCheckout checkout = isNew ? createNewCheckout(dto, tenant) : updateExistingCheckout(dto);

        // 设置基本信息
        checkout.setCheckoutType(dto.getCheckoutType());
        checkout.setCheckoutReason(dto.getCheckoutReason());
        checkout.setActualCheckoutDate(dto.getActualCheckoutDate());
        checkout.setDepositAmount(ObjectUtil.defaultIfNull(dto.getDepositAmount(), BigDecimal.ZERO));
        checkout.setRemark(dto.getRemark());

        // 计算并设置费用
        calculateAndSetFees(checkout, dto.getFeeList());

        // 保存退租单和费用明细
        saveCheckoutAndFees(checkout, isNew, dto.getFeeList(), dto.getOperatorId());

        log.info("退租单保存成功: checkoutId={}, tenantId={}", checkout.getId(), dto.getTenantId());
        return checkout.getId();
    }

    /**
     * 创建新退租单
     */
    private TenantCheckout createNewCheckout(TenantCheckoutDTO dto, Tenant tenant) {
        if (tenantCheckoutRepo.hasActiveCheckout(dto.getTenantId())) {
            throw new BizException("该租客已有进行中的退租单");
        }

        TenantCheckout checkout = new TenantCheckout();
        checkout.setCheckoutCode(generateCheckoutCode());
        checkout.setCompanyId(dto.getCompanyId());
        checkout.setTenantId(dto.getTenantId());
        checkout.setLeaseEnd(tenant.getLeaseEnd());
        checkout.setStatus(CheckoutStatusEnum.DRAFT.getCode());
        checkout.setCreateBy(dto.getOperatorId());
        checkout.setCreateTime(DateUtil.date());
        return checkout;
    }

    /**
     * 更新现有退租单
     */
    private TenantCheckout updateExistingCheckout(TenantCheckoutDTO dto) {
        TenantCheckout checkout = tenantCheckoutRepo.getById(dto.getId());
        if (checkout == null) {
            throw new BizException("退租单不存在");
        }

        boolean canModify = CheckoutStatusEnum.DRAFT.getCode().equals(checkout.getStatus())
            || BizApprovalStatusEnum.REJECTED.getCode().equals(checkout.getApprovalStatus());

        if (!canModify) {
            throw new BizException("当前状态不允许修改");
        }

        checkout.setUpdateBy(dto.getOperatorId());
        checkout.setUpdateTime(DateUtil.date());
        return checkout;
    }

    /**
     * 计算并设置费用
     */
    private void calculateAndSetFees(TenantCheckout checkout, List<TenantCheckoutFeeDTO> feeList) {
        BigDecimal deductionAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;

        if (CollUtil.isNotEmpty(feeList)) {
            for (TenantCheckoutFeeDTO fee : feeList) {
                if (fee.getFeeDirection() == 1) {
                    deductionAmount = deductionAmount.add(fee.getFeeAmount());
                } else {
                    refundAmount = refundAmount.add(fee.getFeeAmount());
                }
            }
        }

        checkout.setDeductionAmount(deductionAmount);
        checkout.setRefundAmount(refundAmount);
        checkout.setFinalAmount(deductionAmount.subtract(refundAmount));
    }

    /**
     * 保存退租单和费用明细
     */
    private void saveCheckoutAndFees(TenantCheckout checkout, boolean isNew,
                                     List<TenantCheckoutFeeDTO> feeList, Long operatorId) {
        if (isNew) {
            tenantCheckoutRepo.save(checkout);
        } else {
            tenantCheckoutRepo.updateById(checkout);
            tenantCheckoutFeeRepo.deleteByCheckoutId(checkout.getId());
        }

        if (CollUtil.isNotEmpty(feeList)) {
            List<TenantCheckoutFee> fees = new ArrayList<>();
            for (TenantCheckoutFeeDTO feeDTO : feeList) {
                TenantCheckoutFee fee = new TenantCheckoutFee();
                BeanUtil.copyProperties(feeDTO, fee);
                fee.setCheckoutId(checkout.getId());
                fee.setCreateBy(operatorId);
                fee.setCreateTime(DateUtil.date());
                fees.add(fee);
            }
            tenantCheckoutFeeRepo.saveBatch(fees);
        }
    }

    /**
     * 提交退租审批
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitCheckout(Long checkoutId, OperatorDTO operatorDTO) {
        TenantCheckout checkout = tenantCheckoutRepo.getById(checkoutId);
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
        tenantCheckoutRepo.updateById(checkout);

        // 提交审批
        approvalTemplate.submitIfNeed(
            ApprovalSubmitDTO.builder()
                .companyId(checkout.getCompanyId())
                .bizType(ApprovalBizTypeEnum.TENANT_CHECKOUT.getCode())
                .bizId(checkout.getId())
                .title("退租审批 - " + tenant.getTenantName())
                .applicantId(operatorDTO.getOperatorId())
                .build(),
            // 需要审批：PENDING
            bizId -> tenantCheckoutRepo.updateApprovalStatus(bizId, BizApprovalStatusEnum.PENDING.getCode()),
            // 无需审批：APPROVED + 执行退租
            bizId -> {
                tenantCheckoutRepo.updateApprovalStatus(bizId, BizApprovalStatusEnum.APPROVED.getCode());
                completeCheckout(bizId, operatorDTO.getOperatorId());
            }
        );

        log.info("退租单已提交: checkoutId={}", checkoutId);
    }

    /**
     * 完成退租（审批通过后调用）未付账单不作废，保持原样。
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeCheckout(Long checkoutId, Long operatorId) {
        TenantCheckout checkout = tenantCheckoutRepo.getById(checkoutId);
        if (checkout == null) {
            return;
        }

        // 更新退租单状态
        checkout.setStatus(CheckoutStatusEnum.COMPLETED.getCode());
        checkout.setSettlementTime(DateUtil.date());
        checkout.setUpdateBy(operatorId);
        checkout.setUpdateTime(DateUtil.date());
        tenantCheckoutRepo.updateById(checkout);

        // 更新租客状态为已退租
        tenantRepo.updateStatusById(checkout.getTenantId(), TenantStatusEnum.TERMINATED.getCode());

        // 释放房间
        Tenant tenant = tenantRepo.getById(checkout.getTenantId());
        if (tenant != null) {
            List<Long> roomIds = JSONUtil.toList(tenant.getRoomIds(), Long.class);
            roomRepo.updateRoomStatusByRoomIds(roomIds, RoomStatusEnum.AVAILABLE.getCode());
        }

        log.info("退租完成: checkoutId={}, tenantId={}", checkoutId, checkout.getTenantId());
    }

    /**
     * 取消退租单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelCheckout(Long checkoutId, OperatorDTO operatorDTO) {
        TenantCheckout checkout = tenantCheckoutRepo.getById(checkoutId);
        if (checkout == null) {
            throw new BizException("退租单不存在");
        }

        if (CheckoutStatusEnum.COMPLETED.getCode().equals(checkout.getStatus())) {
            throw new BizException("已完成的退租单不能取消");
        }

        checkout.setStatus(CheckoutStatusEnum.CANCELLED.getCode());
        checkout.setUpdateBy(operatorDTO.getOperatorId());
        checkout.setUpdateTime(DateUtil.date());
        tenantCheckoutRepo.updateById(checkout);

        log.info("退租单已取消: checkoutId={}", checkoutId);
    }

    /**
     * 获取退租单详情
     */
    public TenantCheckoutVO getCheckoutDetail(Long checkoutId) {
        TenantCheckout checkout = tenantCheckoutRepo.getById(checkoutId);
        if (checkout == null) {
            return null;
        }

        return convertToVO(checkout);
    }

    /**
     * 根据租客ID获取退租单
     */
    public TenantCheckoutVO getCheckoutByTenantId(Long tenantId) {
        TenantCheckout checkout = tenantCheckoutRepo.getByTenantId(tenantId);
        if (checkout == null) {
            return null;
        }

        return convertToVO(checkout);
    }

    /**
     * 查询退租单列表
     */
    public PageVO<TenantCheckoutVO> queryCheckoutList(TenantCheckoutQueryDTO query) {
        // TODO: 实现分页查询
        return new PageVO<>();
    }

    /**
     * 转换为 VO
     */
    private TenantCheckoutVO convertToVO(TenantCheckout checkout) {
        TenantCheckoutVO vo = BeanCopyUtils.copyBean(checkout, TenantCheckoutVO.class);
        assert vo != null;

        // 租客信息
        Tenant tenant = tenantRepo.getById(checkout.getTenantId());
        if (tenant != null) {

            vo.setTenantName(tenant.getTenantName());
            vo.setTenantPhone(tenant.getTenantPhone());
            List<Long> roomIds = JSONUtil.toList(tenant.getRoomIds(), Long.class);
//            vo.setRoomInfo(roomService.getRoomInfoByIds(roomIds));
        }

        // 状态名称
        vo.setCheckoutTypeName(CheckoutTypeEnum.getNameByCode(checkout.getCheckoutType()));
        vo.setStatusName(CheckoutStatusEnum.getNameByCode(checkout.getStatus()));
        vo.setApprovalStatusName(BizApprovalStatusEnum.getByCode(checkout.getApprovalStatus()) != null
            ? BizApprovalStatusEnum.getByCode(checkout.getApprovalStatus()).getName() : "");

        // 创建人
        if (checkout.getCreateBy() != null) {
            vo.setCreateByName(userRepo.getUserNicknameById(checkout.getCreateBy()));
        }

        // 费用明细
        List<TenantCheckoutFee> feeList = tenantCheckoutFeeRepo.listByCheckoutId(checkout.getId());
        vo.setFeeList(convertFeeListToVO(feeList));

        return vo;
    }

    /**
     * 转换费用列表为 VO
     */
    private List<TenantCheckoutFeeVO> convertFeeListToVO(List<TenantCheckoutFee> feeList) {
        if (CollUtil.isEmpty(feeList)) {
            return new ArrayList<>();
        }

        List<TenantCheckoutFeeVO> voList = new ArrayList<>();
        for (TenantCheckoutFee fee : feeList) {
            TenantCheckoutFeeVO vo = new TenantCheckoutFeeVO();
            BeanUtil.copyProperties(fee, vo);
            vo.setFeeTypeName(CheckoutFeeTypeEnum.getNameByCode(fee.getFeeType()));
            vo.setFeeDirectionName(fee.getFeeDirection() == 1 ? "扣款" : "退款");
            voList.add(vo);
        }

        return voList;
    }

    /**
     * 生成退租单编号
     */
    private String generateCheckoutCode() {
        return "CK" + IdUtil.getSnowflakeNextIdStr();
    }
}
