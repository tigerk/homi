package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.SaasUserTypeEnum;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.model.dao.entity.Company;
import com.homi.model.dao.entity.CompanyUser;
import com.homi.model.dao.mapper.CompanyUserMapper;
import com.homi.model.company.dto.UserCompanyListDTO;
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
public class CompanyUserRepo extends ServiceImpl<CompanyUserMapper, CompanyUser> {
    @Resource
    private CompanyRepo companyRepo;

    /**
     * 设置公司管理员
     *
     * @param companyId 公司ID
     * @param userId    用户ID
     * @return com.nest.model.entity.CompanyUser
     */
    public CompanyUser createUserCompanyAdmin(Long companyId, Long userId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getCompanyId, companyId);
        queryWrapper.eq(CompanyUser::getUserId, userId);
        CompanyUser companyUser = getOne(queryWrapper);
        if (companyUser != null) {
            companyUser.setUserType(SaasUserTypeEnum.COMPANY_ADMIN.getType());
            updateById(companyUser);
        } else {
            companyUser = new CompanyUser();
            companyUser.setCompanyId(companyId);
            companyUser.setUserId(userId);
            companyUser.setUserType(SaasUserTypeEnum.COMPANY_ADMIN.getType());
            companyUser.setStatus(StatusEnum.ACTIVE.getValue());

            getBaseMapper().insert(companyUser);
        }

        return companyUser;
    }

    /**
     * 更新管理员
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/28 17:52
     *
     * @param companyId 参数说明
     * @param userId    参数说明
     * @return com.nest.model.entity.CompanyUser
     */
    public CompanyUser updateUserCompanyAdmin(Long companyId, Long userId) {
        CompanyUser companyUserAdmin = getUserCompanyAdmin(companyId);
        // 判断是否是同一个用户，如果是则直接返回
        if (companyUserAdmin.getUserId().equals(userId)) {
            return companyUserAdmin;
        }

        deleteUserCompanyAdmin(companyId);

        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getCompanyId, companyId);
        queryWrapper.eq(CompanyUser::getUserId, userId);
        CompanyUser companyUser = getOne(queryWrapper);
        if (companyUser != null) {
            companyUser.setUserType(SaasUserTypeEnum.COMPANY_ADMIN.getType());
            updateById(companyUser);
            return companyUser;
        } else {
            return createUserCompanyAdmin(companyId, userId);
        }
    }

    public void deleteUserCompanyAdmin(Long companyId) {
        CompanyUser companyUserAdmin = getUserCompanyAdmin(companyId);
        if (companyUserAdmin != null) {
            getBaseMapper().deleteById(companyUserAdmin.getId());
        }
    }

    public CompanyUser getUserCompanyAdmin(Long companyId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getCompanyId, companyId);
        queryWrapper.eq(CompanyUser::getUserType, SaasUserTypeEnum.COMPANY_ADMIN.getType());
        return getOne(queryWrapper);
    }

    /**
     * 获取用户的公司列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 23:48
     *
     * @param userId 参数说明
     * @return java.util.List<com.homi.model.entity.CompanyUser>
     */
    public List<UserCompanyListDTO> getCompanyListByUserId(Long userId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getUserId, userId);

        List<CompanyUser> list = list(queryWrapper);
        if (list.isEmpty()) {
            return List.of();
        }

        // 获取公司名称
        List<Long> companyIdList = list.stream().map(CompanyUser::getCompanyId).toList();
        List<Company> companies = companyRepo.getValidCompanyList(companyIdList);
        Map<Long, Company> companyMap = companies.stream().collect(Collectors.toMap(Company::getId, company -> company));

        return list.stream().filter(item -> companyMap.containsKey(item.getCompanyId()))
            .map(item -> {
                UserCompanyListDTO userCompanyListDTO = new UserCompanyListDTO();
                userCompanyListDTO.setCompanyId(item.getCompanyId());
                userCompanyListDTO.setUserId(userId);
                userCompanyListDTO.setUserType(item.getUserType());
                Company company = companyMap.get(item.getCompanyId());
                userCompanyListDTO.setCompanyName(company.getName());
                return userCompanyListDTO;
            }).toList();
    }

    public CompanyUser getCompanyUser(Long companyId, Long userId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getUserId, userId);
        queryWrapper.eq(CompanyUser::getCompanyId, companyId);

        return getOne(queryWrapper);
    }

    public Boolean updateCompanyUserDeptId(Long curCompanyId, Long userId, Long deptId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getCompanyId, curCompanyId)
            .eq(CompanyUser::getUserId, userId);

        CompanyUser companyUser = new CompanyUser();
        companyUser.setDeptId(deptId);

        return update(companyUser, queryWrapper);
    }

    public void updateAllUserStatusByCompanyId(Long companyId, int value) {
        LambdaUpdateWrapper<CompanyUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(CompanyUser::getStatus, value);
        updateWrapper.eq(CompanyUser::getCompanyId, companyId);
        update(updateWrapper);
    }

    public Long getUserCountByRoleId(Long id) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.apply("JSON_CONTAINS(roles, JSON_ARRAY({0}))", id);  // 使用自定义 SQL 查询 JSON 数据
        queryWrapper.eq(CompanyUser::getStatus, StatusEnum.ACTIVE.getValue());

        return count(queryWrapper);
    }
}
