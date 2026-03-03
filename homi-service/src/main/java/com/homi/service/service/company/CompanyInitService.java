package com.homi.service.service.company;

import com.homi.common.lib.enums.StatusEnum;
import com.homi.model.company.dto.init.InitDictDataDTO;
import com.homi.model.dao.entity.Dict;
import com.homi.model.dao.entity.DictData;
import com.homi.model.dao.repo.DictDataRepo;
import com.homi.model.dao.repo.DictRepo;
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
    private final DictRepo dictRepo;
    private final DictDataRepo dictDataRepo;

    public void initCompany(Long companyId) {
    }

    private DictData initDictData(Long companyId, InitDictDataDTO dictDataDTO, Dict childDict) {
        DictData dictData = new DictData();
        dictData.setCompanyId(companyId);
        dictData.setDictId(childDict.getId());
        dictData.setName(dictDataDTO.getName());
        dictData.setValue(dictDataDTO.getValue());
        dictData.setSortOrder(dictDataDTO.getSortOrder());
        dictData.setColor(dictDataDTO.getColor());
        dictData.setStatus(StatusEnum.ACTIVE.getValue());
        return dictData;
    }
}

