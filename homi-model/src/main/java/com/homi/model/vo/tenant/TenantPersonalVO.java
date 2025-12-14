package com.homi.model.vo.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantPersonalVO {
    @Schema(description = "租客ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "租客姓名")
    private String name;

    @Schema(description = "性别：1=男，2=女")
    private Integer gender;

    @Schema(description = "证件类型：0=身份证，1=护照，2=港澳通行证，3=台胞证")
    private Integer idType;

    @Schema(description = "证件号码")
    private String idNo;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "租客标签")
    private String tags;

    @Schema(description = "租客备注")
    private String remark;

    @Schema(description = "租客状态：0=停用，1=启用")
    private Integer status;
}
