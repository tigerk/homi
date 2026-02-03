package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.tenant.TenantStatusEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.mapper.TenantMapper;
import com.homi.model.tenant.dto.TenantQueryDTO;
import com.homi.model.tenant.vo.TenantListVO;
import org.apache.commons.lang3.StringUtils;
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
        Page<Tenant> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNoneBlank(query.getPhone())) {
            wrapper.eq(Tenant::getTenantPhone, query.getPhone());
        }

        if (StringUtils.isNoneBlank(query.getName())) {
            wrapper.like(Tenant::getTenantName, query.getName());
        }

        if (query.getTenantType() != null) {
            wrapper.eq(Tenant::getTenantType, query.getTenantType());
        }

        if (query.getStatus() != null) {
            wrapper.eq(Tenant::getStatus, query.getStatus());
        }

        if (query.getRoomId() != null) {
            wrapper.apply(
                "JSON_CONTAINS(room_ids,JSON_ARRAY('{0}'))",
                query.getRoomId()
            );
        }

        wrapper.orderByDesc(Tenant::getId);

        IPage<Tenant> tenantList = getBaseMapper().selectPage(page, wrapper);

        // 封装返回结果
        PageVO<TenantListVO> pageResult = new PageVO<>();
        pageResult.setTotal(tenantList.getTotal());
        pageResult.setList(tenantList.getRecords().stream()
            .map(t -> BeanCopyUtils.copyBean(t, TenantListVO.class))
            .toList());
        pageResult.setCurrentPage(tenantList.getCurrent());
        pageResult.setPageSize(tenantList.getSize());
        pageResult.setPages(tenantList.getPages());

        return pageResult;
    }

    public boolean updateStatusById(Long tenantId, Integer code) {
        Tenant tenant = getById(tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("未找到租客！");
        }

        tenant.setStatus(code);
        return updateById(tenant);
    }

    /**
     * 根据房间ID查询当前租客
     *
     * @param roomId 房间ID
     * @return 当前租客
     */
    public Tenant getCurrentTenantByRoomId(Long roomId) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Tenant::getStatus, TenantStatusEnum.getValidStatus());
        wrapper.like(Tenant::getRoomIds, roomId);

        return getOne(wrapper);
    }

    public boolean updateApprovalStatus(Long tenantId, Integer bizApprovalStatus) {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setApprovalStatus(bizApprovalStatus);

        return updateById(tenant);
    }

    /**
     * 更新租客状态和审批状态
     *
     * @param tenantId       租客ID
     * @param status         租客状态
     * @param approvalStatus 租客审批状态
     * @return 是否初始化成功
     */
    public boolean updateStatusAndApprovalStatus(Long tenantId, Integer status, Integer approvalStatus) {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setStatus(status);
        tenant.setApprovalStatus(approvalStatus);

        return updateById(tenant);
    }
}
