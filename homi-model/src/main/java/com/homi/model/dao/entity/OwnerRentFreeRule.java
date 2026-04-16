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
 * 轻托管免租规则表
 * </p>
 *
 * @author tk
 * @since 2026-04-02
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_rent_free_rule")
@Schema(name = "OwnerRentFreeRule", description = "轻托管免租规则表")
public class OwnerRentFreeRule implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "SaaS企业ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "业主合同ID")
    @TableField("contract_id")
    private Long contractId;

    @Schema(description = "业主合同房源ID")
    @TableField("contract_subject_id")
    private Long contractSubjectId;

    @Schema(description = "是否启用免租")
    @TableField("enabled")
    private Boolean enabled;

    @Schema(description = "免租类型：BUILT_IN/OUTSIDE")
    @TableField("free_type")
    private String freeType;

    @Schema(description = "开始日期")
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDate;

    @Schema(description = "结束日期")
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;

    @Schema(description = "承担方式：PLATFORM/OWNER/SHARED")
    @TableField("bear_type")
    private String bearType;

    @Schema(description = "业主承担比例")
    @TableField("owner_ratio")
    private BigDecimal ownerRatio;

    @Schema(description = "平台承担比例")
    @TableField("platform_ratio")
    private BigDecimal platformRatio;

    @Schema(description = "计算方式：BY_DAYS/FIXED/RATIO")
    @TableField("calc_mode")
    private String calcMode;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "状态：1=启用，0=禁用")
    @TableField("status")
    private Integer status;

    @Schema(description = "是否删除：0=否，1=是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
