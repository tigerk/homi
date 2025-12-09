package com.homi.service.tenant;

import com.homi.domain.base.PageVO;
import com.homi.domain.dto.tenant.TenantCreateDTO;
import com.homi.domain.dto.tenant.TenantQueryDTO;
import com.homi.domain.enums.tenant.TenantContractStatusEnum;
import com.homi.domain.vo.tenant.TenantListVO;
import com.homi.domain.vo.tenant.TenantTotalItemVO;
import com.homi.dao.repo.TenantContractRepo;
import com.homi.dao.repo.TenantRepo;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租客
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepo tenantRepo;

    private final TenantContractRepo tenantContractRepo;

    /**
     * 获取租客列表
     *
     * @param query 查询参数
     * @return 租客列表
     */
    public PageVO<TenantListVO> getTenantList(TenantQueryDTO query) {
        return tenantRepo.queryTenantList(query);
    }

    public Long createTenant(TenantCreateDTO createDTO) {
        return null;
    }

    public List<TenantTotalItemVO> getTenantStatusTotal(TenantQueryDTO query) {
        Map<Integer, TenantTotalItemVO> result = initTenantTotalItemMap();

        List<TenantTotalItemVO> statusTotal = tenantContractRepo.getBaseMapper().getStatusTotal(query);
        statusTotal.forEach(tenantTotalItemVO -> {
            TenantTotalItemVO orDefault = result.getOrDefault(tenantTotalItemVO.getStatus(), tenantTotalItemVO);
            orDefault.setTotal(tenantTotalItemVO.getTotal());
        });

        return result.values().stream().toList();
    }

    /**
     * 获取房间状态枚举映射
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/7 19:12
     *
     * @return java.util.Map<java.lang.Integer, com.homi.domain.vo.room.RoomTotalItemVO>
     */
    private @NotNull Map<Integer, TenantTotalItemVO> initTenantTotalItemMap() {
        Map<Integer, TenantTotalItemVO> result = new HashMap<>();
        TenantContractStatusEnum[] values = TenantContractStatusEnum.values();
        for (TenantContractStatusEnum contractStatusEnum : values) {
            TenantTotalItemVO tenantTotalItemVO = new TenantTotalItemVO();
            tenantTotalItemVO.setStatus(contractStatusEnum.getCode());
            tenantTotalItemVO.setStatusName(contractStatusEnum.getName());
            tenantTotalItemVO.setTotal(0);
            result.put(contractStatusEnum.getCode(), tenantTotalItemVO);
        }
        return result;
    }
}
