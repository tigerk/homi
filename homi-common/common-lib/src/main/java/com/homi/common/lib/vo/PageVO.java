package com.homi.common.lib.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageVO<T> {

    private Long currentPage;

    private Long pageSize;

    private Long total;

    private Long pages;

    private List<T> list;
}
