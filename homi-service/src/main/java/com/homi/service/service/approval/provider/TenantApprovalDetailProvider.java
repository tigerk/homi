package com.homi.service.service.approval.provider;

import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.model.approval.vo.ApprovalInstanceVO;
import com.homi.model.approval.vo.ApprovalTodoVO;
import com.homi.model.tenant.vo.LeaseDetailVO;
import com.homi.service.service.tenant.TenantService;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/29
 */

@Component
public class TenantApprovalDetailProvider implements ApprovalBizDetailProvider {

    private final TenantService tenantService;

    public TenantApprovalDetailProvider(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public String getBizType() {
        return ApprovalBizTypeEnum.TENANT_CHECKIN.getCode();
    }

    @Override
    public void fillTodoBizDetail(ApprovalTodoVO todoVO, Long bizId) {
        LeaseDetailVO leaseDetail = tenantService.getLeaseDetailById(bizId);
        if (Objects.nonNull(leaseDetail)) {
            todoVO.setTenantDetail(leaseDetail);
        }
    }

    @Override
    public void fillInstanceBizDetail(ApprovalInstanceVO instanceVO, Long bizId) {
        LeaseDetailVO leaseDetail = tenantService.getLeaseDetailById(bizId);
        if (Objects.nonNull(leaseDetail)) {
            instanceVO.setTenantDetail(leaseDetail);
        }
    }
}
