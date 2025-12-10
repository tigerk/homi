package com.homi.domain.saas.facade.impl;

import com.homi.domain.saas.facade.CompanyInitFacade;
import com.homi.domain.saas.service.company.CompanyInitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/10
 */

@Service
@RequiredArgsConstructor
public class CompanyInitFacadeImpl implements CompanyInitFacade {
    private final CompanyInitService companyInitService;


}
