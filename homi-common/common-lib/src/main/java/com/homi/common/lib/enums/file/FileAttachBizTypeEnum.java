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
     * 租户证件正面
     */
    TENANT_ID_CARD_FRONT("tenant_id_card_front"),
    /**
     * 租户证件反面
     */
    TENANT_ID_CARD_BACK("tenant_id_card_back"),
    /**
     * 租户证件手持
     */
    TENANT_ID_CARD_IN_HAND("tenant_id_card_in_hand"),
    CONTRACT_FILE("contract_file"),
    /**
     * 租户图片
     */
    TENANT_IMAGE("tenant_image"),
    ;

    private final String bizType;
}
