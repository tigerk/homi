package com.homi.domain.saas.service.company;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.model.dao.entity.CompanyInit;
import com.homi.model.dao.entity.Dict;
import com.homi.model.dao.entity.DictData;
import com.homi.model.dao.repo.CompanyInitRepo;
import com.homi.model.dao.repo.DictDataRepo;
import com.homi.model.dao.repo.DictRepo;
import com.homi.model.dto.company.init.CompanyInitDTO;
import com.homi.model.dto.company.init.InitDictDTO;
import com.homi.model.dto.company.init.InitDictDataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
    private final CompanyInitRepo companyInitRepo;
    private final DictRepo dictRepo;
    private final DictDataRepo dictDataRepo;

    private static final Integer VER = 1;

    /**
     * 根据版本号获取公司默认值
     *
     * @return 公司默认值
     */
    public CompanyInitDTO getCompanyInit() {
        CompanyInit companyInit = companyInitRepo.getBaseMapper().selectOne(new LambdaQueryWrapper<CompanyInit>()
                .eq(CompanyInit::getVer, VER));

        CompanyInitDTO companyInitDTO = new CompanyInitDTO();
        companyInitDTO.setDicts(JSONUtil.toList(companyInit.getDicts(), InitDictDTO.class));
        return companyInitDTO;
    }

    public void initCompany(Long companyId) {
        CompanyInitDTO companyInitDTO = getCompanyInit();
        if (Objects.isNull(companyInitDTO)) {
            throw new BizException("公司默认值不存在，创建失败！");
        }

        for (InitDictDTO dictDTO : companyInitDTO.getDicts()) {
            Dict dict = new Dict();
            dict.setCompanyId(companyId);
            dict.setParentId(dictDTO.getParentId());
            dict.setDictCode(dictDTO.getDictCode());
            dict.setDictName(dictDTO.getDictName());
            dict.setSortOrder(dictDTO.getSortOrder());
            dict.setHidden(dictDTO.getHidden());
            dict.setStatus(StatusEnum.ACTIVE.getValue());
            dictRepo.save(dict);

            if (dictDTO.getChildren() != null && !dictDTO.getChildren().isEmpty()) {
                dictDTO.getChildren().forEach(child -> {
                    Dict childDict = new Dict();
                    childDict.setCompanyId(companyId);
                    childDict.setParentId(dict.getId());
                    childDict.setDictCode(child.getDictCode());
                    childDict.setDictName(child.getDictName());
                    childDict.setSortOrder(child.getSortOrder());
                    childDict.setHidden(child.getHidden());
                    childDict.setStatus(StatusEnum.ACTIVE.getValue());
                    dictRepo.save(childDict);

                    for (InitDictDataDTO dictDataDTO : child.getDictDataList()) {
                        DictData dictData = initDictData(companyId, dictDataDTO, childDict);
                        dictDataRepo.save(dictData);
                    }
                });
            }
        }
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

    public void saveCompanyDictInit(List<InitDictDTO> list) {
        CompanyInit companyInit = companyInitRepo.getBaseMapper().selectOne(new LambdaQueryWrapper<CompanyInit>()
                .eq(CompanyInit::getVer, VER));

        if (Objects.isNull(companyInit)) {
            companyInit = new CompanyInit();
            companyInit.setVer(VER);
            companyInit.setRemark("默认值");
        }

        companyInit.setDicts(JSONUtil.toJsonStr(list));
        companyInitRepo.saveOrUpdate(companyInit);
    }
}

