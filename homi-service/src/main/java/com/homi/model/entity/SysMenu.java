package com.homi.model.entity;

    import com.baomidou.mybatisplus.annotation.IdType;
    import com.baomidou.mybatisplus.annotation.TableId;
    import com.baomidou.mybatisplus.annotation.TableLogic;
    import com.baomidou.mybatisplus.annotation.TableName;
    import java.io.Serializable;
    import java.util.Date;
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
* @since 2025-06-22
*/
    @EqualsAndHashCode(callSuper = false)
    @Data
    @ToString(callSuper = true)
    @TableName("sys_menu")
    public class SysMenu implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

            /**
            * 菜单ID
            */
            @TableId(value = "id", type = IdType.AUTO)
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
            * 菜单类型（0代表菜单、1代表iframe、2代表外链、3代表按钮）
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
            * 菜单排序（平台规定只有home路由的rank才能为0，所以后端在返回rank的时候需要从非0开始 点击查看更多）

            */
    private Integer sort;

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
    private Integer visible;

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

    private String activePath;

            /**
            * iframe页面地址
            */
    private String frameSrc;

            /**
            * 内嵌的iframe页面是否开启首次加载动画（0否 1是）
            */
    private Integer frameLoading;

            /**
            * 路由组件缓存（开启 `true`、关闭 `false`）`可选
            */
    private Integer keepAlive;

            /**
            * 当前菜单名称或自定义信息禁止添加到标签页（默认`false`）
            */
    private Integer hiddenTag;

            /**
            * 当前菜单名称是否固定显示在标签页且不可关闭（默认`false`）
            */
    private Integer fixedTag;

            /**
            * 是否在菜单中显示（默认`true`）`可选
            */
    private Integer showLink;

            /**
            * 是否显示父级菜单 `可选`
            */
    private Integer showParent;

            /**
            * 0未删除；1：删除
            */
        @TableLogic
    private Integer deleted;

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
