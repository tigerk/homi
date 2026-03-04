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
 * 企业配额表
 * </p>
 *
 * @author tk
 * @since 2026-03-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_quota")
@Schema(name = "CompanyQuota", description = "企业配额表")
public class CompanyQuota implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "商品编码")
    @TableField("product_code")
    private String productCode;

    @Schema(description = "总配额")
    @TableField("total_quota")
    private Integer totalQuota;

    @Schema(description = "已用配额")
    @TableField("used_quota")
    private Integer usedQuota;

    @Schema(description = "冻结中的配额（操作进行中）")
    @TableField("frozen_quota")
    private Integer frozenQuota;

    @Schema(description = "有效期（NULL表示永不过期）")
    @TableField("expire_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireDate;

    @Schema(description = "乐观锁版本号")
    @TableField("version")
    private Integer version;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @TableField("create_by")
    private Long createBy;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField("update_by")
    private Long updateBy;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
