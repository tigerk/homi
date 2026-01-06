package com.homi.model.vo.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author tigerk
 * @version 1.0
 * @date 2024-08-01 21:57
 * @description: 角色简单对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleSimpleVO  implements Serializable {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;
}
