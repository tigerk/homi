package com.homi.model.contract.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/29
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "租客合同 VO")
@Builder
public class LeaseContractVO implements Serializable {
    @Schema(description = "租客合同ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "租约ID")
    @TableField("lease_id")
    private Long leaseId;

    @Schema(description = "合同编号")
    private String contractCode;

    @Schema(description = "合同模板ID")
    @TableField("contract_template_id")
    private Long contractTemplateId;
    private String contractTemplateName;

    @Schema(description = "合同内容")
    @TableField("contract_content")
    private String contractContent;

    @Schema(description = "签约状态：0=待签字、1=已签字")
    @TableField("sign_status")
    private Integer signStatus;

    @Schema(description = "合同签约备注")
    @TableField("remark")
    private String remark;
}
