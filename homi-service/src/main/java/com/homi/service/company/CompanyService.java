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
import com.homi.domain.enums.company.CompanyNatureEnum;
import com.homi.domain.vo.company.CompanyListVO;
import com.homi.domain.vo.company.IdNameVO;
import com.homi.exception.BizException;
import com.homi.model.entity.Company;
import com.homi.model.entity.User;
import com.homi.model.repo.CompanyRepo;
import com.homi.service.system.UserService;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final CompanyPackageService packageService;

    public PageVO<CompanyListVO> getCompanyList(CompanyQueryDTO query) {
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

        Map<Long, String> packageMap = packageService.listSimple().stream()
                .collect(Collectors.toMap(IdNameVO::getId, IdNameVO::getName));


        IPage<Company> companyPage = companyRepo.page(page, queryWrapper);

        PageVO<CompanyListVO> pageVO = new PageVO<>();
        pageVO.setTotal(companyPage.getTotal());
        pageVO.setList(companyPage.getRecords().stream().map(company -> {
            CompanyListVO vo = BeanCopyUtils.copyBean(company, CompanyListVO.class);
            vo.setPackageName(packageMap.get(company.getPackageId()));
            return vo;
        }).collect(Collectors.toList()));
        pageVO.setCurrentPage(companyPage.getCurrent());
        pageVO.setPageSize(companyPage.getSize());
        pageVO.setPages(companyPage.getPages());

        return pageVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean createCompany(CompanyCreateDTO createDTO) {
        if (Objects.isNull(createDTO.getLegalPerson())) {
            createDTO.setLegalPerson(StringUtils.EMPTY);
        }

        createDTO.setNature(CompanyNatureEnum.ENTERPRISE.getCode());

        // 创建公司
        Company company = new Company();
        BeanUtil.copyProperties(createDTO, company);

        // 公司名称 & uscc 不能重复
        validateCompanyUniqueness(company);

        companyRepo.save(company);

        // 创建管理员账号，并绑定公司
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setUsername(createDTO.getUsername());
        userCreateDTO.setPhone(createDTO.getContactPhone());
        userCreateDTO.setPassword(createDTO.getPassword());
        userCreateDTO.setCompanyId(company.getId());
        userCreateDTO.setNickname(createDTO.getContactName());

        User user = BeanCopyUtils.copyBean(userCreateDTO, User.class);
        user.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));

        userService.createUser(user);

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCompany(CompanyCreateDTO createDTO) {
        Company company = companyRepo.getBaseMapper().selectById(createDTO.getId());

        validateCompanyUniqueness(company);

        BeanUtil.copyProperties(createDTO, company);
        companyRepo.updateById(company);

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

        Company selectedOne = companyRepo.getBaseMapper().selectOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getName, company.getName())
                .ne(Company::getId, ObjectUtil.defaultIfNull(company.getId(), 0L)));
        if (Objects.nonNull(selectedOne)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该公司已存在");
        }
        if (CharSequenceUtil.isNotBlank(company.getUscc())) {
            Company uscc = companyRepo.getBaseMapper().selectOne(new LambdaQueryWrapper<Company>()
                    .eq(Company::getUscc, company.getUscc())
                    .ne(Company::getId, ObjectUtil.defaultIfNull(company.getId(), 0L)));
            if (Objects.nonNull(uscc)) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "该社会统一信用代码已被用户：" + uscc.getName() + "绑定");
            }
        }

    }
}
