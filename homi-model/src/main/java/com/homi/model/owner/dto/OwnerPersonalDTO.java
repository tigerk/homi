package com.homi.model.owner.dto;

import com.homi.common.lib.enums.GenderEnum;
import com.homi.common.lib.enums.IdTypeEnum;
import com.homi.common.lib.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "业主个人信息DTO")
public class OwnerPersonalDTO {
    @Schema(description = "个人业主ID")
    private Long id;

    @Schema(description = "SaaS企业ID")
    private Long companyId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "性别")
    private GenderEnum gender;

    @Schema(description = "证件类型")
    private IdTypeEnum idType;

    @Schema(description = "证件号码")
    private String idNo;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "收款人姓名")
    private String payeeName;

    @Schema(description = "收款人电话")
    private String payeePhone;

    @Schema(description = "收款人证件类型")
    private IdTypeEnum payeeIdType;

    @Schema(description = "收款人证件号码")
    private String payeeIdNo;

    @Schema(description = "银行卡开户名")
    private String bankAccountName;

    @Schema(description = "银行卡号")
    private String bankAccountNo;

    @Schema(description = "开户行名称")
    private String bankName;

    @Schema(description = "身份证国徽面")
    private List<String> idCardFrontList;

    @Schema(description = "身份证人像面")
    private List<String> idCardBackList;

    @Schema(description = "手持身份证照片")
    private List<String> idCardInHandList;

    @Schema(description = "其他附件")
    private List<String> otherImageList;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态")
    private StatusEnum status;

    @Schema(description = "创建人")
    private Long createBy;
}
