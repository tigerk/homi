package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 企业商品表
 * </p>
 *
 * @author tk
 * @since 2026-03-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_product")
@Schema(name = "CompanyProduct", description = "企业商品表")
public class CompanyProduct implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "商品编码：HOUSE/CONTRACT/SMS/ID_AUTH/ZHIMA_HOUSE/YUMENG_HOUSE/ALIPAY_HOUSE/ZHIMA_CREDIT")
    @TableField("product_code")
    private String productCode;

    @Schema(description = "商品名称")
    @TableField("product_name")
    private String productName;

    @Schema(description = "单位：间/份/次/条/个")
    @TableField("unit")
    private String unit;

    @Schema(description = "单价（元）")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    @Schema(description = "最小购买数量")
    @TableField("min_quantity")
    private Integer minQuantity;

    @Schema(description = "商品介绍")
    @TableField("description")
    private String description;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "状态：1上架，0下架")
    @TableField("status")
    private Integer status;

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
