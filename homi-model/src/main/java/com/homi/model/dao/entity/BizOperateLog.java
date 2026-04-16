package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("biz_operate_log")
@Schema(name = "BizOperateLog", description = "通用业务操作日志表")
public class BizOperateLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "SaaS企业ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "业务类型")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "业务主键ID")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "操作类型")
    @TableField("operate_type")
    private String operateType;

    @Schema(description = "操作描述")
    @TableField("operate_desc")
    private String operateDesc;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "操作前快照")
    @TableField("before_snapshot")
    private String beforeSnapshot;

    @Schema(description = "操作后快照")
    @TableField("after_snapshot")
    private String afterSnapshot;

    @Schema(description = "扩展数据")
    @TableField("extra_data")
    private String extraData;

    @Schema(description = "来源类型")
    @TableField("source_type")
    private String sourceType;

    @Schema(description = "来源ID")
    @TableField("source_id")
    private Long sourceId;

    @Schema(description = "操作人ID")
    @TableField("operator_id")
    private Long operatorId;

    @Schema(description = "操作人名称")
    @TableField("operator_name")
    private String operatorName;

    @Schema(description = "是否删除：0=否，1=是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新时间")
    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
