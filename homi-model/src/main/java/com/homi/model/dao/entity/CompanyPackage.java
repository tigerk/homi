package com.homi.model.dao.entity;

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
 * 公司套餐表
 * </p>
 *
 * @author tk
 * @since 2026-03-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_package")
@Schema(name = "CompanyPackage", description = "公司套餐表")
public class CompanyPackage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @Schema(description = "套餐名称")
    @TableField("name")
    private String name;

    @Schema(description = "关联菜单id")
    @TableField("package_menus")
    private String packageMenus;

    @Schema(description = "月付单价")
    @TableField("month_price")
    private BigDecimal monthPrice;

    @Schema(description = "年付总价（NULL表示无年付优惠）")
    @TableField("year_price")
    private BigDecimal yearPrice;

    @Schema(description = "房源数量")
    @TableField("house_count")
    private Integer houseCount;

    @Schema(description = "是否为注册默认套餐")
    @TableField("register_default")
    private Integer registerDefault;

    @Schema(description = "状态（0正常，-1禁用）")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "更新时间")
    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;
}
