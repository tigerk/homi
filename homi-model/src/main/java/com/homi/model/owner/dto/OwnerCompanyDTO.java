package com.homi.model.owner.dto;

import com.homi.common.lib.enums.IdTypeEnum;
import com.homi.common.lib.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "业主企业信息DTO")
public class OwnerCompanyDTO {
    @Schema(description = "业主企业ID")
    private Long id;

    @Schema(description = "SaaS企业ID")
    private Long companyId;

    @Schema(description = "企业名称")
    private String name;

    @Schema(description = "统一社会信用代码")
    private String uscc;

    @Schema(description = "法人姓名")
    private String legalPerson;

    @Schema(description = "法人证件类型")
    private IdTypeEnum legalPersonIdType;

    @Schema(description = "法人证件号码")
    private String legalPersonIdNo;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "注册地址")
    private String registeredAddress;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态")
    private StatusEnum status;

    @Schema(description = "创建人")
    private Long createBy;
}
