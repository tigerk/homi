package com.homi.common.lib.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 菜单类型
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 01:26
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {
    /**
     * 菜单
     */
    MENU(0, "菜单"),

    /**
     * Iframe
     */
    IFRAME(1, "Iframe"),

    /**
     * 外链
     */
    EXTERNAL_LINK(2, "外链"),

    /**
     * 按钮
     */
    BUTTON(3, "按钮");

    private final int type;
    private final String typeStr;

    public static List<Integer> getMenuList() {
        return List.of(MENU.getType(), IFRAME.getType(), EXTERNAL_LINK.getType());
    }

    public static List<Integer> getButtonList() {
        return List.of(BUTTON.getType());
    }
}
