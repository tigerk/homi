package com.homi.common.lib.enums;

import lombok.Getter;

/**
 * 业务操作类型
 *
 * @author ruoyi
 */
@Getter
public enum OperationTypeEnum {
    /**
     * 其它
     */
    OTHER(0, "其它"),

    /**
     * 新增
     */
    INSERT(1, "新增"),

    /**
     * 修改
     */
    UPDATE(2, "修改"),

    /**
     * 删除
     */
    DELETE(3, "删除"),

    /**
     * 授权
     */
    GRANT(4, "授权"),

    /**
     * 导出
     */
    EXPORT(5, "导出"),

    /**
     * 导入
     */
    IMPORT(6, "导入"),

    /**
     * 强退
     */
    FORCE(7, "强退"),

    /**
     * 清空数据
     */
    CLEAR(8, "清空数据");

    private final int code;

    private final String name;

    OperationTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

}
