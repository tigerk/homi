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
 * 待办任务表
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("sys_todo")
@Schema(name = "SysTodo", description = "待办任务表")
public class SysTodo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "待办ID")
    @TableId("id")
    private Long id;

    @Schema(description = "公司/租户ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "待办负责人")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "待办标题（如：张三 3月房租待收）")
    @TableField("title")
    private String title;

    @Schema(description = "待办描述")
    @TableField("content")
    private String content;

    @Schema(description = "1=租约到期 2=账单催收 3=报修处理 4=合同续签 5=退房办理 6=其他")
    @TableField("todo_type")
    private Integer todoType;

    @Schema(description = "关联业务类型（contract/bill/repair 等）")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "关联业务ID，点击可跳转到对应详情页")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "优先级：1=高 2=中 3=低")
    @TableField("priority")
    private Integer priority;

    @Schema(description = "0=待处理 1=已处理 2=已忽略 3=已过期")
    @TableField("status")
    private Integer status;

    @Schema(description = "截止时间")
    @TableField("deadline")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date deadline;

    @Schema(description = "处理时间")
    @TableField("handle_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date handleTime;

    @Schema(description = "处理备注")
    @TableField("handle_remark")
    private String handleRemark;

    @Schema(description = "是否删除：0=否 1=是")
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

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
