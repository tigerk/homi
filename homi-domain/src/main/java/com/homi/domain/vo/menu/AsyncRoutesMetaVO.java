package com.homi.domain.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
public class AsyncRoutesMetaVO {

    private String title;

    private String icon;

    private Boolean showLink;

    @Schema(description = "排序")
    private Integer rank;

    private List<String> roles;

    private List<String> auths;

    private Boolean keepAlive;

    private String frameSrc;

    private Boolean frameLoading;


}
