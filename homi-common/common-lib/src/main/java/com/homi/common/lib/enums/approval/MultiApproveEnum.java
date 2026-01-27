package com.homi.common.lib.enums.approval;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MultiApproveEnum {
    OR_SIGN(1, "或签（一人通过即可）"),
    AND_SIGN(2, "会签（所有人通过）");

    private final int code;
    private final String name;
}
