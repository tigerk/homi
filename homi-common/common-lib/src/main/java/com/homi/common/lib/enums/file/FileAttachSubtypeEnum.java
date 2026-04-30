package com.homi.common.lib.enums.file;

import cn.hutool.core.util.EnumUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true, description = "文件附件业务子类型枚举")
public enum FileAttachSubtypeEnum {
    SIGNED_CONTRACT("SIGNED_CONTRACT", "线下签约合同"),
    SUPPLEMENT_AGREEMENT("SUPPLEMENT_AGREEMENT", "补充协议"),
    AUTHORIZATION("AUTHORIZATION", "授权委托书"),
    OWNER_MATERIAL("OWNER_MATERIAL", "业主资料"),
    HOUSE_MATERIAL("HOUSE_MATERIAL", "房源资料"),
    OTHER("OTHER", "其他资料");

    private final String code;
    private final String name;

    public static FileAttachSubtypeEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return EnumUtil.getBy(FileAttachSubtypeEnum::getCode, code);
    }

    public static String normalizeCode(String code) {
        FileAttachSubtypeEnum subtype = fromCode(code);
        return subtype == null ? OTHER.getCode() : subtype.getCode();
    }
}
