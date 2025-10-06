package com.homi.service.company;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.dto.user.UserVO;
import com.homi.model.entity.CompanyUser;
import com.homi.model.entity.SysUser;
import com.homi.model.repo.CompanyUserRepo;
import com.homi.model.repo.SysUserRepo;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/28
 */

@Service
@RequiredArgsConstructor
public class CompanyUserService {
    private final CompanyUserRepo companyUserRepo;

    private final SysUserRepo sysUserRepo;

    /**
     * 获取公司的人员
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/26 09:31
     *
     * @param companyId 参数说明
     * @return java.util.List<com.homi.model.entity.SysUser>
     */
    public List<CompanyUser> getCompanyUserByCompanyId(Long companyId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getCompanyId, companyId);

        return companyUserRepo.list(queryWrapper);
    }

    /**
     * 获取部门下的人员
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/12 09:46
     *
     * @param deptId 参数说明
     * @return java.util.List<com.homi.domain.dto.user.UserVO>
     */
    public List<UserVO> getUserListByDeptId(Long deptId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getDeptId, deptId);

        List<CompanyUser> companyUsers = companyUserRepo.list(queryWrapper);

        List<UserVO> result = new ArrayList<>();
        companyUsers.forEach(companyUser -> {
            SysUser sysUser = sysUserRepo.getById(companyUser.getUserId());
            UserVO userVO = BeanCopyUtils.copyBean(sysUser, UserVO.class);
            result.add(userVO);
        });

        return result;
    }

    /**
     * 判断用户是否属于公司
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/12 10:52

     * @param userId 参数说明
     * @param companyId 参数说明
     * @return boolean
     */
    public boolean userHasCompany(Long userId, Long companyId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getUserId, userId);
        queryWrapper.eq(CompanyUser::getCompanyId, companyId);

        return companyUserRepo.exists(queryWrapper);
    }
}
