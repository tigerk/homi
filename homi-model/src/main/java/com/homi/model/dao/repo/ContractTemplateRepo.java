package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.model.dao.entity.ContractTemplate;
import com.homi.model.dao.mapper.ContractTemplateMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 合同模板表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-12
 */
@Service
public class ContractTemplateRepo extends ServiceImpl<ContractTemplateMapper, ContractTemplate> {

    public List<ContractTemplate> getContractTemplateList(Long deptId, Integer contractType) {
        LambdaQueryWrapper<ContractTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractTemplate::getContractType, contractType);
        wrapper.apply("JSON_CONTAINS(dept_ids, JSON_ARRAY({0}))", deptId);  // 使用自定义 SQL 查询 JSON 数据
        wrapper.eq(ContractTemplate::getStatus, StatusEnum.ACTIVE.getValue());

        wrapper.select(ContractTemplate::getId,
            ContractTemplate::getCompanyId,
            ContractTemplate::getContractType,
            ContractTemplate::getTemplateName,
            ContractTemplate::getRemark
        );

        return list(wrapper);
    }

    public List<ContractTemplate> getContractTemplateListByCompanyIdAndType(Long curCompanyId, Integer contractType) {
        LambdaQueryWrapper<ContractTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractTemplate::getCompanyId, curCompanyId);
        wrapper.eq(ContractTemplate::getContractType, contractType);
        wrapper.eq(ContractTemplate::getStatus, StatusEnum.ACTIVE.getValue());

        wrapper.select(ContractTemplate::getId,
            ContractTemplate::getCompanyId,
            ContractTemplate::getContractType,
            ContractTemplate::getTemplateName,
            ContractTemplate::getRemark
        );

        return list(wrapper);
    }
}
