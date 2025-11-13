package com.homi.service.contract;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.contract.ContractTemplateCreateDTO;
import com.homi.domain.dto.contract.ContractTemplateQueryDTO;
import com.homi.domain.dto.contract.ContractTemplateDeleteDTO;
import com.homi.domain.dto.contract.ContractTemplateStatusDTO;
import com.homi.domain.enums.contract.ContractTemplateStatusEnum;
import com.homi.domain.vo.contract.ContractTemplateListDTO;
import com.homi.model.entity.ContractTemplate;
import com.homi.model.repo.ContractTemplateRepo;
import com.homi.service.pdf.PdfService;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final ContractTemplateRepo contractTemplateRepo;

    private final PdfService pdfService;

    public PageVO<ContractTemplateListDTO> getContractTemplateList(ContractTemplateQueryDTO query) {

        LambdaQueryWrapper<ContractTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractTemplate::getContractType, query.getContractType());

        if (CharSequenceUtil.isNotBlank(query.getTemplateName())) {
            wrapper.like(ContractTemplate::getTemplateName, query.getTemplateName());
        }
        if (Objects.nonNull(query.getStatus())) {
            wrapper.eq(ContractTemplate::getStatus, query.getStatus());
        }

        wrapper.like(ContractTemplate::getTemplateName, query.getTemplateName());

        Page<ContractTemplate> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        Page<ContractTemplate> sysDictDataPage = contractTemplateRepo.page(page, wrapper);

        PageVO<ContractTemplateListDTO> pageVO = new PageVO<>();
        pageVO.setTotal(sysDictDataPage.getTotal());
        pageVO.setList(sysDictDataPage.getRecords().stream().map(c -> BeanCopyUtils.copyBean(c, ContractTemplateListDTO.class)).toList());
        pageVO.setCurrentPage(sysDictDataPage.getCurrent());
        pageVO.setPageSize(sysDictDataPage.getSize());
        pageVO.setPages(sysDictDataPage.getPages());


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

        return pdfService.generatePdf(contractTemplate.getTemplateContent());

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
}
