package com.homi.service.service.tenant;

import cn.hutool.core.util.ObjectUtil;
import com.homi.common.lib.enums.IdTypeEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantCompany;
import com.homi.model.dao.entity.TenantPersonal;
import com.homi.model.dao.repo.TenantCompanyRepo;
import com.homi.model.dao.repo.TenantPersonalRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 统一解析账单付款人信息，避免账单服务与审批服务各自维护一套租客信息拼装逻辑。
 */
@Component
@RequiredArgsConstructor
public class LeaseBillPayerResolver {
    private final TenantPersonalRepo tenantPersonalRepo;
    private final TenantCompanyRepo tenantCompanyRepo;

    /**
     * 解析付款人展示信息与证件信息。
     */
    public BillPayerInfo resolve(Tenant tenant) {
        if (tenant == null) {
            return BillPayerInfo.empty();
        }
        if (TenantTypeEnum.PERSONAL.getCode().equals(tenant.getTenantType())) {
            return resolvePersonal(tenant);
        }
        if (TenantTypeEnum.ENTERPRISE.getCode().equals(tenant.getTenantType())) {
            return resolveEnterprise(tenant);
        }
        return BillPayerInfo.basic(tenant.getTenantName(), tenant.getTenantPhone());
    }

    private BillPayerInfo resolvePersonal(Tenant tenant) {
        TenantPersonal personal = getTenantPersonal(tenant);
        if (personal == null) {
            return BillPayerInfo.basic(tenant.getTenantName(), tenant.getTenantPhone());
        }
        return new BillPayerInfo(
            personal.getName(),
            personal.getPhone(),
            personal.getIdType(),
            getIdTypeName(personal.getIdType()),
            personal.getIdNo()
        );
    }

    private BillPayerInfo resolveEnterprise(Tenant tenant) {
        TenantCompany company = getTenantCompany(tenant);
        if (company == null) {
            return BillPayerInfo.basic(tenant.getTenantName(), tenant.getTenantPhone());
        }
        return new BillPayerInfo(
            ObjectUtil.defaultIfNull(company.getContactName(), company.getCompanyName()),
            ObjectUtil.defaultIfNull(company.getContactPhone(), tenant.getTenantPhone()),
            company.getLegalPersonIdType(),
            getIdTypeName(company.getLegalPersonIdType()),
            company.getLegalPersonIdNo()
        );
    }

    private TenantPersonal getTenantPersonal(Tenant tenant) {
        if (tenant.getTenantTypeId() == null) {
            return null;
        }
        return tenantPersonalRepo.getById(tenant.getTenantTypeId());
    }

    private TenantCompany getTenantCompany(Tenant tenant) {
        if (tenant.getTenantTypeId() == null) {
            return null;
        }
        return tenantCompanyRepo.getById(tenant.getTenantTypeId());
    }

    private String getIdTypeName(Integer idType) {
        if (idType == null) {
            return null;
        }
        return Arrays.stream(IdTypeEnum.values())
            .filter(item -> item.getCode().equals(idType))
            .map(IdTypeEnum::getName)
            .findFirst()
            .orElse(null);
    }

    public record BillPayerInfo(
        String payerName,
        String payerPhone,
        Integer payerIdType,
        String payerIdTypeName,
        String payerIdNo
    ) {
        public static BillPayerInfo basic(String payerName, String payerPhone) {
            return new BillPayerInfo(payerName, payerPhone, null, null, null);
        }

        public static BillPayerInfo empty() {
            return new BillPayerInfo(null, null, null, null, null);
        }
    }
}
