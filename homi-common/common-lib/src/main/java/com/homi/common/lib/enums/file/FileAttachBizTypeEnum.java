package com.homi.common.lib.enums.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件业务类型：user_avatar, house_image, contract_file
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/11/5 10:50
 */

@Getter
@AllArgsConstructor
public enum FileAttachBizTypeEnum {
    USER_AVATAR("user_avatar"),
    HOUSE_IMAGE("house_image"),
    ROOM_IMAGE("room_image"),

    /**
     * 租客证件正面
     */
    TENANT_ID_CARD_FRONT("tenant_id_card_front"),
    /**
     * 租客证件反面
     */
    TENANT_ID_CARD_BACK("tenant_id_card_back"),
    /**
     * 租客证件手持
     */
    TENANT_ID_CARD_IN_HAND("tenant_id_card_in_hand"),
    /**
     * 租客其他照片
     */
    TENANT_OTHER_IMAGE("tenant_other_image"),
    /**
     * 合同文件
     */
    CONTRACT_FILE("contract_file"),
    /**
     * 租客图片
     */
    TENANT_IMAGE("tenant_image"),
    /**
     * 营业执照
     */
    BUSINESS_LICENSE("business_license"),

    /**
     * 租客同住人证件正面
     */
    TENANT_MATE_ID_CARD_FRONT("tenant_mate_id_card_front"),
    /**
     * 租客同住人证件反面
     */
    TENANT_MATE_ID_CARD_BACK("tenant_mate_id_card_back"),
    /**
     * 租客同住人证件手持
     */
    TENANT_MATE_ID_CARD_IN_HAND("tenant_mate_id_card_in_hand"),
    /**
     * 租客同住人其他照片
     */
    TENANT_MATE_OTHER_IMAGE("tenant_mate_other_image"),
    ;

    private final String bizType;
}
