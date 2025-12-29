package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.TenantOtherFee;
import com.homi.model.dao.mapper.TenantOtherFeeMapper;
import com.homi.model.dto.room.price.OtherFeeDTO;
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
public class TenantOtherFeeRepo extends ServiceImpl<TenantOtherFeeMapper, TenantOtherFee> {

    /**
     * 根据租客ID获取其他费用列表
     *
     * @param tenantId 租客ID
     * @return 其他费用DTO列表
     */
    public List<OtherFeeDTO> getTenantOtherFeeByTenantId(Long tenantId) {
        return list(new LambdaQueryWrapper<TenantOtherFee>()
            .eq(TenantOtherFee::getTenantId, tenantId))
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
