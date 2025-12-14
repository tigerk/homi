package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.TenantContract;
import com.homi.model.dao.mapper.TenantContractMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dto.tenant.TenantQueryDTO;
import com.homi.model.vo.tenant.TenantContractListVO;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 租赁合同信息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Service
public class TenantContractRepo extends ServiceImpl<TenantContractMapper, TenantContract> {

    public PageVO<TenantContractListVO> queryTenantContractList(TenantQueryDTO query) {
        Page<TenantContractListVO> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        IPage<TenantContractListVO> tenantList = getBaseMapper().pageTenantList(page, query);

        // 封装返回结果
        PageVO<TenantContractListVO> pageResult = new PageVO<>();
        pageResult.setTotal(tenantList.getTotal());
        pageResult.setList(tenantList.getRecords());
        pageResult.setCurrentPage(tenantList.getCurrent());
        pageResult.setPageSize(tenantList.getSize());
        pageResult.setPages(tenantList.getPages());

        return pageResult;
    }
}
