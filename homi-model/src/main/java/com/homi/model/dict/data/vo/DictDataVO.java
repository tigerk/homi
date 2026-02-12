package com.homi.model.dict.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author tigerk
 * @version 1.0
 */
@Data
@Schema(description = "字典数据")
public class DictDataVO {
    private String id;

    private String name;

    private Object value;

    private String color;
}
