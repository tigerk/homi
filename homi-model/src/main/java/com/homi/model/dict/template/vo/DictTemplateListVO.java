package com.homi.model.dict.template.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DictTemplateListVO {
    private Long id;
    private String dictCode;
    private String dictName;
    private String parentCode;
    private Integer sortOrder;
    private Integer status;
    private Boolean hidden;
    private Boolean enabled;
    private Integer ver;
    private String remark;
    private List<DictTemplateListVO> children = new ArrayList<>();
}

