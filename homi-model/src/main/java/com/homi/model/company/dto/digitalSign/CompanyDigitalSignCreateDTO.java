package com.homi.model.company.dto.digitalSign;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "企业电子签章创建/更新DTO")
public class CompanyDigitalSignCreateDTO {
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "签章类型：1=企业，2=个人")
    @NotNull(message = "签章类型不能为空")
    private Integer signType;

    @Schema(description = "公司名称")
    private String name;

    @Schema(description = "公司社会统一信用代码")
    private String uscc;

    @Schema(description = "法人姓名")
    private String legalPerson;

    @Schema(description = "法人证件类型")
    private String legalPersonIdType;

    @Schema(description = "法人证件号")
    private String legalPersonIdNo;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "电子印章图片URL列表")
    private List<String> sealUrls;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人ID")
    private Long updateBy;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
