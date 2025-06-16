package com.homi.domain.base;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {

    private Long currentPage;

    private Long pageSize;

    private Long total;

    private Long pages;

    private List<T> list;
}
