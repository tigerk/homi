package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * 菜单表
 * </p>
 *
 * @author tk
 * @since 2025-07-30
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("sys_menu")
@Schema(name = "SysMenu", description = "菜单表")
public class SysMenu implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    @TableId("id")
    private Long id;

    @Schema(description = "菜单名称")
    @TableField("title")
    private String title;

    @Schema(description = "路由名称")
    @TableField("name")
    private String name;

    @Schema(description = "菜单类型（0代表菜单、1代表iframe、2代表外链、3代表按钮）")
    @TableField("menu_type")
    private Integer menuType;

    @Schema(description = "父菜单ID")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "路由地址")
    @TableField("path")
    private String path;

    @Schema(description = "组件路径")
    @TableField("component")
    private String component;

    @Schema(description = "路由参数")
    @TableField("query")
    private String query;

    @Schema(description = "菜单排序（平台规定只有home路由的rank才能为0，所以后端在返回rank的时候需要从非0开始 点击查看更多）")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "路由重定向")
    @TableField("redirect")
    private String redirect;

    @Schema(description = "菜单图标")
    @TableField("icon")
    private String icon;

    @Schema(description = "右侧菜单图标")
    @TableField("extra_icon")
    private String extraIcon;

    @Schema(description = "菜单状态（1显示 0隐藏）")
    @TableField("visible")
    private Boolean visible;

    @Schema(description = "权限标识")
    @TableField("auths")
    private String auths;

    @Schema(description = "进场动画")
    @TableField("enter_transition")
    private String enterTransition;

    @Schema(description = "离场动画")
    @TableField("leave_transition")
    private String leaveTransition;

    @Schema(description = "菜单所属平台（0后台 1前台）")
    @TableField("platform_type")
    private Integer platformType;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @TableField("active_path")
    private String activePath;

    @Schema(description = "iframe页面地址")
    @TableField("frame_src")
    private String frameSrc;

    @Schema(description = "内嵌的iframe页面是否开启首次加载动画（0否 1是）")
    @TableField("frame_loading")
    private Boolean frameLoading;

    @Schema(description = "路由组件缓存（开启 `true`、关闭 `false`）`可选")
    @TableField("keep_alive")
    private Boolean keepAlive;

    @Schema(description = "当前菜单名称或自定义信息禁止添加到标签页（默认`false`）")
    @TableField("hidden_tag")
    private Boolean hiddenTag;

    @Schema(description = "当前菜单名称是否固定显示在标签页且不可关闭（默认`false`）")
    @TableField("fixed_tag")
    private Boolean fixedTag;

    @Schema(description = "是否在菜单中显示（默认`true`）`可选")
    @TableField("show_link")
    private Boolean showLink;

    @Schema(description = "是否显示父级菜单 `可选`")
    @TableField("show_parent")
    private Boolean showParent;

    @Schema(description = "是不是平台的（0非平台；1：平台菜单）")
    @TableField("is_platform")
    private Boolean isPlatform;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建者")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新者")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
}
