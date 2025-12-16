package com.homi.model.vo.contract;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "合同模板列表VO")
public class ContractTemplateListVO {
    @Schema(description = "合同模板ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "合同模板类型：1=租客、2=业主、3=预定")
    private Integer contractType;

    @Schema(description = "合同模板名称")
    private String templateName;

    @Schema(description = "合同模板内容，包含模板变量占位符")
    private String templateContent;

    @Schema(description = "生效部门json，格式：[1,2,3]")
    private List<String> deptIds;

    @Schema(description = "合同状态：0=未生效，1=生效中，-1=已作废")
    private Integer status;

    @Schema(description = "合同模板备注")
    private String remark;

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
