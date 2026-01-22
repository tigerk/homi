package com.homi.model.dao.entity;

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
 * 通用物业交割主表
 * </p>
 *
 * @author tk
 * @since 2026-01-22
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("delivery")
@Schema(name = "Delivery", description = "通用物业交割主表")
public class Delivery implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @Schema(description = "主体类型：TENANT-租客, OWNER-业主")
    @TableField("subject_type")
    private String subjectType;

    @Schema(description = "对应主体ID (租客ID或业主ID)")
    @TableField("subject_type_id")
    private Long subjectTypeId;

    @Schema(description = "房间ID")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "交割方向：CHECK_IN-迁入/接收, CHECK_OUT-迁出/交付")
    @TableField("handover_type")
    private String handoverType;

    @Schema(description = "交割单状态: 0-草稿, 1-已签署/生效, -1-作废")
    @TableField("status")
    private Integer status;

    @Schema(description = "交割日期")
    @TableField("handover_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date handoverDate;

    @Schema(description = "操作员/管家ID")
    @TableField("inspector_id")
    private Long inspectorId;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人ID")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人ID")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "修改时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
