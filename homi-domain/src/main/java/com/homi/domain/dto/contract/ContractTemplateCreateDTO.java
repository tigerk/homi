package com.homi.domain.dto.contract;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/12
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractTemplateCreateDTO {
    @Schema(description = "合同模板ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "合同模板类型：1=租客、2=业主、3=预定")
    @NotNull(message = "合同模板类型不能为空")
    private Integer contractType;

    @Schema(description = "合同模板名称")
    @NotBlank(message = "合同模板名称不能为空")
    private String templateName;

    @Schema(description = "合同模板内容，包含模板变量占位符")
    @NotBlank(message = "合同模板内容不能为空")
    private String templateContent;

    @Schema(description = "生效部门json，格式：[1,2,3]")
    @NotNull(message = "生效部门不能为空")
    private List<String> deptIds;

    @Schema(description = "合同状态：0=未生效，1=生效中")
    private Integer status;

    @Schema(description = "合同模板备注")
    private String remark;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人ID")
    private Long updateBy;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
}
