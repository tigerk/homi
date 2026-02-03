package com.homi.service.service.tenant;

import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.TenantBill;
import com.homi.model.dao.entity.TenantBillOtherFee;
import com.homi.model.dao.repo.TenantBillOtherFeeRepo;
import com.homi.model.dao.repo.TenantBillRepo;
import com.homi.model.tenant.vo.bill.TenantBillListVO;
import com.homi.model.tenant.vo.bill.TenantBillOtherFeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantBillService {
    private final TenantBillRepo tenantBillRepo;
    private final TenantBillOtherFeeRepo tenantBillOtherFeeRepo;

    /**
     * 根据租客ID查询账单列表
     *
     * @param tenantId 租客ID
     * @param valid    是否有效
     * @return 账单列表VO
     */
    public List<TenantBillListVO> getBillListByTenantId(Long tenantId, Boolean valid) {
        List<TenantBill> tenantBillList = tenantBillRepo.getBillListByTenantId(tenantId, valid);

        if (tenantBillList.isEmpty()) {
            return List.of();
        }

        // 1. 收集所有 billId
        List<Long> billIds = tenantBillList.stream().map(TenantBill::getId).toList();

        // 2. 一次性查询所有 otherFees
        List<TenantBillOtherFee> allOtherFees = tenantBillOtherFeeRepo.getOtherFeesByBillIds(billIds);

        // 3. 按 billId 分组
        Map<Long, List<TenantBillOtherFeeVO>> otherFeesMap = allOtherFees.stream()
            .collect(Collectors.groupingBy(
                TenantBillOtherFee::getBillId,
                Collectors.mapping(of -> BeanCopyUtils.copyBean(of, TenantBillOtherFeeVO.class), Collectors.toList())
            ));

        // 4. 组装结果
        return tenantBillList.stream().map(tb -> {
            TenantBillListVO vo = BeanCopyUtils.copyBean(tb, TenantBillListVO.class);
            assert vo != null;
            vo.setOtherFees(otherFeesMap.getOrDefault(tb.getId(), List.of()));
            return vo;
        }).toList();
    }
}
