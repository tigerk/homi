package com.homi.service.service.tenant;

import com.homi.common.lib.enums.contract.TenantParamsEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.ContractTemplate;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantContract;
import com.homi.model.dao.repo.ContractTemplateRepo;
import com.homi.model.dao.repo.TenantContractRepo;
import com.homi.model.dao.repo.TenantRepo;
import com.homi.model.dto.tenant.TenantContractGenerateDTO;
import com.homi.model.vo.contract.TenantContractVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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
    private final TenantRepo tenantRepo;
    private final TenantContractRepo tenantContractRepo;
    private final ContractTemplateRepo contractTemplateRepo;

    /**
     * 根据租客ID查询租客合同
     *
     * @param tenantId 租客ID
     * @return 租客合同
     */
    public TenantContractVO getTenantContractByTenantId(Long tenantId) {
        TenantContract tenantContract = tenantContractRepo.getTenantContractByTenantId(tenantId);

        TenantContractVO tenantContractByTenantId = BeanCopyUtils.copyBean(tenantContract, TenantContractVO.class);
        assert tenantContractByTenantId != null;
        ContractTemplate contractTemplate = contractTemplateRepo.getById(tenantContractByTenantId.getContractTemplateId());
        tenantContractByTenantId.setContractTemplateName(contractTemplate.getTemplateName());

        return tenantContractByTenantId;
    }

    /**
     * 添加租客合同
     *
     * @param contractTemplateId 合同模板ID
     * @param tenant             租客
     * @return 租客合同
     */
    public TenantContract saveTenantContract(Long contractTemplateId, Tenant tenant) {
        ContractTemplate contractTemplate = contractTemplateRepo.getById(contractTemplateId);

        TenantContract tenantContract = new TenantContract();
        tenantContract.setTenantId(tenant.getId());
        // todo 按照租客信息来替换所有的 ${} 变量
        tenantContract.setContractTemplateId(contractTemplateId);
        tenantContract.setContractContent(replaceContractVariables(contractTemplate.getTemplateContent(), tenant));
        tenantContract.setSignStatus(0);
        tenantContract.setDeleted(false);

        // 如果租客合同已存在，更新合同内容
        TenantContract existingContract = tenantContractRepo.getTenantContractByTenantId(tenant.getId());
        if (existingContract != null) {
            BeanUtils.copyProperties(tenantContract, existingContract);
            tenantContractRepo.updateById(existingContract);
            return existingContract;
        } else {
            tenantContractRepo.save(tenantContract);
            return tenantContract;
        }
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
        contractContent = contractContent.replace(TenantParamsEnum.RENTAL_PRICE.getKey(), String.valueOf(tenant.getRentPrice()));
        return contractContent;
    }

    /**
     * 根据租客ID生成租客合同
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/29 19:20
     *
     * @param query 参数说明
     * @return java.lang.String
     */
    public String generateTenantContractByTenantId(TenantContractGenerateDTO query) {
        Tenant tenant = tenantRepo.getById(query.getTenantId());
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant not found");
        }

        TenantContract tenantContract = saveTenantContract(query.getContractTemplateId(), tenant);
        return tenantContract.getContractContent();
    }
}
