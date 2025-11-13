package com.homi.domain.enums.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LandlordParamsEnum {
    CONTRACT_NUMBER("${合同编号}", "${合同编号}"),
    HOUSE_ADDRESS("${房屋地址}", "${房屋地址}"),
    PROJECT_NAME("${小区/项目名称}", "${小区/项目名称}"),
    BUILDING_NUMBER("${楼栋号}", "${楼栋号}"),
    UNIT_NUMBER("${单元号}", "${单元号}"),
    HOUSE_NUMBER("${门牌号}", "${门牌号}"),
    SHARED_ROOM_NUMBER("${合租房间号}", "${合租房间号}"),
    SIGNED_HOUSE_LIST("${签约房源列表}", "${签约房源列表}"),
    HOUSE_PROPERTY_NUMBER("${房屋产权编号}", "${房屋产权编号}"),
    HOUSE_TYPE("${房屋类型}", "${房屋类型}"),
    PROPERTY_TYPE("${产权类型}", "${产权类型}"),
    TOTAL_AREA("${房屋总面积}", "${房屋总面积}"),
    SIGNED_AREA("${签约面积数}", "${签约面积数}"),
    TENANT_NAME("${租客姓名}", "${租客姓名}");

    private final String key;
    private final String value;

}
