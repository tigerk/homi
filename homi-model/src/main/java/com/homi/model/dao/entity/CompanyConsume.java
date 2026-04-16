package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 企业配额消费记录表
 * </p>
 *
 * @author tk
 * @since 2026-03-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_consume")
@Schema(name = "CompanyConsume", description = "企业配额消费记录表")
public class CompanyConsume implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "消费流水号")
    @TableField("consume_no")
    private String consumeNo;

    @Schema(description = "关联购买订单ID")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "商品编码")
    @TableField("product_code")
    private String productCode;

    @Schema(description = "业务类型：SMS/CONTRACT/ID_AUTH/HOUSE/...")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "业务关联ID")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "业务单号")
    @TableField("biz_no")
    private String bizNo;

    @Schema(description = "消耗数量")
    @TableField("quantity")
    private Integer quantity;

    @Schema(description = "状态：1成功，2失败，3已退还")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @TableField("create_by")
    private Long createBy;

    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @TableField("update_by")
    private Long updateBy;

    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
