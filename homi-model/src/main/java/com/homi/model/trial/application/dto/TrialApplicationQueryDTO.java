package com.homi.model.trial.application.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "试用申请查询参数")
public class TrialApplicationQueryDTO extends PageDTO {

    private String phone;

    private String cityName;

    @Schema(description = "状态：0申请中 1已通过 2已拒绝")
    private Integer status;
}
