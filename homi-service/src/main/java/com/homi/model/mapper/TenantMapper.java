package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.domain.dto.tenant.TenantQueryDTO;
import com.homi.domain.vo.tenant.TenantListVO;
import com.homi.model.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 租客信息表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
    /**
     * 分页查询租客列表
     *
     * @param query 查询参数
     * @return 租客列表
     */
    IPage<TenantListVO> pageTenantList(IPage<TenantListVO> page, @Param("query")TenantQueryDTO query);
}
