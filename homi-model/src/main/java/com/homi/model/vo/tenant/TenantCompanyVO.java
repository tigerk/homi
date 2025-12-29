package com.homi.model.vo.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
public class TenantCompanyVO {
    @Schema(description = "企业租客ID")
    private Long id;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "统一社会信用代码")
    private String uscc;

    @Schema(description = "法定代表人")
    private String legalPerson;

    @Schema(description = "法人证件类型")
    private Integer legalPersonIdType;

    @Schema(description = "法人证件号码")
    private String legalPersonIdNo;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "注册地址")
    private String registeredAddress;

    @Schema(description = "租客标签")
    private String tags;

    @Schema(description = "租客备注")
    private String remark;

    @Schema(description = "租客状态：0=停用，1=启用")
    private Integer status;

    @Schema(description = "营业执照附件列表")
    private List<String> businessLicenseList;

    @Schema(description = "其他附件列表")
    private List<String> otherImageList;
}
