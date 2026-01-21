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

@Service
@RequiredArgsConstructor
public class TenantBillService {
    private final TenantBillRepo tenantBillRepo;
    private final TenantBillOtherFeeRepo tenantBillOtherFeeRepo;

    public List<TenantBillListVO> getBillListByTenantId(Long tenantId, Boolean valid) {
        List<TenantBill> tenantBillList = tenantBillRepo.getBillListByTenantId(tenantId, valid);

        return tenantBillList.stream().map(tb -> {
            TenantBillListVO vo = BeanCopyUtils.copyBean(tb, TenantBillListVO.class);
            List<TenantBillOtherFee> otherFees = tenantBillOtherFeeRepo.getOtherFeesByBillId(tb.getId());
            List<TenantBillOtherFeeVO> otherFeeListVOS = otherFees.stream().map(of -> BeanCopyUtils.copyBean(of, TenantBillOtherFeeVO.class)).toList();
            assert vo != null;
            vo.setOtherFees(otherFeeListVOS);
            return vo;
        }).toList();
    }
}
