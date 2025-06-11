package com.homi.domain.base;

import lombok.Data;

@Data
public class BasePageDTO {

    private Long currentPage = 1L;

    private Long pageSize = 10L;
}
