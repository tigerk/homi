package com.homi.domain.base;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {

    private Long currentPage = 1L;

    private Long pageSize = 10L;

    private Long total = 0L;

    private Long pages = 0L;

    private List<T> list;
}
