package com.homi.model.dao.entity;

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
 * 用户微信绑定表
 * </p>
 *
 * @author tk
 * @since 2026-02-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("user_wechat")
@Schema(name = "UserWechat", description = "用户微信绑定表")
public class UserWechat implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @TableId("user_id")
    private Long userId;

    @Schema(description = "微信 openid")
    @TableField("open_id")
    private String openId;

    @Schema(description = "微信 unionid")
    @TableField("union_id")
    private String unionId;

    @Schema(description = "小程序 appid")
    @TableField("app_id")
    private String appId;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
