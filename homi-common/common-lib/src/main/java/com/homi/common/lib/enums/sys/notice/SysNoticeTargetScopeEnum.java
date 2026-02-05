package com.homi.common.lib.enums.sys.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysNoticeTargetScopeEnum {
    /*
     * 发布范围：1=全员 2=房东 3=租客 4=指定角色
     */

    ALL(1, "全员"),
    LANDLORD(2, "房东"),
    TENANT(3, "租客"),
    SPECIFIED_ROLE(4, "指定角色");

    private final Integer code;
    private final String name;
}
