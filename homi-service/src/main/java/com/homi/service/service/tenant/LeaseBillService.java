package com.homi.service.service.tenant;

import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillOtherFee;
import com.homi.model.dao.repo.LeaseBillOtherFeeRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.tenant.vo.bill.LeaseBillListVO;
import com.homi.model.tenant.vo.bill.LeaseBillOtherFeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaseBillService {
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillOtherFeeRepo leaseBillOtherFeeRepo;

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
}
