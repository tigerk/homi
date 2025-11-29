package com.homi.service.company;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.vo.IdNameVO;
import com.homi.domain.vo.company.CompanyListVO;
import com.homi.model.entity.Company;
import com.homi.model.entity.UserCompany;
import com.homi.model.entity.User;
import com.homi.model.repo.CompanyRepo;
import com.homi.model.repo.UserCompanyRepo;
import com.homi.service.system.UserService;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
public class CompanyService {
    private final CompanyRepo companyRepo;
    private final UserService userService;

    private final UserCompanyRepo userCompanyRepo;

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
        LambdaQueryWrapper<UserCompany> queryWrapper = new LambdaQueryWrapper<UserCompany>()
            .eq(UserCompany::getCompanyId, curCompanyId);

        List<UserCompany> userCompanies = userCompanyRepo.list(queryWrapper);

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
}
