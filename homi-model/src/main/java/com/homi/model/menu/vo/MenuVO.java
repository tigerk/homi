package com.homi.model.menu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuVO {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 菜单名称
     */
    private String title;

    /**
     * 路由名称
     */
    private String name;

    /**
     * 菜单类型（0目录 1菜单 2iframe 3外链 4按钮）
     */
    private Integer menuType;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 路由参数
     */
    private String query;

    /**
     * 显示顺序
     */
    private Integer sortOrder;

    /**
     * 路由重定向
     */
    private String redirect;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 右侧菜单图标
     */
    private String extraIcon;

    /**
     * 菜单状态（0显示 1隐藏）
     */
    private Boolean visible;

    /**
     * 权限标识
     */
    private String auths;

    /**
     * 进场动画
     */
    private String enterTransition;

    /**
     * 离场动画
     */
    private String leaveTransition;

    /**
     * 菜单所属平台（0后台 1前台）
     */
    private Integer platformType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 将某个菜单激活
     */
    private String activePath;

    /**
     * iframe页面地址
     */
    private String frameSrc;

    /**
     * 内嵌的iframe页面是否开启首次加载动画（0否 1是）
     */
    private Boolean frameLoading;

    /**
     * 路由组件缓存（开启 `true`、关闭 `false`）`可选
     */
    private Boolean keepAlive;

    /**
     * 当前菜单名称或自定义信息禁止添加到标签页（默认`false`）
     */
    private Boolean hiddenTag;

    /**
     * 当前菜单名称是否固定显示在标签页且不可关闭（默认`false`）
     */
    private Boolean fixedTag;

    /**
     * 是否在菜单中显示（默认`true`）`可选
     */
    private Boolean showLink;

    /**
     * 是否显示父级菜单 `可选`
     */
    private Boolean showParent;

    /**
     * 是否为平台菜单（0否 1是）
     */
    private Boolean isPlatform;

    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;


}
