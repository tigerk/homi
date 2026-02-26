package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 企业租客数据传输对象
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/2
 */
@Data
public class TenantCompanyDTO {

    @Schema(description = "企业租客ID", example = "1")
    private Long id;

    @Schema(description = "企业名称", example = "光辉未来科技有限公司")
    private String companyName;

    @Schema(description = "统一社会信用代码", example = "911101011234567890")
    private String uscc;

    @Schema(description = "法定代表人", example = "张三")
    private String legalPerson;

    @Schema(description = "法人证件类型", example = "1")
    private Integer legalPersonIdType;

    @Schema(description = "法人证件号码", example = "123456789012345678")
    private String legalPersonIdNo;

    @Schema(description = "联系人姓名", example = "李四")
    private String contactName;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "注册地址", example = "北京市朝阳区某街道")
    private String registeredAddress;

    @Schema(description = "营业执照附件", example = "https://example.com/businessLicense.jpg")
    private String businessLicenseUrl;

    @Schema(description = "租客标签 (JSON 格式)", example = "{\"key\":\"value\"}")
    private List<String> tags;

    @Schema(description = "租客备注", example = "这是一个测试备注")
    private String remark;

    @Schema(description = "租客状态：0=停用，1=启用", example = "1")
    private Integer status;

    @Schema(description = "创建人ID", hidden = true)
    private Long createBy;
}
