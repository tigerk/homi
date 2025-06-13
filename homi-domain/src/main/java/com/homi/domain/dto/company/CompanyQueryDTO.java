package com.homi.domain.dto.company;

import com.homi.domain.base.BasePageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "公司查询对象")
public class CompanyQueryDTO extends BasePageDTO {

    @Schema(description = "公司名称")
    private String name;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "状态")
    private Integer status;
}
