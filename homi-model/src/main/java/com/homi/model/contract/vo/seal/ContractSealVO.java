package com.homi.model.contract.vo.seal;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "合同电子印章VO")
public class ContractSealVO {
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "印章类型:1=企业,2=个人")
    private Integer sealType;

    @Schema(description = "来源:1=自有图片,2=法大大,3=E签宝,4=其他第三方")
    private Integer source;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "公司社会统一信用代码")
    private String companyUscc;

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

    @Schema(description = "状态:0=待审核,1=正常,2=已禁用,3=审核失败")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "电子印章图片URL列表")
    private List<String> sealUrls;

    @Schema(description = "服务商平台的账号/企业ID")
    private String providerAccountId;

    @Schema(description = "服务商平台的印章ID")
    private String providerSealId;

    @Schema(description = "认证状态:0=未认证,1=认证中,2=已认证,3=失败")
    private Integer authStatus;

    @Schema(description = "认证完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date authAt;

    @Schema(description = "授权到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireAt;

    @Schema(description = "各服务商差异化字段,JSON存储")
    private String providerExtra;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
