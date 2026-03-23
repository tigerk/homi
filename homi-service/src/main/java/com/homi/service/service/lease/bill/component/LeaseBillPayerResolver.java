package com.homi.service.service.lease.bill.component;

import cn.hutool.core.util.ObjectUtil;
import com.homi.common.lib.enums.IdTypeEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantCompany;
import com.homi.model.dao.entity.TenantPersonal;
import com.homi.service.service.lease.bill.LeaseBillService;
import com.homi.service.service.lease.bill.PaymentApprovalService;
import com.homi.service.service.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 账单付款人信息解析器。
 *
 * <p>统一将 {@link Tenant} 解析为 {@link BillPayerInfo}，
 * 供 {@link LeaseBillService}、{@link PaymentApprovalService} 等共同复用，
 * 避免各处各自维护一套租客信息拼装逻辑。
 *
 * <p>解析规则：
 * <ul>
 *   <li>个人租客：从 {@link TenantPersonal} 取姓名、手机、证件信息。</li>
 *   <li>企业租客：从 {@link TenantCompany} 取联系人（缺省则用公司名）、联系电话、法人证件信息。</li>
 *   <li>其他/数据缺失：降级为 Tenant 表的 tenantName / tenantPhone，证件信息为空。</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class LeaseBillPayerResolver {
    private final TenantService tenantService;

    /**
     * 解析付款人展示信息与证件信息。
     *
     * @param tenant 租客实体，为 {@code null} 时返回全空结果
     * @return 付款人信息，永不返回 {@code null}
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
        // 未知租客类型：降级处理
        return BillPayerInfo.basic(tenant.getTenantName(), tenant.getTenantPhone());
    }

    // -------------------------------------------------------------------------
    // 私有：按类型解析
    // -------------------------------------------------------------------------
    private BillPayerInfo resolvePersonal(Tenant tenant) {
        TenantPersonal personal = tenantService.getPersonalDetail(tenant);
        if (personal == null) {
            return BillPayerInfo.basic(tenant.getTenantName(), tenant.getTenantPhone());
        }
        return new BillPayerInfo(
            personal.getName(),
            personal.getPhone(),
            personal.getIdType(),
            IdTypeEnum.getIdTypeName(personal.getIdType()),
            personal.getIdNo());
    }

    private BillPayerInfo resolveEnterprise(Tenant tenant) {
        TenantCompany company = tenantService.getCompanyDetail(tenant);
        if (company == null) {
            return BillPayerInfo.basic(tenant.getTenantName(), tenant.getTenantPhone());
        }
        return new BillPayerInfo(
            // 联系人姓名优先，缺省用公司名
            ObjectUtil.defaultIfNull(company.getContactName(), company.getCompanyName()),
            // 联系人电话优先，缺省用 tenant 表冗余字段
            ObjectUtil.defaultIfNull(company.getContactPhone(), tenant.getTenantPhone()),
            company.getLegalPersonIdType(),
            IdTypeEnum.getIdTypeName(company.getLegalPersonIdType()),
            company.getLegalPersonIdNo());
    }

    /**
     * 付款人信息（不可变值对象）。
     *
     * @param payerName       付款人姓名
     * @param payerPhone      付款人手机
     * @param payerIdType     证件类型 code
     * @param payerIdTypeName 证件类型名称
     * @param payerIdNo       证件号码
     */
    public record BillPayerInfo(
        String payerName,
        String payerPhone,
        Integer payerIdType,
        String payerIdTypeName,
        String payerIdNo
    ) {
        /**
         * 仅包含姓名和手机的降级结果。
         */
        public static BillPayerInfo basic(String payerName, String payerPhone) {
            return new BillPayerInfo(payerName, payerPhone, null, null, null);
        }

        /**
         * 全空结果（租客为 null 时使用）。
         */
        public static BillPayerInfo empty() {
            return new BillPayerInfo(null, null, null, null, null);
        }
    }
}
