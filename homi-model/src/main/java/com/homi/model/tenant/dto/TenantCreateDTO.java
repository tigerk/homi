package com.homi.model.tenant.dto;

import com.homi.model.booking.dto.BookingIdDTO;
import com.homi.model.booking.vo.BookingListVO;
import com.homi.model.room.dto.price.OtherFeeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantCreateDTO {
    @Schema(description = "预订信息")
    private BookingIdDTO booking;

    @Schema(description = "个人租户信息")
    private TenantPersonalDTO tenantPersonal;

    @Schema(description = "企业租户信息")
    private TenantCompanyDTO tenantCompany;

    private List<TenantMateDTO> tenantMateList;

    @Schema(description = "租约信息")
    private LeaseDTO lease;

    private List<OtherFeeDTO> otherFees;

    @Schema(description = "创建人ID", hidden = true)
    private Long createBy;
}
