package com.homi.model.vo.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
public class TenantListVO {
    @Schema(description = "租客 ID")
    private Long id;


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

    @Schema(description = "租客状态：0=正常，1=禁用")
    private Integer status;

    @Schema(description = "租客备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
