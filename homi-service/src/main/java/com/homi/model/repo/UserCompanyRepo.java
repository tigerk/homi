package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.company.CompanyUserListDTO;
import com.homi.model.entity.Company;
import com.homi.model.entity.UserCompany;
import com.homi.model.mapper.UserCompanyMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 公司用户表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-09-10
 */
@Service
public class UserCompanyRepo extends ServiceImpl<UserCompanyMapper, UserCompany> {
    @Resource
    private CompanyRepo companyRepo;

    /**
     * 获取用户的公司列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 23:48
     *
     * @param userId 参数说明
     * @return java.util.List<com.homi.model.entity.UserCompany>
     */
    public List<CompanyUserListDTO> getCompanyListByUserId(Long userId) {
        LambdaQueryWrapper<UserCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCompany::getUserId, userId);

        List<UserCompany> list = list(queryWrapper);
        if (list.isEmpty()) {
            return List.of();
        }

        // 获取公司名称
        List<Long> companyIdList = list.stream().map(UserCompany::getCompanyId).toList();
        List<Company> companies = companyRepo.listByIds(companyIdList);
        Map<Long, Company> companyMap = companies.stream().collect(Collectors.toMap(Company::getId, company -> company));

        return list.stream().map(item -> {
            CompanyUserListDTO companyUserListDTO = new CompanyUserListDTO();
            companyUserListDTO.setCompanyId(item.getCompanyId());
            companyUserListDTO.setUserId(userId);
            companyUserListDTO.setCompanyUserType(item.getCompanyUserType());
            Company company = companyMap.get(item.getCompanyId());
            companyUserListDTO.setCompanyName(company.getName());
            return companyUserListDTO;
        }).toList();
    }

    public UserCompany getCompanyUser(Long companyId, Long userId) {
        LambdaQueryWrapper<UserCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCompany::getUserId, userId);
        queryWrapper.eq(UserCompany::getCompanyId, companyId);

        return getOne(queryWrapper);
    }

    public Boolean updateCompanyUserDeptId(Long curCompanyId, Long userId, Long deptId) {
        LambdaQueryWrapper<UserCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCompany::getCompanyId, curCompanyId)
            .eq(UserCompany::getUserId, userId);

        UserCompany userCompany = new UserCompany();
        userCompany.setDeptId(deptId);

        return update(userCompany, queryWrapper);
    }
}
