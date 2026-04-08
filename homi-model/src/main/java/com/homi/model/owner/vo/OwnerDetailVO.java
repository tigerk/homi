package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.model.owner.dto.OwnerCompanyDTO;
import com.homi.model.owner.dto.OwnerContractDTO;
import com.homi.model.owner.dto.OwnerLeaseFreeRuleDTO;
import com.homi.model.owner.dto.OwnerLeaseRuleDTO;
import com.homi.model.owner.dto.OwnerPersonalDTO;
import com.homi.model.owner.dto.OwnerContractSubjectDTO;
import com.homi.common.lib.enums.owner.OwnerTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "业主详情VO")
public class OwnerDetailVO {
    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "业主类型")
    private OwnerTypeEnum ownerType;

    @Schema(description = "个人业主信息")
    private OwnerPersonalDTO ownerPersonal;

    @Schema(description = "企业业主信息")
    private OwnerCompanyDTO ownerCompany;

    @Schema(description = "业主合同")
    private OwnerContractDTO ownerContract;

    @Schema(description = "合同模板名称")
    private String contractTemplateName;

    @Schema(description = "合同房源列表")
    private List<OwnerContractSubjectDTO> contractSubjectList;

    @Schema(description = "合同房源数量")
    private Integer subjectCount;

    @Schema(description = "总面积")
    private BigDecimal totalArea;

    @Schema(description = "已配置房源数")
    private Integer configuredSubjectCount;

    @Schema(description = "包租规则")
    private OwnerLeaseRuleDTO ownerLeaseRule;

    @Schema(description = "包租免租规则列表")
    private List<OwnerLeaseFreeRuleDTO> ownerLeaseFreeRuleList;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "创建人姓名")
    private String createByName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "更新人姓名")
    private String updateByName;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
