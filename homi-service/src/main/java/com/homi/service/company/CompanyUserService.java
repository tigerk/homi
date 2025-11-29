package com.homi.service.company;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.user.UserCreateDTO;
import com.homi.domain.dto.user.UserQueryDTO;
import com.homi.domain.dto.user.UserUpdateStatusDTO;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.vo.dept.DeptSimpleVO;
import com.homi.domain.vo.company.user.UserCreateVO;
import com.homi.domain.vo.company.user.UserVO;
import com.homi.exception.BizException;
import com.homi.model.entity.UserCompany;
import com.homi.model.entity.Dept;
import com.homi.model.entity.User;
import com.homi.model.repo.UserCompanyRepo;
import com.homi.model.repo.UserRepo;
import com.homi.service.system.DeptService;
import com.homi.service.system.UserService;
import com.homi.utils.BeanCopyUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final UserCompanyRepo userCompanyRepo;

    private final UserRepo userRepo;

    private final DeptService deptService;

    private final UserService userService;

    /**
     * 获取公司的人员
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/26 09:31
     *
     * @param companyId 参数说明
     * @return java.util.List<com.homi.model.entity.User>
     */
    public List<UserCompany> getCompanyUserByCompanyId(Long companyId) {
        LambdaQueryWrapper<UserCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCompany::getCompanyId, companyId);

        return userCompanyRepo.list(queryWrapper);
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
        LambdaQueryWrapper<UserCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCompany::getDeptId, deptId);

        List<UserCompany> userCompanies = userCompanyRepo.list(queryWrapper);

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
        LambdaQueryWrapper<UserCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCompany::getUserId, userId);
        queryWrapper.eq(UserCompany::getCompanyId, companyId);

        return userCompanyRepo.exists(queryWrapper);
    }

    public PageVO<UserVO> pageUserList(UserQueryDTO query) {
        Page<UserVO> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        IPage<UserVO> userVOPage = userCompanyRepo.getBaseMapper().selectUserList(page, query);

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

        UserCompany userCompany = new UserCompany();
        userCompany.setUserId(userId);
        userCompany.setCompanyId(createDTO.getCompanyId());
        userCompany.setDeptId(createDTO.getDeptId());
        userCompany.setStatus(StatusEnum.ACTIVE.getValue());
        userCompany.setCreateBy(createDTO.getUpdateBy());

        userCompanyRepo.save(userCompany);

        return UserCreateVO.builder()
            .companyUserId(userCompany.getId())
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

        UserCompany userCompany = new UserCompany();
        userCompany.setId(createDTO.getCompanyUserId());
        userCompany.setUserId(userId);
        userCompany.setCompanyId(createDTO.getCompanyId());
        userCompany.setDeptId(createDTO.getDeptId());
        userCompany.setStatus(StatusEnum.ACTIVE.getValue());
        userCompany.setUpdateBy(createDTO.getUpdateBy());

        userCompanyRepo.updateById(userCompany);

        return UserCreateVO.builder()
            .companyUserId(userCompany.getId())
            .userId(userId)
            .phone(createDTO.getPhone())
            .existed(true)
            .build();
    }

    public Long updateUserUserStatus(@Valid UserUpdateStatusDTO updateDTO) {
        UserCompany userCompany = userCompanyRepo.getById(updateDTO.getCompanyUserId());
        if (Objects.isNull(userCompany) || !userCompany.getCompanyId().equals(updateDTO.getCompanyId())) {
            throw new BizException("用户不再该公司任职");
        }

        userCompany.setStatus(updateDTO.getStatus());
        userCompanyRepo.updateById(userCompany);

        return userCompany.getId();
    }

    public Integer deleteByIds(List<Long> idList) {
        Long loginId = Long.valueOf(StpUtil.getLoginId().toString());
        for (Long id : idList) {
            if (id.equals(loginId)) {
                throw new BizException("无法删除自身");
            }
        }

        userCompanyRepo.getBaseMapper().deleteBatchIds(idList);

        return idList.size();
    }

    public UserCompany getCompanyUserById(@NotNull(message = "ID不能为空") Long companyUserId) {
        return userCompanyRepo.getById(companyUserId);
    }
}
