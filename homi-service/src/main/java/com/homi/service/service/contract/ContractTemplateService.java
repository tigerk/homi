package com.homi.service.service.contract;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.contract.ContractTemplateStatusEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.utils.ConvertHtml2PdfUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.CompanyUser;
import com.homi.model.dao.entity.ContractTemplate;
import com.homi.model.dao.repo.CompanyUserRepo;
import com.homi.model.dao.repo.ContractTemplateRepo;
import com.homi.model.contract.dto.ContractTemplateCreateDTO;
import com.homi.model.contract.dto.ContractTemplateDeleteDTO;
import com.homi.model.contract.dto.ContractTemplateQueryDTO;
import com.homi.model.contract.dto.ContractTemplateStatusDTO;
import com.homi.model.contract.vo.ContractTemplateListVO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/12
 */

@Service
@RequiredArgsConstructor
public class ContractTemplateService {
    private final CompanyUserRepo companyUserRepo;
    private final ContractTemplateRepo contractTemplateRepo;

    public PageVO<ContractTemplateListVO> getContractTemplateList(ContractTemplateQueryDTO query) {

        LambdaQueryWrapper<ContractTemplate> wrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(query.getContractType())) {
            wrapper.eq(ContractTemplate::getContractType, query.getContractType());
        }

        if (CharSequenceUtil.isNotBlank(query.getTemplateName())) {
            wrapper.like(ContractTemplate::getTemplateName, query.getTemplateName());
        }
        if (Objects.nonNull(query.getStatus())) {
            wrapper.eq(ContractTemplate::getStatus, query.getStatus());
        }

        wrapper.like(ContractTemplate::getTemplateName, query.getTemplateName());
        wrapper.orderByDesc(ContractTemplate::getId);

        Page<ContractTemplate> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        Page<ContractTemplate> dictDataPage = contractTemplateRepo.page(page, wrapper);

        PageVO<ContractTemplateListVO> pageVO = new PageVO<>();
        pageVO.setTotal(dictDataPage.getTotal());
        pageVO.setList(dictDataPage.getRecords().stream().map(c -> {
            ContractTemplateListVO contractTemplateListVO = BeanCopyUtils.copyBean(c, ContractTemplateListVO.class);
            if (Objects.nonNull(c.getDeptIds())) {
                assert contractTemplateListVO != null;
                contractTemplateListVO.setDeptIds(JSONUtil.toList(c.getDeptIds(), String.class));
            }
            return contractTemplateListVO;
        }).toList());
        pageVO.setCurrentPage(dictDataPage.getCurrent());
        pageVO.setPageSize(dictDataPage.getSize());
        pageVO.setPages(dictDataPage.getPages());


        return pageVO;
    }

    /**
     * 创建合同模板
     *
     * @param createDTO 合同模板创建DTO
     * @return 是否创建成功
     */
    public Long createContractTemplate(ContractTemplateCreateDTO createDTO) {
        ContractTemplate contractTemplate = BeanCopyUtils.copyBean(createDTO, ContractTemplate.class);

        assert contractTemplate != null;

        contractTemplate.setDeptIds(JSONUtil.toJsonStr(createDTO.getDeptIds()));
        contractTemplate.setStatus(ContractTemplateStatusEnum.UNEFFECTIVE.getCode());

        contractTemplateRepo.save(contractTemplate);

        return contractTemplate.getId();
    }

    /**
     * 更新合同模板
     *
     * @param createDTO 合同模板创建DTO
     * @return 是否更新成功
     */
    public Long updateContractTemplate(ContractTemplateCreateDTO createDTO) {
        ContractTemplate contractTemplate = BeanCopyUtils.copyBean(createDTO, ContractTemplate.class);

        assert contractTemplate != null;
        contractTemplate.setDeptIds(JSONUtil.toJsonStr(createDTO.getDeptIds()));
        contractTemplateRepo.updateById(contractTemplate);

        return contractTemplate.getId();
    }

    /**
     * 合同模板预览功能
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/12 17:33
     *
     * @param query 参数说明
     * @return java.lang.String
     */
    public byte[] previewContractTemplate(ContractTemplateQueryDTO query) {
        ContractTemplate contractTemplate = contractTemplateRepo.getById(query.getId());

        assert contractTemplate != null;

        return ConvertHtml2PdfUtils.generatePdf(contractTemplate.getTemplateContent());

    }

    public Boolean updateContractTemplateStatus(ContractTemplateStatusDTO updateDTO) {
        ContractTemplate contractTemplate = contractTemplateRepo.getById(updateDTO.getId());
        assert contractTemplate != null;
        contractTemplate.setStatus(updateDTO.getStatus());

        contractTemplateRepo.updateById(contractTemplate);

        return true;
    }

    public Boolean deleteContractTemplate(ContractTemplateDeleteDTO deleteDTO) {
        ContractTemplate contractTemplate = contractTemplateRepo.getById(deleteDTO.getId());
        assert contractTemplate != null;

        contractTemplateRepo.removeById(contractTemplate);

        return true;
    }

    /**
     * 获取用户可用的合同模板列表
     *
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/16 23:22
     *
     * @param companyId     公司ID
     * @param currentUserId 用户 ID
     * @param contractType  合同类型
     * @return java.util.List<com.homi.model.contract.vo.ContractTemplateListVO> 合同列表
     */
    public List<ContractTemplateListVO> getMyAvailableContractTemplates(Long companyId, Long currentUserId, Integer contractType) {
        CompanyUser companyUser = companyUserRepo.getCompanyUser(companyId, currentUserId);
        Long deptId = companyUser.getDeptId();
        if (Objects.isNull(deptId)) {
            return Collections.emptyList();
        }
        List<ContractTemplate> contractTemplateList = contractTemplateRepo.getContractTemplateList(deptId, contractType);

        return formatContractTemplateListVO(contractTemplateList);
    }

    public List<ContractTemplateListVO> getAllContractTemplateList(Long curCompanyId, Integer contractType) {
        List<ContractTemplate> contractTemplateList = contractTemplateRepo.getContractTemplateListByCompanyIdAndType(curCompanyId, contractType);

        return formatContractTemplateListVO(contractTemplateList);
    }

    /**
     * 格式化合同模板列表VO
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/17 01:13
     *
     * @param contractTemplateList 参数说明
     * @return java.util.@org.jetbrains.annotations.NotNull List<com.homi.model.contract.vo.ContractTemplateListVO>
     */
    @NotNull
    private List<ContractTemplateListVO> formatContractTemplateListVO(List<ContractTemplate> contractTemplateList) {
        return contractTemplateList.stream().map(c -> {
            ContractTemplateListVO contractTemplateListVO = BeanCopyUtils.copyBean(c, ContractTemplateListVO.class);
            if (Objects.nonNull(c.getDeptIds())) {
                assert contractTemplateListVO != null;
                contractTemplateListVO.setDeptIds(JSONUtil.toList(c.getDeptIds(), String.class));
            }
            return contractTemplateListVO;
        }).toList();
    }
}
