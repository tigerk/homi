package com.homi.common.lib.enums.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TenantParamsEnum {
    CONTRACT_CODE("${租客合同编号}", "${租客合同编号}"),
    SIGNED_HOUSE_LIST("${签约房源}", "${签约房源}"),
    TOTAL_AREA("${房屋总面积}", "${房屋总面积}"),

    // 房东姓名
    OWNER_NAME("${房东姓名}", "${房东姓名}"),
    // 房东手机号码
    OWNER_PHONE("${房东手机号}", "${房东手机号}"),
    // 房东证件号码
    OWNER_ID_CARD("${房东身份证号}", "${房东身份证号}"),

    TENANT_NAME("${租客姓名}", "${租客姓名}"),
    TENANT_PHONE("${租客手机号}", "${租客手机号}"),
    // 租客身份证号
    TENANT_ID_CARD("${租客身份证号}", "${租客身份证号}"),
    LEASE_START("${合同开始日期}", "${合同开始日期}"),
    LEASE_END("${合同结束日期}", "${合同结束日期}"),
    // 租赁天数
    LEASE_DAYS("${租赁天数}", "${租赁天数}"),
    RENT_PRICE("${月租金}", "${月租金}"),
    // 支付周期
    PAYMENT_MONTHS("${支付周期}（月）", "${支付周期（月）}"),
    // 押金月数
    DEPOSIT_MONTHS("${押金月数}", "${押金月数}"),
    // 租客备注信息
    TENANT_REMARK("${租客备注}", "${租客备注}"),

    // 房东签字位
    OWNER_SIGNATURE("${房东签字}", "${房东签字}"),

    // 租客签字位
    TENANT_SIGNATURE("${租客签字}", "${租客签字}"),

    // 公司盖章位置
    COMPANY_SEAL("${公司盖章}", "${公司盖章}"),

    // 合同时间
    CONTRACT_DATE("${合同时间}", "${合同时间}"),
    ;

    private final String key;
    private final String value;

}
