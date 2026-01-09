package com.homi.model.role.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author tigerk
 * @version 1.0
 * @date 2024-07-01 22:54
 * @description: 角色返回对象
 */
@Data
public class RoleVO {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色状态（0正常 1停用）
     */
    private Integer status;


    /**
     * 创建者
     */
    private Long createBy;
    private String createByName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 角色描述
     */
    private String remark;

    /**
     * 绑定用户数
     */
    private Long userCount;

}
