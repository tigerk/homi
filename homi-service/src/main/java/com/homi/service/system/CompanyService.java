package com.homi.service.system;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.company.CompanyCreateDTO;
import com.homi.domain.dto.company.CompanyQueryDTO;
import com.homi.domain.dto.user.UserCreateDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.exception.BizException;
import com.homi.model.entity.Company;
import com.homi.model.entity.User;
import com.homi.model.repo.CompanyRepo;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class CompanyService {
    private final CompanyRepo companyRepo;
    private final UserService userService;

    public PageVO<Company> getCompanyList(CompanyQueryDTO query) {
        Page<Company> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(query.getName())) {
            queryWrapper.like(Company::getName, query.getName());
        }

        if (CharSequenceUtil.isNotBlank(query.getContactName())) {
            queryWrapper.like(Company::getContactName, query.getContactName());
        }

        if (CharSequenceUtil.isNotBlank(query.getContactPhone())) {
            queryWrapper.like(Company::getContactPhone, query.getContactPhone());
        }

        if (query.getStatus() != null) {
            queryWrapper.eq(Company::getStatus, query.getStatus());
        }

        IPage<Company> companyPage = companyRepo.page(page, queryWrapper);

        PageVO<Company> pageVO = new PageVO<>();
        pageVO.setTotal(companyPage.getTotal());
        pageVO.setList(companyPage.getRecords());
        pageVO.setCurrentPage(companyPage.getCurrent());
        pageVO.setPageSize(companyPage.getSize());
        pageVO.setPages(companyPage.getPages());

        return pageVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean createCompany(CompanyCreateDTO createDTO) {
        // 创建公司
        Company company = new Company();
        BeanUtil.copyProperties(createDTO, company);

        // 公司名称 & uscc 不能重复
        validateCompanyUniqueness(company);

        companyRepo.save(company);

        // 创建管理员账号，并绑定公司
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setUsername(createDTO.getAccount());
        userCreateDTO.setPhone(createDTO.getAccount());
        userCreateDTO.setPassword(createDTO.getPassword());
        userCreateDTO.setCompanyId(company.getId());
        userCreateDTO.setNickname(createDTO.getContactName());

        User user = BeanCopyUtils.copyBean(userCreateDTO, User.class);
        user.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));

        userService.createUser(user);

        return true;
    }

    /**
     * 检查公司是否唯一
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/16 13:44
     *
     * @param company 参数说明
     */
    private void validateCompanyUniqueness(Company company) {
        Company selectedOne = companyRepo.getBaseMapper().selectOne(new LambdaQueryWrapper<Company>().eq(Company::getName, company.getName()));
        if (Objects.nonNull(selectedOne)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该公司已存在");
        }
        if (CharSequenceUtil.isNotBlank(company.getUscc())) {
            Company uscc = companyRepo.getBaseMapper().selectOne(new LambdaQueryWrapper<Company>().eq(Company::getUscc, company.getUscc()));
            if (Objects.nonNull(uscc)) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该社会统一信用代码已被用户：" + uscc.getName() + "绑定");
            }
        }

    }
}
