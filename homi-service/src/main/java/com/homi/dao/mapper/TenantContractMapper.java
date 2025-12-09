package com.homi.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.domain.dto.tenant.TenantQueryDTO;
import com.homi.domain.vo.tenant.TenantTotalItemVO;
import com.homi.dao.entity.TenantContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 租赁合同信息表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Mapper
public interface TenantContractMapper extends BaseMapper<TenantContract> {

    /**
     * 获取租客状态统计
     *
     * @param tenantQueryDTO 查询参数
     * @return 租客状态统计
     */
    List<TenantTotalItemVO> getStatusTotal(@Param("query") TenantQueryDTO tenantQueryDTO);
}
