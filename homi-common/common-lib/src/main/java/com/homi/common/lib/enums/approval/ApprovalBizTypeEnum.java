package com.homi.common.lib.enums.approval;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批业务类型枚举
 */
@Getter
@AllArgsConstructor
public enum ApprovalBizTypeEnum {

    TENANT_CHECKIN("TENANT_CHECKIN", "租客入住", "tenant", "id"),
    TENANT_CHECKOUT("TENANT_CHECKOUT", "租客退租", "tenant_checkout", "id"),
    HOUSE_CREATE("HOUSE_CREATE", "房源录入", "house", "id"),
//    ROOM_CREATE("ROOM_CREATE", "房间录入", "room", "id"),
//    CONTRACT_SIGN("CONTRACT_SIGN", "合同签署", "tenant_contract", "id"),
//    BOOKING_CONVERT("BOOKING_CONVERT", "预定转签约", "booking", "id"),
    ;

    /**
     * 业务类型编码
     */
    private final String code;

    /**
     * 业务类型名称
     */
    private final String name;

    /**
     * 关联的业务表名
     */
    private final String tableName;

    /**
     * 业务表主键字段
     */
    private final String pkField;

    public static ApprovalBizTypeEnum getByCode(String code) {
        for (ApprovalBizTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
