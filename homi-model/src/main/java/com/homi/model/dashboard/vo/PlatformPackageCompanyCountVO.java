package com.homi.model.dashboard.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class PlatformPackageCompanyCountVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packageId;

    private String packageName;

    private Integer companyCount;
}
