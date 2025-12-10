package com.homi.common.lib.dto;

import lombok.Data;

@Data
public class PageDTO {

    private Long currentPage = 1L;

    private Long pageSize = 10L;
}
