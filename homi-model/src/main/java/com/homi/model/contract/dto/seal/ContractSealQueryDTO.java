package com.homi.model.contract.dto.seal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "合同电子印章查询DTO")
public class ContractSealQueryDTO {
    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "印章类型:1=企业,2=个人")
    private Integer sealType;

    @Schema(description = "来源:1=自有图片,2=法大大,3=E签宝,4=其他第三方")
    private Integer source;

    @Schema(description = "状态:0=待审核,1=正常,2=已禁用,3=审核失败")
    private Integer status;
}
