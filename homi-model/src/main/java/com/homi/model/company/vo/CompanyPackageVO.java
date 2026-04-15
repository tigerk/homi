package com.homi.model.company.vo;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/16
 */

@Data
@Schema(description = "公司套餐列表")
public class CompanyPackageVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 套餐名称
     */
    private String name;

    @Schema(description = "月付单价")
    private BigDecimal monthPrice;

    @Schema(description = "年付总价")
    private BigDecimal yearPrice;

    @Schema(description = "套餐可配置的房屋数量")
    private Integer houseCount;

    @Schema(description = "是否为注册默认套餐：1是 0否")
    private Integer registerDefault;

    /**
     * 关联菜单id
     */
    private List<Long> packageMenus;

    /**
     * 状态（0正常，-1禁用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新时间
     */
    private Date updateAt;

    /**
     * 更新人
     */
    private Long updateBy;
}
