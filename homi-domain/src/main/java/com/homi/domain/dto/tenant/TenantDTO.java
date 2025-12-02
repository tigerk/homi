package com.homi.domain.dto.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/2
 */
@Data
public class TenantDTO {
    @Schema(description = "租户ID")
    private Integer id;

    @Schema(description = "公司ID")
    private Integer companyId;

    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "证件类型")
    private Integer idType;

    @Schema(description = "证件号码")
    private String idNo;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "身份证正面照片")
    private List<String> idCardFrontList;

    @Schema(description = "身份证反面照片")
    private List<String> idCardBackList;

    @Schema(description = "手持身份证照片")
    private List<String> idCardInHandList;

    @Schema(description = "其他照片")
    private List<String> otherImageList;
}
