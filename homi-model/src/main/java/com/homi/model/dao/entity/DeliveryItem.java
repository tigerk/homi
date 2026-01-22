package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 物业交割明细表
 * </p>
 *
 * @author tk
 * @since 2026-01-22
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
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联交割主表ID")
    @TableField("delivery_id")
    private Long deliveryId;

    @Schema(description = "交割项编码(字典数据项value)")
    @TableField("item_code")
    private String itemCode;

    @Schema(description = "交割项名称")
    @TableField("item_name")
    private String itemName;

    @Schema(description = "项目分类: UTILITY-水电气,FACILITY-设施")
    @TableField("item_category")
    private String itemCategory;

    @Schema(description = "交割前数值/状态(对比参考)")
    @TableField("pre_value")
    private String preValue;

    @Schema(description = "当前交付数值/状态")
    @TableField("current_value")
    private String currentValue;

    @Schema(description = "单位(如: 度、m³、元、个)")
    @TableField("item_unit")
    private String itemUnit;

    @Schema(description = "是否损坏/异常: 0-正常, 1-损坏")
    @TableField("damaged")
    private Boolean damaged;

    @Schema(description = "备注(如: 空调遥控器缺失)")
    @TableField("remark")
    private String remark;

    @Schema(description = "排序序号")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
