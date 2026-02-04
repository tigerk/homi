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
 * 通知公告表
 * </p>
 *
 * @author tk
 * @since 2026-02-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("notice")
@Schema(name = "Notice", description = "通知公告表")
public class Notice implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "公告ID")
    @TableId("id")
    private Long id;

    @Schema(description = "公告标题")
    @TableField("title")
    private String title;

    @Schema(description = "公告内容")
    @TableField("content")
    private String content;

    @Schema(description = "公司 ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "公告类型（1=通知，2=消息）")
    @TableField("type")
    private Integer type;

    @Schema(description = "公告状态（1正常 0关闭）")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建者")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新者")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
