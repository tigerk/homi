package com.homi.model.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
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

    @Schema(description = "月付单价")
    @NotNull(message = "月付单价不能为空")
    private BigDecimal monthPrice;

    @Schema(description = "年付总价")
    private BigDecimal yearPrice;

    @Schema(description = "房源数量")
    @NotNull(message = "房源数量不能为空")
    private Integer houseCount;

    @Schema(description = "是否为注册默认套餐：1是 0否")
    private Integer registerDefault;

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
