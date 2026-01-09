package com.homi.model.dict.data.vo;

import lombok.Data;

/**
 * @author tigerk
 * @version 1.0
 * @date 2024-09-03 10:36
 * @description: 字典数据
 */
@Data
public class DictDataVO {
    private String id;

    private String name;

    private Object value;

    private String color;
}
