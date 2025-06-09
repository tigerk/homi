package com.homi.domain.vo.menu;

import lombok.Data;

import java.util.List;


@Data
public class AsyncRoutesMetaVO {

    private String title;

    private String icon;

    private Boolean showLink;

    private Integer sortOrder;

    private List<String> roles;

    private List<String> auths;

    private Boolean keepAlive;

    private String frameSrc;

    private Boolean frameLoading;


}
