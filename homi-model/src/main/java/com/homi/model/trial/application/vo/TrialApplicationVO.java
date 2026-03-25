package com.homi.model.trial.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class TrialApplicationVO {
    private Long id;
    private String phone;
    private Long regionId;
    private String cityName;
    private String usageRemark;
    private Integer status;
    private String handleRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
