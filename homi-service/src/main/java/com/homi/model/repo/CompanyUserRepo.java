package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.CompanyUser;
import com.homi.model.mapper.CompanyUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * 获取用户的公司列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 23:48
     *
     * @param userId 参数说明
     * @return java.util.List<com.homi.model.entity.CompanyUser>
     */
    public List<CompanyUser> getCompanyListByUserId(Long userId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getUserId, userId);

        return list(queryWrapper);
    }

    public CompanyUser getCompanyUser(Long companyId, Long userId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getUserId, userId);
        queryWrapper.eq(CompanyUser::getCompanyId, companyId);

        return getOne(queryWrapper);
    }
}
