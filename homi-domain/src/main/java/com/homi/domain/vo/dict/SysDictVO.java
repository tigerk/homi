package com.homi.domain.vo.dict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 字典表
 * </p>
 *
 * @author tk
 * @since 2025-07-30
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Schema(description = "字典表，包含子项列表")
public class SysDictVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "子项列表")
    private List<SysDictVO> children;
}
