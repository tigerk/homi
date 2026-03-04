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
 * 企业账户表
 * </p>
 *
 * @author tk
 * @since 2026-03-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_account")
@Schema(name = "CompanyAccount", description = "企业账户表")
public class CompanyAccount implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "可用余额（元）")
    @TableField("balance")
    private BigDecimal balance;

    @Schema(description = "冻结金额（元）")
    @TableField("frozen_amount")
    private BigDecimal frozenAmount;

    @Schema(description = "累计充值（元）")
    @TableField("total_recharge")
    private BigDecimal totalRecharge;

    @Schema(description = "累计消费（元）")
    @TableField("total_consume")
    private BigDecimal totalConsume;

    @Schema(description = "状态：1正常，2冻结，-1禁用")
    @TableField("status")
    private Integer status;

    @Schema(description = "乐观锁版本号")
    @TableField("version")
    private Integer version;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除：0否，1是")
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
