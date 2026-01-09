package com.homi.model.user;

import com.homi.common.lib.enums.GenderEnum;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.validator.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户创建对象")
public class UserCreateDTO {
    @Schema(description = "公司用户ID")
    private Long companyUserId;

    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名（登录名）
     */
    @Schema(description = "用户名，在注册时自动填充为手机号")
    private String username;

    /**
     * 密码
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String password;

    /**
     * 邮箱号
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 简介
     */
    @Size(max = 300, message = "简介长度不能超过300个字符")
    private String intro;

    /**
     * 性别（0未知，1男，2女）
     */
    @NotNull(message = "性别不能为空")
    @EnumValue(enumClass = GenderEnum.class, message = "性别只能为0（未知），1（男），2（女）")
    private Integer gender;

    /**
     * 出生日期
     */
    @Past(message = "出生日期必须是过去的时间")
    private LocalDateTime birthday;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "证件类型")
    private Integer idType;

    @Schema(description = "证件号码")
    private String idNo;

    /**
     * 状态（0正常，1禁用）
     */
    @NotNull(message = "状态不能为空")
    @EnumValue(enumClass = StatusEnum.class, message = "状态只能为0（正常）或1（冻结）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "更新人")
    private Long updateBy;
}
