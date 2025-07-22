package com.homi.model.entity;

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
 * 操作日志记录表
 * </p>
 *
 * @author tk
 * @since 2025-07-22
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
    @TableField("business_type")
    private Integer businessType;

    @Schema(description = "方法名称")
    @TableField("method")
    private String method;

    @Schema(description = "请求方式")
    @TableField("request_method")
    private String requestMethod;

    @Schema(description = "操作类别（0其它 1后台用户 2前台用户）")
    @TableField("operator_type")
    private Integer operatorType;

    @Schema(description = "操作用户名")
    @TableField("username")
    private String username;

    @Schema(description = "请求URL")
    @TableField("request_url")
    private String requestUrl;

    @Schema(description = "主机地址")
    @TableField("ip")
    private String ip;

    @Schema(description = "操作地点")
    @TableField("location")
    private String location;

    @Schema(description = "请求参数")
    @TableField("param")
    private String param;

    @Schema(description = "返回参数")
    @TableField("json_result")
    private String jsonResult;

    @Schema(description = "操作状态（0正常 1异常）")
    @TableField("status")
    private Integer status;

    @Schema(description = "错误消息")
    @TableField("error_msg")
    private String errorMsg;

    @Schema(description = "操作时间")
    @TableField("oper_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date operTime;

    @Schema(description = "消耗时间")
    @TableField("cost_time")
    private Long costTime;
}
