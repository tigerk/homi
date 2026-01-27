package com.homi.common.lib.enums.approval;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批实例状态枚举
 */
@Getter
@AllArgsConstructor
public enum ApproverTypeEnum {
    /*
     * 指定用户: 1,
     * 指定角色: 2,
     * 部门主管: 3,
     * 发起人自选: 4
     */
    SPECIFIC_USER(1, "指定用户"),
    SPECIFIC_ROLE(2, "指定角色"),
    DEPARTMENT_SUPERVISOR(3, "部门主管"),
    SELF_OPTION(4, "发起人自选");

    private final Integer code;
    private final String name;

    public static ApproverTypeEnum fromCode(Integer code) {
        for (ApproverTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
