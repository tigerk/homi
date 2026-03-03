package com.homi.model.dict.template.vo;

import lombok.Data;

@Data
public class DictTemplateSyncVO {
    private Integer toVer;
    private Integer companyCount;
    private Integer successCount;
    private Integer failCount;
}

