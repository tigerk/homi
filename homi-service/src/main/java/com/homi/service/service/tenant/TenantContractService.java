package com.homi.service.service.tenant;

import com.homi.common.lib.enums.contract.TenantParamsEnum;
import com.homi.common.lib.enums.tenant.TenantStatusEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.ContractTemplate;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantContract;
import com.homi.model.dao.repo.ContractTemplateRepo;
import com.homi.model.dao.repo.TenantContractRepo;
import com.homi.model.dao.repo.TenantRepo;
import com.homi.model.tenant.dto.TenantContractGenerateDTO;
import com.homi.model.contract.vo.TenantContractVO;
import com.homi.model.tenant.vo.TenantContractSignStatusUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

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
        if (Objects.isNull(tenantContract)) {
            return null;
        }

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
    public TenantContract addTenantContract(Long contractTemplateId, Tenant tenant) {
        ContractTemplate contractTemplate = contractTemplateRepo.getById(contractTemplateId);

        TenantContract tenantContract = tenantContractRepo.getTenantContractByTenantId(tenant.getId());
        if (Objects.isNull(tenantContract)) {
            tenantContract = new TenantContract();
        }

        tenantContract.setTenantId(tenant.getId());
        // todo 按照租客信息来替换所有的 ${} 变量
        tenantContract.setContractTemplateId(contractTemplateId);
        tenantContract.setContractContent(replaceContractVariables(contractTemplate.getTemplateContent(), tenant));
        tenantContract.setSignStatus(0);
        tenantContract.setDeleted(false);

        // 如果租客合同已存在，更新合同内容

        if (Objects.nonNull(tenantContract.getId())) {
            tenantContractRepo.updateById(tenantContract);
        } else {
            tenantContractRepo.save(tenantContract);
        }

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
    @Transactional(rollbackFor = Exception.class)
    public TenantContractVO generateTenantContractByTenantId(TenantContractGenerateDTO query) {
        Tenant tenant = tenantRepo.getById(query.getTenantId());
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant not found");
        }

        // 删除旧合同
        tenantContractRepo.removeById(query.getTenantContractId());

        // 添加新合同
        TenantContract tenantContract = addTenantContract(query.getContractTemplateId(), tenant);

        // 租客重置为待签约状态
        tenantRepo.updateStatusById(tenant.getId(), TenantStatusEnum.TO_SIGN.getCode());

        return BeanCopyUtils.copyBean(tenantContract, TenantContractVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTenantContractSignStatus(TenantContractSignStatusUpdateDTO query) {
        TenantContract tenantContract = tenantContractRepo.getById(query.getTenantContractId());
        if (tenantContract == null) {
            throw new IllegalArgumentException("未找到指定的租客合同");
        }

        Tenant tenant = tenantRepo.getById(tenantContract.getTenantId());
        if (tenant == null) {
            throw new IllegalArgumentException("未找到租客！");
        }

        if (Objects.equals(tenant.getStatus(), TenantStatusEnum.CANCELLED.getCode()) || Objects.equals(tenant.getStatus(), TenantStatusEnum.TERMINATED.getCode())) {
            throw new IllegalArgumentException("租客已取消或已终止，无法签署合同！");
        }

        // 更新租客状态为有效
        boolean isUpdateSuccess = tenantRepo.updateStatusById(tenantContract.getTenantId(), TenantStatusEnum.EFFECTIVE.getCode());
        if (!isUpdateSuccess) {
            throw new IllegalArgumentException("更新租客状态失败！");
        }

        tenantContract.setSignStatus(query.getSignStatus());
        tenantContractRepo.updateById(tenantContract);

        return true;
    }

    public Boolean deleteTenantContract(Long tenantContractId) {
        TenantContract tenantContract = tenantContractRepo.getById(tenantContractId);
        if (tenantContract == null) {
            throw new IllegalArgumentException("未找到指定的租客合同");
        }

        tenantContractRepo.removeById(tenantContractId);

        return true;
    }

    public Integer cancelTenant(Long tenantId) {
        Tenant tenant = tenantRepo.getById(tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("未找到指定的租客");
        }

        tenantRepo.updateStatusById(tenantId, TenantStatusEnum.CANCELLED.getCode());

        return TenantStatusEnum.CANCELLED.getCode();
    }
}
