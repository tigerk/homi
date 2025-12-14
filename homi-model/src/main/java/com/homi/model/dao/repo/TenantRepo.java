package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.mapper.TenantMapper;
import com.homi.model.dto.tenant.TenantQueryDTO;
import com.homi.model.vo.tenant.TenantListVO;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 租客信息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Service
public class TenantRepo extends ServiceImpl<TenantMapper, Tenant> {

    public PageVO<TenantListVO> queryTenantList(TenantQueryDTO query) {
        Page<TenantListVO> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        IPage<TenantListVO> tenantList = getBaseMapper().pageTenantList(page, query);

        // 封装返回结果
        PageVO<TenantListVO> pageResult = new PageVO<>();
        pageResult.setTotal(tenantList.getTotal());
        pageResult.setList(tenantList.getRecords());
        pageResult.setCurrentPage(tenantList.getCurrent());
        pageResult.setPageSize(tenantList.getSize());
        pageResult.setPages(tenantList.getPages());

        return pageResult;
    }

    public Tenant getTenantByIdNo(String idNo) {
        return getOne(new LambdaQueryWrapper<Tenant>().eq(Tenant::getIdNo, idNo));
    }
}
