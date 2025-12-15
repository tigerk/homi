package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dto.tenant.TenantQueryDTO;
import com.homi.model.vo.tenant.TenantListVO;
import com.homi.model.vo.tenant.TenantTotalItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     * 获取租客状态统计
     *
     * @param tenantQueryDTO 查询参数
     * @return 租客状态统计
     */
    List<TenantTotalItemVO> getStatusTotal(@Param("query") TenantQueryDTO tenantQueryDTO);

    /**
     * 分页查询租客列表
     *
     * @param query 查询参数
     * @return 租客列表
     */
    IPage<TenantListVO> pageTenantList(IPage<TenantListVO> page, @Param("query") TenantQueryDTO query);
}
