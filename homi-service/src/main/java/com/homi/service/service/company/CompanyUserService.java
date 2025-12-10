package com.homi.service.service.company;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dto.user.UserCreateDTO;
import com.homi.model.dto.user.UserQueryDTO;
import com.homi.model.dto.user.UserUpdateStatusDTO;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.model.vo.company.user.UserCreateVO;
import com.homi.model.vo.company.user.UserVO;
import com.homi.model.vo.dept.DeptSimpleVO;
import com.homi.common.lib.exception.BizException;
import com.homi.model.dao.entity.Company;
import com.homi.model.dao.entity.CompanyUser;
import com.homi.model.dao.entity.Dept;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.repo.CompanyRepo;
import com.homi.model.dao.repo.CompanyUserRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.service.service.system.DeptService;
import com.homi.service.service.system.UserService;
import com.homi.common.lib.utils.BeanCopyUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    private final UserRepo userRepo;

    private final DeptService deptService;

    private final UserService userService;
    private final CompanyRepo companyRepo;

    /**
     * 获取公司的人员
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/26 09:31
     *
     * @param companyId 参数说明
     * @return java.util.List<com.homi.model.entity.User>
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
     * @return java.util.List<com.homi.domain.vo.user.UserVO>
     */
    public List<UserVO> getUserListByDeptId(Long deptId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getDeptId, deptId);

        List<CompanyUser> userCompanies = companyUserRepo.list(queryWrapper);

        List<UserVO> result = new ArrayList<>();
        userCompanies.forEach(companyUser -> {
            User user = userRepo.getById(companyUser.getUserId());
            UserVO userVO = BeanCopyUtils.copyBean(user, UserVO.class);
            result.add(userVO);
        });

        return result;
    }

    /**
     * 判断用户是否属于公司
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/12 10:52
     *
     * @param userId    参数说明
     * @param companyId 参数说明
     * @return boolean
     */
    public boolean userHasCompany(Long userId, Long companyId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyUser::getUserId, userId);
        queryWrapper.eq(CompanyUser::getCompanyId, companyId);

        return companyUserRepo.exists(queryWrapper);
    }

    public PageVO<UserVO> pageUserList(UserQueryDTO query) {
        Page<UserVO> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        IPage<UserVO> userVOPage = companyUserRepo.getBaseMapper().selectUserList(page, query);

        List<UserVO> records = userVOPage.getRecords();
        records.forEach(userVO -> {
            Dept deptById = deptService.getDeptById(userVO.getDeptId());
            if (Objects.nonNull(deptById)) {
                DeptSimpleVO deptSimpleVO = BeanCopyUtils.copyBean(deptById, DeptSimpleVO.class);
                userVO.setDept(deptSimpleVO);
            }
        });

        PageVO<UserVO> pageVO = new PageVO<>();
        pageVO.setTotal(userVOPage.getTotal());
        pageVO.setList(records);
        pageVO.setCurrentPage(userVOPage.getCurrent());
        pageVO.setPageSize(userVOPage.getSize());
        pageVO.setPages(userVOPage.getPages());

        return pageVO;
    }

    public UserCreateVO createUser(@Valid UserCreateDTO createDTO) {
        boolean existed = false;
        try {
            userService.validateUserUniqueness(null, createDTO.getUsername(), createDTO.getEmail(), createDTO.getPhone());
        } catch (BizException e) {
            existed = true;
        }

        Long userId;
        if (existed) {
            User userById = userService.getUserByPhone(createDTO.getPhone());
            userService.updateUser(createDTO);
            userId = userById.getId();
        } else {
            userId = userService.createUser(createDTO);
        }

        CompanyUser companyUser = new CompanyUser();
        companyUser.setUserId(userId);
        companyUser.setCompanyId(createDTO.getCompanyId());
        companyUser.setDeptId(createDTO.getDeptId());
        companyUser.setStatus(StatusEnum.ACTIVE.getValue());
        companyUser.setCreateBy(createDTO.getUpdateBy());

        companyUserRepo.save(companyUser);

        return UserCreateVO.builder()
            .companyUserId(companyUser.getId())
            .userId(userId)
            .phone(createDTO.getPhone())
            .existed(existed)
            .build();
    }

    public UserCreateVO updateUser(@Valid UserCreateDTO createDTO) {

        // 有冻结的行为
        if (createDTO.getStatus().equals(StatusEnum.DISABLED.getValue()) && Long.valueOf(StpUtil.getLoginId().toString()).equals(createDTO.getUserId())) {
            throw new BizException("无法冻结自身");
        }

        Long userId = userService.updateUser(createDTO);

        CompanyUser companyUser = new CompanyUser();
        companyUser.setId(createDTO.getCompanyUserId());
        companyUser.setUserId(userId);
        companyUser.setCompanyId(createDTO.getCompanyId());
        companyUser.setDeptId(createDTO.getDeptId());
        companyUser.setStatus(StatusEnum.ACTIVE.getValue());
        companyUser.setUpdateBy(createDTO.getUpdateBy());

        companyUserRepo.updateById(companyUser);

        return UserCreateVO.builder()
            .companyUserId(companyUser.getId())
            .userId(userId)
            .phone(createDTO.getPhone())
            .existed(true)
            .build();
    }

    public Long updateUserUserStatus(@Valid UserUpdateStatusDTO updateDTO) {
        CompanyUser companyUser = companyUserRepo.getById(updateDTO.getCompanyUserId());
        if (Objects.isNull(companyUser) || !companyUser.getCompanyId().equals(updateDTO.getCompanyId())) {
            throw new BizException("用户不再该公司任职");
        }

        companyUser.setStatus(updateDTO.getStatus());
        companyUserRepo.updateById(companyUser);

        return companyUser.getId();
    }

    public Integer deleteByIds(List<Long> idList) {
        Long loginId = Long.valueOf(StpUtil.getLoginId().toString());
        for (Long id : idList) {
            if (id.equals(loginId)) {
                throw new BizException("无法删除自身");
            }
        }

        companyUserRepo.getBaseMapper().deleteBatchIds(idList);

        return idList.size();
    }

    public CompanyUser getCompanyUserById(@NotNull(message = "ID不能为空") Long companyUserId) {
        return companyUserRepo.getById(companyUserId);
    }

    public Optional<User> getUsernameByCompanyUserId(Long companyUserId) {
        Optional<CompanyUser> optById = companyUserRepo.getOptById(companyUserId);

        return optById.map(companyUser -> userService.getUserById(companyUser.getUserId()));
    }

    public User deleteCompanyUser(Long companyUserId) {
        CompanyUser companyUser = companyUserRepo.getById(companyUserId);
        if (Objects.isNull(companyUser)) {
            throw new BizException("用户不再该公司任职");
        }

        Company company = companyRepo.getById(companyUser.getCompanyId());
        if (Objects.isNull(company)) {
            throw new BizException("公司不存在");
        }

        if (company.getAdminUserId().equals(companyUser.getUserId())) {
            throw new BizException("无法删除公司的超级管理员");
        }

        User user = userService.getUserById(companyUser.getUserId());

        companyUserRepo.getBaseMapper().deleteById(companyUserId);

        return user;
    }
}
