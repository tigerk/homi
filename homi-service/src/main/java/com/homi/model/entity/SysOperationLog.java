package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 操作日志记录表
 * </p>
 *
 * @author tk
 * @since 2025-07-30
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("sys_operation_log")
@Schema(name = "SysOperationLog", description = "操作日志记录表")
public class SysOperationLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "日志主键")
    @TableId("id")
    private Long id;

    @Schema(description = "模块标题")
    @TableField("title")
    private String title;

    @Schema(description = "业务类型（0其它 1新增 2修改 3删除）")
    @TableField("operation_type")
    private Integer operationType;

    @Schema(description = "操作人类别（0其它 1后台用户 2前台用户）")
    @TableField("operator_type")
    private Integer operatorType;

    @TableField("company_id")
    private Long companyId;

    @Schema(description = "操作用户名")
    @TableField("username")
    private String username;

    @Schema(description = "操作系统")
    @TableField("os")
    private String os;

    @Schema(description = "浏览器类型")
    @TableField("browser")
    private String browser;

    @Schema(description = "方法名称")
    @TableField("method")
    private String method;

    @Schema(description = "请求方式")
    @TableField("request_method")
    private String requestMethod;

    @Schema(description = "请求URL")
    @TableField("request_url")
    private String requestUrl;

    @Schema(description = "主机地址")
    @TableField("ip_address")
    private String ipAddress;

    @Schema(description = "操作地点")
    @TableField("location")
    private String location;

    @Schema(description = "请求参数")
    @TableField("param")
    private String param;

    @Schema(description = "返回参数")
    @TableField("json_result")
    private String jsonResult;

    @Schema(description = "操作状态（0：正常；-1：异常）")
    @TableField("status")
    private Integer status;

    @Schema(description = "错误消息")
    @TableField("error_msg")
    private String errorMsg;

    @Schema(description = "操作时间")
    @TableField("request_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date requestTime;

    @Schema(description = "消耗时间")
    @TableField("cost_time")
    private Long costTime;
}
