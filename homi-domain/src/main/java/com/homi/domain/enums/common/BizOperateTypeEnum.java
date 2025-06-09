package com.homi.domain.enums.common;

import lombok.Getter;

/**
 * 业务操作类型
 *
 * @author ruoyi
 */
@Getter
public enum BizOperateTypeEnum {
    /**
     * 其它
     */
    OTHER(0),

    /**
     * 新增
     */
    INSERT(1),

    /**
     * 修改
     */
    UPDATE(2),

    /**
     * 删除
     */
    DELETE(3),

    /**
     * 授权
     */
    GRANT(4),

    /**
     * 导出
     */
    EXPORT(5),

    /**
     * 导入
     */
    IMPORT(6),

    /**
     * 强退
     */
    FORCE(7),

    /**
     * 清空数据
     */
    CLEAR(8);

    private final int value;

    BizOperateTypeEnum(int value) {
        this.value = value;
    }

}
