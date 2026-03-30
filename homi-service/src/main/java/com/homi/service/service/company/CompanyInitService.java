package com.homi.service.service.company;

import com.homi.service.service.sys.DictTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 应用于 domix-platform
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/4
 */

@Service
@RequiredArgsConstructor
public class CompanyInitService {
    private final DictTemplateService dictTemplateService;

    public void initCompany(Long companyId) {
        dictTemplateService.syncCompanyDictByLatestTemplate(companyId);
    }
}
