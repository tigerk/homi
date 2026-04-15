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
 * 第三方签章供应商信息
 * </p>
 *
 * @author tk
 * @since 2026-03-10
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("contract_seal_provider")
@Schema(name = "ContractSealProvider", description = "第三方签章供应商信息")
public class ContractSealProvider implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联主表ID")
    @TableField("seal_id")
    private Long sealId;

    @Schema(description = "服务商平台的账号/企业ID")
    @TableField("account_id")
    private String accountId;

    @Schema(description = "服务商平台的印章ID")
    @TableField("provider_seal_id")
    private String providerSealId;

    @Schema(description = "认证状态:0=未认证,1=认证中,2=已认证,3=失败")
    @TableField("auth_status")
    private Integer authStatus;

    @Schema(description = "认证完成时间")
    @TableField("auth_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date authAt;

    @Schema(description = "授权到期时间")
    @TableField("expire_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireAt;

    @Schema(description = "各服务商差异化字段,JSON存储")
    @TableField("extra")
    private String extra;

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
