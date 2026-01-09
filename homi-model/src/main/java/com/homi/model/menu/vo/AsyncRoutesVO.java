package com.homi.model.menu.vo;

import lombok.Data;

import java.util.List;


@Data
public class AsyncRoutesVO {

    private String path;

    private String name;

    private String redirect;

    private String component;

    private Integer type;

    private AsyncRoutesMetaVO meta;

    private List<AsyncRoutesVO> children;
}
