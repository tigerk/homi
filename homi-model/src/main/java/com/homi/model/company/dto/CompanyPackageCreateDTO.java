package com.homi.model.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
@Schema(description = "公司套餐创建请求")
public class CompanyPackageCreateDTO {
    @Schema(description = "id，修改时需要传")
    private Long id;

    @Schema(description = "套餐名称")
    @NotBlank(message = "套餐名称不能为空")
    private String name;

    @Schema(description = "状态")
    private Integer status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @NotBlank(message = "备注不能为空")
    private String remark;

    @Schema(description = "公司套餐菜单ID列表")
    private List<Long> packageMenus;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", hidden = true)
    private Date createTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人", hidden = true)
    private Long createBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", hidden = true)
    private Date updateTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人", hidden = true)
    private Long updateBy;
}
