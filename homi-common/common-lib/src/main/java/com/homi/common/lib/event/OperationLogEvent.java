package com.homi.common.lib.event;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/7/22
 */
@Data
public class OperationLogEvent {
    /**
     * 日志主键
     */
    @TableId
    private Long id;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 模块标题
     */
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    private Integer operationType;

    /**
     * 方法名称
     */
    private String method;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作类别（0其它 1后台用户 2前台用户）
     */
    private Integer operatorType;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 主机地址
     */
    private String ipAddress;

    /**
     * 操作地点
     */
    private String location;

    /**
     * 请求参数
     */
    private String param;

    /**
     * 返回参数
     */
    private String jsonResult;

    /**
     * 操作状态（0正常 1异常）
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private Date requestTime;

    /**
     * 消耗时间
     */
    private Long costTime;
}
