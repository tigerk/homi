package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.LeaseOtherFee;
import com.homi.model.dao.mapper.LeaseOtherFeeMapper;
import com.homi.model.room.dto.price.OtherFeeDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 租客其他费用 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-12-26
 */
@Service
public class LeaseOtherFeeRepo extends ServiceImpl<LeaseOtherFeeMapper, LeaseOtherFee> {

    /**
     * 根据租约ID获取其他费用列表
     *
     * @param leaseId 租约ID
     * @return 其他费用DTO列表
     */
    public List<OtherFeeDTO> getLeaseOtherFeeByLeaseId(Long leaseId) {
        return list(new LambdaQueryWrapper<LeaseOtherFee>()
            .eq(LeaseOtherFee::getLeaseId, leaseId))
            .stream()
            .map(tenantOtherFee -> new OtherFeeDTO(
                tenantOtherFee.getDictDataId(),
                tenantOtherFee.getName(),
                tenantOtherFee.getPaymentMethod(),
                tenantOtherFee.getPriceMethod(),
                tenantOtherFee.getPriceInput()))
            .collect(Collectors.toList());
    }
}
