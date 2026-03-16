package com.homi.service.service.tenant;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillOtherFee;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.repo.FinanceFlowRepo;
import com.homi.model.dao.repo.LeaseBillOtherFeeRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.dao.repo.PaymentFlowRepo;
import com.homi.model.tenant.dto.LeaseBillOtherFeeDTO;
import com.homi.model.tenant.dto.LeaseBillUpdateDTO;
import com.homi.model.tenant.vo.bill.FinanceFlowVO;
import com.homi.model.tenant.vo.bill.LeaseBillListVO;
import com.homi.model.tenant.vo.bill.LeaseBillOtherFeeVO;
import com.homi.model.tenant.vo.bill.PaymentFlowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaseBillService {
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillOtherFeeRepo leaseBillOtherFeeRepo;
    private final FinanceFlowRepo financeFlowRepo;
    private final PaymentFlowRepo paymentFlowRepo;

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

        // 2. 一次性查询所有 otherFees
        List<LeaseBillOtherFee> allOtherFees = leaseBillOtherFeeRepo.getOtherFeesByBillIds(billIds);

        // 3. 按 billId 分组
        Map<Long, List<LeaseBillOtherFeeVO>> otherFeesMap = allOtherFees.stream()
            .collect(Collectors.groupingBy(
                LeaseBillOtherFee::getBillId,
                Collectors.mapping(of -> BeanCopyUtils.copyBean(of, LeaseBillOtherFeeVO.class), Collectors.toList())
            ));

        // 4. 组装结果
        return leaseBillList.stream().map(tb -> {
            LeaseBillListVO vo = BeanCopyUtils.copyBean(tb, LeaseBillListVO.class);
            assert vo != null;
            vo.setOtherFees(otherFeesMap.getOrDefault(tb.getId(), List.of()));
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

        List<LeaseBillOtherFee> otherFees = leaseBillOtherFeeRepo.getOtherFeesByBillId(billId);
        List<LeaseBillOtherFeeVO> otherFeeVos = otherFees.stream()
            .map(of -> BeanCopyUtils.copyBean(of, LeaseBillOtherFeeVO.class))
            .toList();
        vo.setOtherFees(otherFeeVos);
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

        DateTime now = DateUtil.date();

        // 忽略 null，不覆盖已有字段
        BeanUtil.copyProperties(dto, bill, CopyOptions.create().setIgnoreNullValue(true));
        if (dto.getValid() != null) {
            bill.setValid(dto.getValid());
        }
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        leaseBillRepo.updateById(bill);

        if (dto.getOtherFees() != null) {
            leaseBillOtherFeeRepo.removeByBillId(bill.getId());
            List<LeaseBillOtherFee> toSave = new ArrayList<>();
            for (LeaseBillOtherFeeDTO fee : dto.getOtherFees()) {
                LeaseBillOtherFee entity = BeanCopyUtils.copyBean(fee, LeaseBillOtherFee.class);
                assert entity != null;
                entity.setBillId(bill.getId());
                entity.setCreateBy(operatorId);
                entity.setUpdateBy(operatorId);
                entity.setCreateTime(now);
                entity.setUpdateTime(now);
                toSave.add(entity);
            }
            if (!toSave.isEmpty()) {
                leaseBillOtherFeeRepo.saveBatch(toSave);
            }
        }
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

        List<FinanceFlow> financeFlows = financeFlowRepo.getListByBiz(
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

        PaymentFlow paymentFlow = paymentFlowRepo.getByBiz(PaymentFlowBizTypeEnum.LEASE_BILL.getCode(), bill.getId());
        if (paymentFlow == null) {
            return;
        }
        PaymentFlowVO paymentFlowVO = new PaymentFlowVO();
        BeanUtils.copyProperties(paymentFlow, paymentFlowVO);
        vo.setPaymentFlow(paymentFlowVO);
    }
}
