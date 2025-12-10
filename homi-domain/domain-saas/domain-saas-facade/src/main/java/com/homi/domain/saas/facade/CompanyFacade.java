package com.homi.domain.saas.facade;

import cn.hutool.core.lang.Pair;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dto.company.CompanyCreateDTO;
import com.homi.model.dto.company.CompanyDeleteDTO;
import com.homi.model.dto.company.CompanyQueryDTO;
import com.homi.model.vo.IdNameVO;
import com.homi.model.vo.company.CompanyCreateVO;
import com.homi.model.vo.company.CompanyListVO;

import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/10
 */

public interface CompanyFacade {
    /**
     * 创建公司
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/10 17:46
     *
     * @param createDTO 参数说明
     * @return com.homi.model.vo.company.CompanyCreateVO
     */
    CompanyCreateVO createCompany(CompanyCreateDTO createDTO);

    /**
     * 创建公司的管理员账号
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/10 17:46
     *
     * @param createDTO 参数说明
     * @return cn.hutool.core.lang.Pair<java.lang.Long, com.homi.common.lib.response.ResponseCodeEnum>
     */
    Pair<Long, ResponseCodeEnum> createUser4Company(CompanyCreateDTO createDTO);

    /**
     * 获取公司列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/10 17:46
     *
     * @param query 参数说明
     * @return com.homi.common.lib.vo.PageVO<com.homi.model.vo.company.CompanyListVO>
     */
    PageVO<CompanyListVO> getCompanyList(CompanyQueryDTO query);

    /**
     * 更新公司
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/10 17:46
     *
     * @param createDTO 参数说明
     * @return com.homi.model.vo.company.CompanyCreateVO
     */
    CompanyCreateVO updateCompany(CompanyCreateDTO createDTO);

    /**
     * 更新公司的管理员账号
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/10 17:46
     *
     * @param createDTO 参数说明
     * @return cn.hutool.core.lang.Pair<java.lang.Long, com.homi.common.lib.response.ResponseCodeEnum>
     */
    Pair<Long, ResponseCodeEnum> updateUser4Company(CompanyCreateDTO createDTO);

    /**
     * 获取公司详情
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/10 17:46
     *
     * @param companyId 参数说明
     * @return com.homi.model.vo.company.CompanyListVO
     */
    CompanyListVO getCompanyById(Long companyId);

    /**
     * 删除公司
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/10 17:46
     *
     * @param deleteDTO 参数说明
     */
    void deleteCompany(CompanyDeleteDTO deleteDTO);

    /**
     * 获取公司的用户选项
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/10 17:46
     *
     * @param curCompanyId 参数说明
     * @return java.util.List<com.homi.model.vo.IdNameVO>
     */
    List<IdNameVO> getUserOptions(Long curCompanyId);
}
