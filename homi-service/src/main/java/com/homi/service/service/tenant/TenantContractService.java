package com.homi.service.service.tenant;

import com.homi.common.lib.enums.contract.TenantParamsEnum;
import com.homi.model.dao.entity.ContractTemplate;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantContract;
import com.homi.model.dao.repo.ContractTemplateRepo;
import com.homi.model.dao.repo.TenantContractRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 租客
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@Service
@RequiredArgsConstructor
public class TenantContractService {
    private final TenantContractRepo tenantContractRepo;
    private final ContractTemplateRepo contractTemplateRepo;

    public TenantContract addTenantContract(Long contractTemplateId, Tenant tenant) {
        ContractTemplate contractTemplate = contractTemplateRepo.getById(contractTemplateId);

        TenantContract tenantContract = new TenantContract();
        tenantContract.setTenantId(tenant.getId());
        // todo 按照租客信息来替换所有的 ${} 变量
        tenantContract.setContractTemplateId(contractTemplateId);
        tenantContract.setContractContent(replaceContractVariables(contractTemplate.getTemplateContent(), tenant));
        tenantContract.setSignStatus(0);
        tenantContract.setDeleted(false);
        tenantContractRepo.save(tenantContract);

        return tenantContract;
    }

    private String replaceContractVariables(String contractContent, Tenant tenant) {
        // 替换 ${tenantName} 为租客姓名
        contractContent = contractContent.replace(TenantParamsEnum.TENANT_NAME.getKey(), tenant.getTenantName());
        // 替换 ${tenantPhone} 为租客手机号
        contractContent = contractContent.replace(TenantParamsEnum.TENANT_PHONE.getKey(), tenant.getTenantPhone());
        // 替换 ${contractStartDate} 为合同开始日期
        contractContent = contractContent.replace(TenantParamsEnum.LEASE_START.getKey(), tenant.getLeaseStart().toString());
        // 替换 ${contractEndDate} 为合同结束日期
        contractContent = contractContent.replace(TenantParamsEnum.LEASE_END.getKey(), tenant.getLeaseEnd().toString());
        // 替换 ${rentalAmount} 为租金金额
        contractContent = contractContent.replace(TenantParamsEnum.RENTAL_PRICE.getKey(), String.valueOf(tenant.getRentalPrice()));
        return contractContent;
    }
}
