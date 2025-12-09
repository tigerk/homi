package com.homi.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.dao.entity.Company;
import com.homi.dao.mapper.CompanyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 公司表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class CompanyRepo extends ServiceImpl<CompanyMapper, Company> {

    public List<Company> getValidCompanyList(List<Long> companyIds) {
        return getBaseMapper().selectList(new LambdaQueryWrapper<Company>()
            .eq(Company::getStatus, StatusEnum.ACTIVE.getValue())
            .in(Company::getId, companyIds));
    }
}
