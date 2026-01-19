package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 物业交割明细表
 * </p>
 *
 * @author tk
 * @since 2026-01-19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("delivery_item")
@Schema(name = "DeliveryItem", description = "物业交割明细表")
public class DeliveryItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @Schema(description = "关联交割主表ID")
    @TableField("delivery_id")
    private Long deliveryId;

    @Schema(description = "类别：ASSET-资产物品, METER-能耗表计")
    @TableField("category")
    private String category;

    @Schema(description = "项目ID")
    @TableField("item_id")
    private String itemId;

    @Schema(description = "项目名称：如空调、电表、钥匙")
    @TableField("item_name")
    private String itemName;

    @Schema(description = "单位：如个、度、m³")
    @TableField("item_unit")
    private String itemUnit;

    @Schema(description = "交割前数值/状态 (对比参考)")
    @TableField("pre_value")
    private String preValue;

    @Schema(description = "当前交付数值/状态")
    @TableField("current_value")
    private String currentValue;

    @Schema(description = "是否损坏/异常：0-正常, 1-损坏")
    @TableField("damaged")
    private Boolean damaged;

    @Schema(description = "物品备注 (如：空调遥控器缺失)")
    @TableField("remarks")
    private String remarks;

    @Schema(description = "排序序号")
    @TableField("sort_order")
    private Integer sortOrder;
}
