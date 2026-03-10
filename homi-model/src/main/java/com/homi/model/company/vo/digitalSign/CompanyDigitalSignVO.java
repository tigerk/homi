package com.homi.model.company.vo.digitalSign;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "企业电子签章VO")
public class CompanyDigitalSignVO {
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "签章类型：1=企业，2=个人")
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

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作人联系电话")
    private String operatorPhone;

    @Schema(description = "操作人证件类型")
    private Integer operatorIdType;

    @Schema(description = "操作人证件号")
    private String operatorIdNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "电子印章图片URL列表")
    private List<String> sealUrls;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
