package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.Lease;
import com.homi.model.dao.mapper.LeaseMapper;
import com.homi.model.tenant.dto.TenantQueryDTO;
import com.homi.model.tenant.vo.LeaseListVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaseRepo extends ServiceImpl<LeaseMapper, Lease> {

    public PageVO<LeaseListVO> queryLeaseList(TenantQueryDTO query) {
        return queryLeaseList(query, null);
    }

    public PageVO<LeaseListVO> queryLeaseList(TenantQueryDTO query, List<Long> tenantIds) {
        Page<Lease> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        LambdaQueryWrapper<Lease> wrapper = new LambdaQueryWrapper<>();
        if (query.getStatus() != null) {
            wrapper.eq(Lease::getStatus, query.getStatus());
        }

        if (tenantIds != null) {
            if (tenantIds.isEmpty()) {
                return new PageVO<>();
            }
            wrapper.in(Lease::getTenantId, tenantIds);
        }

        if (query.getRoomId() != null) {
            wrapper.apply(
                "JSON_CONTAINS(room_ids,JSON_ARRAY('{0}'))",
                query.getRoomId()
            );
        }

        wrapper.orderByDesc(Lease::getId);

        IPage<Lease> leaseList = getBaseMapper().selectPage(page, wrapper);

        PageVO<LeaseListVO> pageResult = new PageVO<>();
        pageResult.setTotal(leaseList.getTotal());
        pageResult.setList(leaseList.getRecords().stream()
            .map(lease -> {
                LeaseListVO vo = new LeaseListVO();
                vo.setLeaseId(lease.getId());
                vo.setTenantId(lease.getTenantId());
                vo.setContractNature(lease.getContractNature());
                vo.setCompanyId(lease.getCompanyId());
                vo.setDeptId(lease.getDeptId());
                vo.setRoomIds(lease.getRoomIds());
                vo.setRentPrice(lease.getRentPrice());
                vo.setDepositMonths(lease.getDepositMonths());
                vo.setPaymentMonths(lease.getPaymentMonths());
                vo.setLeaseStart(lease.getLeaseStart());
                vo.setLeaseEnd(lease.getLeaseEnd());
                vo.setCheckInTime(lease.getCheckInTime());
                vo.setCheckOutTime(lease.getCheckOutTime());
                vo.setOriginalLeaseStart(lease.getOriginalLeaseStart());
                vo.setOriginalLeaseEnd(lease.getOriginalLeaseEnd());
                vo.setLeaseDurationDays(lease.getLeaseDurationDays());
                vo.setRentDueType(lease.getRentDueType());
                vo.setRentDueDay(lease.getRentDueDay());
                vo.setRentDueOffsetDays(lease.getRentDueOffsetDays());
                vo.setSalesmanId(lease.getSalesmanId());
                vo.setHelperId(lease.getHelperId());
                vo.setSignStatus(lease.getSignStatus());
                vo.setCheckOutStatus(lease.getCheckOutStatus());
                vo.setStatus(lease.getStatus());
                vo.setTenantSource(lease.getTenantSource());
                vo.setDealChannel(lease.getDealChannel());
                vo.setRemark(lease.getRemark());
                vo.setDeleted(lease.getDeleted());
                vo.setCreateBy(lease.getCreateBy());
                vo.setCreateTime(lease.getCreateTime());
                vo.setUpdateBy(lease.getUpdateBy());
                vo.setUpdateTime(lease.getUpdateTime());
                return vo;
            })
            .toList());
        pageResult.setCurrentPage(leaseList.getCurrent());
        pageResult.setPageSize(leaseList.getSize());
        pageResult.setPages(leaseList.getPages());

        return pageResult;
    }

    public List<Lease> getLeasesByTenantId(Long tenantId) {
        return lambdaQuery()
            .eq(Lease::getTenantId, tenantId)
            .orderByDesc(Lease::getId)
            .list();
    }

    public Lease getCurrentLeaseByTenantId(Long tenantId, List<Integer> validStatuses) {
        return lambdaQuery()
            .eq(Lease::getTenantId, tenantId)
            .in(Lease::getStatus, validStatuses)
            .orderByDesc(Lease::getId)
            .last("LIMIT 1")
            .one();
    }

    public boolean updateStatusById(Long leaseId, Integer status) {
        Lease lease = new Lease();
        lease.setId(leaseId);
        lease.setStatus(status);
        return updateById(lease);
    }

    public boolean updateStatusAndApprovalStatus(Long leaseId, Integer status, Integer approvalStatus) {
        Lease lease = new Lease();
        lease.setId(leaseId);
        lease.setStatus(status);
        lease.setApprovalStatus(approvalStatus);
        return updateById(lease);
    }

    public boolean updateApprovalStatus(Long leaseId, Integer approvalStatus) {
        Lease lease = new Lease();
        lease.setId(leaseId);
        lease.setApprovalStatus(approvalStatus);
        return updateById(lease);
    }
}
