package com.homi.model.dto.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 同住人数据传输对象
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/02
 */
@Data
public class TenantMateDTO {
    @Schema(description = "同住人ID", example = "1")
    private Integer id;

    @Schema(description = "租客ID", example = "1")
    private Integer tenantId;

    @Schema(description = "姓名",  example = "张三")
    private String name;

    @Schema(description = "性别：0=男，1=女", example = "0")
    private Integer gender;

    @Schema(description = "证件类型：1=身份证，2=护照",  example = "1")
    private Integer idType;

    @Schema(description = "证件号码",  example = "123456789012345678")
    private String idNo;

    @Schema(description = "联系电话",  example = "13800138000")
    private String phone;

    @Schema(description = "标签列表", example = "[\"学生\", \"无业\"]")
    private List<String> tags;

    @Schema(description = "备注", example = "这是一个测试备注")
    private String remark;

    @Schema(description = "状态：0=停用，1=启用", example = "1")
    private Integer status;

    @Schema(description = "身份证正面照片列表",  example = "[\"https://example.com/idCardFront1.jpg\", \"https://example.com/idCardFront2.jpg\"]")
    private List<String> idCardFrontList;

    @Schema(description = "身份证背面照片列表",  example = "[\"https://example.com/idCardBack1.jpg\", \"https://example.com/idCardBack2.jpg\"]")
    private List<String> idCardBackList;

    @Schema(description = "手持身份证照片列表",  example = "[\"https://example.com/idCardInHand1.jpg\", \"https://example.com/idCardInHand2.jpg\"]")
    private List<String> idCardInHandList;

    @Schema(description = "其他照片列表",  example = "[\"https://example.com/otherImage1.jpg\", \"https://example.com/otherImage2.jpg\"]")
    private List<String> otherImageList;
}
