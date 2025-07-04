package com.homi.service.company;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.company.CompanyCreateDTO;
import com.homi.domain.dto.company.CompanyQueryDTO;
import com.homi.domain.dto.user.UserCreateDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.enums.common.UserTypeEnum;
import com.homi.domain.enums.company.CompanyNatureEnum;
import com.homi.domain.vo.company.CompanyListVO;
import com.homi.domain.vo.company.IdNameVO;
import com.homi.exception.BizException;
import com.homi.model.entity.Company;
import com.homi.model.entity.SysRole;
import com.homi.model.entity.User;
import com.homi.model.repo.CompanyRepo;
import com.homi.service.system.SysRoleService;
import com.homi.service.system.SysUserRoleService;
import com.homi.service.system.UserService;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/28
 */

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepo companyRepo;
    private final UserService userService;

    /**
     * 获取公司信息
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/25 13:49
     *
     * @param companyId 参数说明
     * @return com.homi.domain.vo.company.CompanyListVO
     */
    public CompanyListVO getCompanyById(Long companyId) {
        Company company = companyRepo.getBaseMapper().selectById(companyId);
        return BeanCopyUtils.copyBean(company, CompanyListVO.class);
    }

    public void changeStatus(CompanyCreateDTO createDTO) {
        Company company = companyRepo.getBaseMapper().selectById(createDTO.getId());
        company.setStatus(createDTO.getStatus());

        if (createDTO.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            userService.updateUserStatusByCompanyId(company.getId(), StatusEnum.DISABLED.getValue());
        } else {
            List<User> companyUserByType = userService.getCompanyUserByType(company.getId(), UserTypeEnum.COMPANY_ADMIN.getType());
            if (companyUserByType.isEmpty()) {
                throw new BizException("该公司下没有管理员，请先创建管理员");
            }
            User user = companyUserByType.getFirst();
            user.setStatus(StatusEnum.ACTIVE.getValue());
            userService.updateUser(user);
        }

        companyRepo.updateById(company);
    }
}
