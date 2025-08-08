package com.homi.domain.dto.menu;

import lombok.Data;


@Data
public class SimpleMenuVO {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 菜单名称
     */
    private String title;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 显示顺序
     */
    private Integer sortOrder;


    /**
     * 菜单类型（0菜单 1iframe 2外链 3按钮）
     */
    private Integer menuType;


}
