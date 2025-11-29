package com.homi.model.entity;

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
 * 用户表
 * </p>
 *
 * @author tk
 * @since 2025-11-28
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("user")
@Schema(name = "User", description = "用户表")
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键（用户id）")
    @TableId("id")
    private Long id;

    @Schema(description = "用户名（登录名）")
    @TableField("username")
    private String username;

    @Schema(description = "密码")
    @TableField("password")
    private String password;

    @Schema(description = "真实姓名")
    @TableField("real_name")
    private String realName;

    @Schema(description = "证件类型")
    @TableField("id_type")
    private Integer idType;

    @Schema(description = "证件号")
    @TableField("id_no")
    private String idNo;

    @Schema(description = "用户类型，参考UserTypeEnum")
    @TableField("user_type")
    private Integer userType;

    @Schema(description = "邮箱号")
    @TableField("email")
    private String email;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "头像")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "简介")
    @TableField("remark")
    private String remark;

    @Schema(description = "性别（0未知，1男，2女）")
    @TableField("gender")
    private Integer gender;

    @Schema(description = "出生日期")
    @TableField("birthday")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date birthday;

    @Schema(description = "注册来源")
    @TableField("register_source")
    private String registerSource;

    @Schema(description = "状态（0正常，-1禁用）")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
