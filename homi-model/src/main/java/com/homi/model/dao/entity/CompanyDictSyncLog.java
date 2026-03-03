package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 公司字典同步日志
 * </p>
 *
 * @author tk
 * @since 2026-03-03
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_dict_sync_log")
@Schema(name = "CompanyDictSyncLog", description = "公司字典同步日志")
public class CompanyDictSyncLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "起始版本")
    @TableField("from_ver")
    private Integer fromVer;

    @Schema(description = "目标版本")
    @TableField("to_ver")
    private Integer toVer;

    @Schema(description = "状态：0进行中 1成功 -1失败")
    @TableField("status")
    private Integer status;

    @Schema(description = "成功处理条数")
    @TableField("success_count")
    private Integer successCount;

    @Schema(description = "失败条数")
    @TableField("fail_count")
    private Integer failCount;

    @Schema(description = "错误信息")
    @TableField("error_msg")
    private String errorMsg;

    @Schema(description = "开始时间")
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @Schema(description = "结束时间")
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
}
