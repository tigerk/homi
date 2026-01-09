package com.homi.service.service.company;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.Company;
import com.homi.model.dao.entity.CompanyUser;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.repo.CompanyRepo;
import com.homi.model.dao.repo.CompanyUserRepo;
import com.homi.model.dao.repo.RegionRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.model.company.dto.CompanyCreateDTO;
import com.homi.model.company.dto.CompanyDeleteDTO;
import com.homi.model.company.dto.CompanyQueryDTO;
import com.homi.model.common.vo.IdNameVO;
import com.homi.model.company.vo.CompanyCreateVO;
import com.homi.model.company.vo.CompanyListVO;
import com.homi.service.service.sys.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepo companyRepo;
    private final UserRepo userRepo;
    private final CompanyUserRepo companyUserRepo;
    private final RegionRepo regionRepo;

    private final UserService userService;
    private final CompanyPackageService packageService;
    private final CompanyCodeService codeService;
    private final CompanyInitService companyInitService;

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

    public List<IdNameVO> getUserOptions(Long curCompanyId) {
        LambdaQueryWrapper<CompanyUser> queryWrapper = new LambdaQueryWrapper<CompanyUser>()
            .eq(CompanyUser::getCompanyId, curCompanyId);

        List<CompanyUser> userCompanies = companyUserRepo.list(queryWrapper);

        return userCompanies.stream().map(companyUser -> {
                IdNameVO build = IdNameVO.builder()
                    .id(companyUser.getUserId())
                    .build();

                User userById = userService.getUserById(companyUser.getUserId());
                build.setName(userById.getNickname());

                return build;
            }
        ).toList();
    }

    /**
     * 获取公司列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/25 13:49
     *
     * @param query 查询实体
     * @return 所有数据
     */
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
            assert vo != null;
            vo.setPackageName(packageMap.get(company.getPackageId()));

            userRepo.getOptById(company.getAdminUserId()).ifPresent(account -> vo.setAdminPhone(account.getUsername()));

            List<Long> parentIdsById = regionRepo.findParentIdsById(company.getRegionId());
            parentIdsById.add(company.getRegionId());
            vo.setRegionIds(parentIdsById);

            return vo;
        }).collect(Collectors.toList()));
        pageVO.setCurrentPage(companyPage.getCurrent());
        pageVO.setPageSize(companyPage.getSize());
        pageVO.setPages(companyPage.getPages());

        return pageVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public CompanyCreateVO updateCompany(CompanyCreateDTO createDTO) {
        Company company = companyRepo.getBaseMapper().selectById(createDTO.getId());

        validateCompanyUniqueness(company);

        BeanUtil.copyProperties(createDTO, company);

        if (CollUtil.isNotEmpty(createDTO.getRegionIds())) {
            company.setRegionId(CollUtil.getLast(createDTO.getRegionIds()));
        }

        Pair<Long, ResponseCodeEnum> longResponseCodeEnumPair = updateUser4Company(createDTO);
        company.setAdminUserId(longResponseCodeEnumPair.getKey());

        companyRepo.updateById(company);

        return CompanyCreateVO.builder()
            .id(company.getId())
            .name(company.getName())
            .message(longResponseCodeEnumPair.getValue().getMsg())
            .build();
    }

    public Pair<Long, ResponseCodeEnum> updateUser4Company(CompanyCreateDTO createDTO) {
        // 创建统一账号
        Pair<Long, ResponseCodeEnum> user = userRepo.createCompanyAdmin(createDTO);

        // 创建用户与company的关联表
        CompanyUser companyUserAdmin = companyUserRepo.updateUserCompanyAdmin(createDTO.getId(), user.getKey());
        log.info("更新了用户与company的关联表, userCompany={}", companyUserAdmin);

        return user;
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

    @Transactional(rollbackFor = Exception.class)
    public CompanyCreateVO createCompany(CompanyCreateDTO createDTO) {
        if (Objects.isNull(createDTO.getLegalPerson())) {
            createDTO.setLegalPerson(StringUtils.EMPTY);
        }

        // 创建公司
        Company company = new Company();
        BeanUtil.copyProperties(createDTO, company);

        // 公司名称 & uscc 不能重复
        validateCompanyUniqueness(company);

        if (CollUtil.isNotEmpty(createDTO.getRegionIds())) {
            company.setRegionId(CollUtil.getLast(createDTO.getRegionIds()));
        }

        // 生成公司代码
        String generateCompanyCode = codeService.generateCompanyCode(company.getName());
        company.setCode(generateCompanyCode);

        long companyId = IdWorker.getId(company);
        // 创建账号
        createDTO.setId(companyId);

        Pair<Long, ResponseCodeEnum> user = createUser4Company(createDTO);
        company.setAdminUserId(user.getKey());

        company.setId(companyId);
        companyRepo.save(company);

        // 初始化公司默认值
        companyInitService.initCompany(companyId);

        CompanyCreateVO companyCreateVO = new CompanyCreateVO();
        companyCreateVO.setId(company.getId());
        companyCreateVO.setName(company.getName());
        companyCreateVO.setMessage(user.getValue().getMsg());

        log.info("创建了新公司, company={}；创建了公司的管理员账号, user={}", company, user);

        return companyCreateVO;
    }

    /**
     * 创建公司管理员账号
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/25 13:49
     *
     * @param createDTO 参数说明
     * @return 创建结果
     */
    public Pair<Long, ResponseCodeEnum> createUser4Company(CompanyCreateDTO createDTO) {
        // 创建统一账号
        Pair<Long, ResponseCodeEnum> user = userRepo.createCompanyAdmin(createDTO);

        // 创建用户与company的关联表
        CompanyUser companyUserAdmin = companyUserRepo.createUserCompanyAdmin(createDTO.getId(), user.getKey());
        log.info("创建了用户与company的关联表, userCompany={}", companyUserAdmin);

        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(CompanyCreateDTO createDTO) {
        Company company = companyRepo.getBaseMapper().selectById(createDTO.getId());
        company.setStatus(createDTO.getStatus());

        companyUserRepo.updateAllUserStatusByCompanyId(company.getId(), createDTO.getStatus());

        companyRepo.updateById(company);
    }

    public void deleteCompany(CompanyDeleteDTO deleteDTO) {
        companyRepo.removeById(deleteDTO.getId());
    }
}
