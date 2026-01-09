package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 租客其他费用
 * </p>
 *
 * @author tk
 * @since 2025-12-26
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant_other_fee")
@Schema(name = "TenantOtherFee", description = "租客其他费用")
public class TenantOtherFee implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "租客ID")
    @TableField("tenant_id")
    private Long tenantId;

    @Schema(description = "其他费用 ID")
    @TableField("dict_data_id")
    private Long dictDataId;

    @Schema(description = "其他费用名称")
    @TableField("name")
    private String name;

    @Schema(description = "付款方式（如：随房租付、按固定金额等）")
    @TableField("payment_method")
    private Integer paymentMethod;

    @Schema(description = "价格计算方式")
    @TableField("price_method")
    private Integer priceMethod;

    @Schema(description = "价格输入值")
    @TableField("price_input")
    private BigDecimal priceInput;

    @Schema(description = "是否删除：0=否，1=是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
