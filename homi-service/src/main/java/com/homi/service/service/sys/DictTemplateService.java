package com.homi.service.service.sys;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.Company;
import com.homi.model.dao.entity.CompanyDictSyncLog;
import com.homi.model.dao.entity.Dict;
import com.homi.model.dao.entity.DictData;
import com.homi.model.dao.entity.DictDataTemplate;
import com.homi.model.dao.entity.DictTemplate;
import com.homi.model.dao.repo.CompanyDictSyncLogRepo;
import com.homi.model.dao.repo.CompanyRepo;
import com.homi.model.dao.repo.DictDataRepo;
import com.homi.model.dao.repo.DictDataTemplateRepo;
import com.homi.model.dao.repo.DictRepo;
import com.homi.model.dao.repo.DictTemplateRepo;
import com.homi.model.dict.template.dto.DictDataTemplateQueryDTO;
import com.homi.model.dict.template.dto.DictDataTemplateSaveDTO;
import com.homi.model.dict.template.dto.DictTemplateSaveDTO;
import com.homi.model.dict.template.vo.DictTemplateListVO;
import com.homi.model.dict.template.vo.DictTemplateSyncVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DictTemplateService {
    private final DictTemplateRepo dictTemplateRepo;
    private final DictDataTemplateRepo dictDataTemplateRepo;
    private final DictRepo dictRepo;
    private final DictDataRepo dictDataRepo;
    private final CompanyRepo companyRepo;
    private final CompanyDictSyncLogRepo companyDictSyncLogRepo;

    public List<DictTemplateListVO> listTemplateTree() {
        List<DictTemplate> templateList = dictTemplateRepo.list(new LambdaQueryWrapper<DictTemplate>()
            .orderByAsc(DictTemplate::getSortOrder)
            .orderByAsc(DictTemplate::getId));
        Map<String, DictTemplateListVO> voMap = new LinkedHashMap<>();
        templateList.forEach(item -> {
            DictTemplateListVO vo = BeanCopyUtils.copyBean(item, DictTemplateListVO.class);
            if (Objects.nonNull(vo)) {
                voMap.put(item.getDictCode(), vo);
            }
        });

        List<DictTemplateListVO> roots = new ArrayList<>();
        voMap.values().forEach(vo -> {
            if ("0".equals(vo.getParentCode()) || Objects.isNull(voMap.get(vo.getParentCode()))) {
                roots.add(vo);
            } else {
                voMap.get(vo.getParentCode()).getChildren().add(vo);
            }
        });
        return roots;
    }

    public List<DictDataTemplate> listDataTemplate(DictDataTemplateQueryDTO queryDTO) {
        LambdaQueryWrapper<DictDataTemplate> queryWrapper = new LambdaQueryWrapper<DictDataTemplate>()
            .orderByAsc(DictDataTemplate::getSortOrder)
            .orderByAsc(DictDataTemplate::getId);
        if (Objects.nonNull(queryDTO) && Objects.nonNull(queryDTO.getDictCode()) && !queryDTO.getDictCode().isBlank()) {
            queryWrapper.eq(DictDataTemplate::getDictCode, queryDTO.getDictCode());
        }
        return dictDataTemplateRepo.list(queryWrapper);
    }

    public Boolean saveTemplate(DictTemplateSaveDTO saveDTO) {
        if (Objects.isNull(saveDTO.getDictCode()) || saveDTO.getDictCode().isBlank()) {
            throw new BizException("字典编码不能为空");
        }
        DictTemplate existsCode = dictTemplateRepo.getOne(new LambdaQueryWrapper<DictTemplate>()
            .eq(DictTemplate::getDictCode, saveDTO.getDictCode())
            .eq(DictTemplate::getVer, saveDTO.getVer()));
        if (Objects.nonNull(existsCode) && !Objects.equals(existsCode.getId(), saveDTO.getId())) {
            throw new BizException("同版本下字典编码已存在");
        }

        DictTemplate template = BeanCopyUtils.copyBean(saveDTO, DictTemplate.class);
        if (Objects.isNull(template)) {
            throw new BizException("参数错误");
        }
        if (Objects.isNull(template.getParentCode()) || template.getParentCode().isBlank()) {
            template.setParentCode("0");
        }
        if (Objects.isNull(template.getSortOrder())) {
            template.setSortOrder(0);
        }
        if (Objects.isNull(template.getStatus())) {
            template.setStatus(1);
        }
        if (Objects.isNull(template.getEnabled())) {
            template.setEnabled(Boolean.TRUE);
        }
        if (Objects.isNull(template.getHidden())) {
            template.setHidden(Boolean.FALSE);
        }
        if (Objects.isNull(template.getVer())) {
            template.setVer(1);
        }

        return dictTemplateRepo.saveOrUpdate(template);
    }

    public Boolean deleteTemplate(Long id) {
        DictTemplate template = dictTemplateRepo.getById(id);
        if (Objects.isNull(template)) {
            throw new BizException("模板不存在");
        }
        dictDataTemplateRepo.remove(new LambdaQueryWrapper<DictDataTemplate>()
            .eq(DictDataTemplate::getDictCode, template.getDictCode())
            .eq(DictDataTemplate::getVer, template.getVer()));
        return dictTemplateRepo.removeById(id);
    }

    public Boolean saveDataTemplate(DictDataTemplateSaveDTO saveDTO) {
        if (Objects.isNull(saveDTO.getDictCode()) || saveDTO.getDictCode().isBlank()) {
            throw new BizException("字典编码不能为空");
        }
        if (Objects.isNull(saveDTO.getValue()) || saveDTO.getValue().isBlank()) {
            throw new BizException("数据项值不能为空");
        }
        DictDataTemplate exists = dictDataTemplateRepo.getOne(new LambdaQueryWrapper<DictDataTemplate>()
            .eq(DictDataTemplate::getDictCode, saveDTO.getDictCode())
            .eq(DictDataTemplate::getValue, saveDTO.getValue())
            .eq(DictDataTemplate::getVer, saveDTO.getVer()));
        if (Objects.nonNull(exists) && !Objects.equals(exists.getId(), saveDTO.getId())) {
            throw new BizException("同版本下该数据项值已存在");
        }

        DictDataTemplate template = BeanCopyUtils.copyBean(saveDTO, DictDataTemplate.class);
        if (Objects.isNull(template)) {
            throw new BizException("参数错误");
        }
        if (Objects.isNull(template.getSortOrder())) {
            template.setSortOrder(0);
        }
        if (Objects.isNull(template.getStatus())) {
            template.setStatus(1);
        }
        if (Objects.isNull(template.getEnabled())) {
            template.setEnabled(Boolean.TRUE);
        }
        if (Objects.isNull(template.getDeletable())) {
            template.setDeletable(Boolean.TRUE);
        }
        if (Objects.isNull(template.getVer())) {
            template.setVer(1);
        }
        return dictDataTemplateRepo.saveOrUpdate(template);
    }

    public Boolean deleteDataTemplate(Long id) {
        return dictDataTemplateRepo.removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public DictTemplateSyncVO syncAllCompanyDict() {
        Integer toVer = getLatestTemplateVersion();
        if (toVer <= 0) {
            throw new BizException("未配置任何模板版本，无法同步");
        }

        List<Company> companyList = companyRepo.list();
        int successCount = 0;
        int failCount = 0;

        for (Company company : companyList) {
            Integer fromVer = Objects.nonNull(company.getDictVer()) ? company.getDictVer() : 0;
            if (fromVer >= toVer) {
                successCount++;
                continue;
            }

            CompanyDictSyncLog syncLog = new CompanyDictSyncLog();
            syncLog.setCompanyId(company.getId());
            syncLog.setFromVer(fromVer);
            syncLog.setToVer(toVer);
            syncLog.setStatus(0);
            syncLog.setSuccessCount(0);
            syncLog.setFailCount(0);
            syncLog.setStartTime(DateUtil.date());
            companyDictSyncLogRepo.save(syncLog);

            try {
                SyncCounter counter = syncCompanyDictByTemplate(company.getId(), toVer);
                syncLog.setStatus(1);
                syncLog.setSuccessCount(counter.getSuccess());
                syncLog.setFailCount(counter.getFail());
                successCount++;

                company.setDictVer(toVer);
                company.setDictSyncTime(DateUtil.date());
                companyRepo.updateById(company);
            } catch (Exception e) {
                syncLog.setStatus(-1);
                syncLog.setErrorMsg(e.getMessage());
                failCount++;
            } finally {
                syncLog.setEndTime(DateUtil.date());
                companyDictSyncLogRepo.updateById(syncLog);
            }
        }

        DictTemplateSyncVO syncVO = new DictTemplateSyncVO();
        syncVO.setToVer(toVer);
        syncVO.setCompanyCount(companyList.size());
        syncVO.setSuccessCount(successCount);
        syncVO.setFailCount(failCount);
        return syncVO;
    }

    private Integer getLatestTemplateVersion() {
        DictTemplate dictTemplate = dictTemplateRepo.getOne(new LambdaQueryWrapper<DictTemplate>()
            .orderByDesc(DictTemplate::getVer));
        DictDataTemplate dataTemplate = dictDataTemplateRepo.getOne(new LambdaQueryWrapper<DictDataTemplate>()
            .orderByDesc(DictDataTemplate::getVer));
        int dictVer = Objects.isNull(dictTemplate) ? 0 : dictTemplate.getVer();
        int dataVer = Objects.isNull(dataTemplate) ? 0 : dataTemplate.getVer();
        return Math.max(dictVer, dataVer);
    }

    private SyncCounter syncCompanyDictByTemplate(Long companyId, Integer toVer) {
        Date now = DateUtil.date();
        int success = 0;
        int fail = 0;

        List<Dict> companyDicts = dictRepo.list(new LambdaQueryWrapper<Dict>()
            .eq(Dict::getCompanyId, companyId));
        Map<String, Dict> dictByCode = new HashMap<>();
        companyDicts.forEach(item -> dictByCode.put(item.getDictCode(), item));

        Map<String, DictTemplate> dictTemplateMap = getEffectiveDictTemplateMap(toVer);
        List<DictTemplate> rootTemplates = new ArrayList<>();
        List<DictTemplate> pendingTemplates = new ArrayList<>();
        dictTemplateMap.values().forEach(item -> {
            if ("0".equals(item.getParentCode())) {
                rootTemplates.add(item);
            } else {
                pendingTemplates.add(item);
            }
        });

        for (DictTemplate template : rootTemplates) {
            if (upsertCompanyDict(companyId, 0L, template, dictByCode, now)) {
                success++;
            } else {
                fail++;
            }
        }

        int rounds = 0;
        while (!pendingTemplates.isEmpty() && rounds < 10) {
            rounds++;
            boolean progressed = false;
            Iterator<DictTemplate> iterator = pendingTemplates.iterator();
            while (iterator.hasNext()) {
                DictTemplate template = iterator.next();
                Dict parent = dictByCode.get(template.getParentCode());
                if (Objects.isNull(parent)) {
                    continue;
                }
                if (upsertCompanyDict(companyId, parent.getId(), template, dictByCode, now)) {
                    success++;
                } else {
                    fail++;
                }
                iterator.remove();
                progressed = true;
            }
            if (!progressed) {
                break;
            }
        }
        if (!pendingTemplates.isEmpty()) {
            fail += pendingTemplates.size();
        }

        Map<String, DictDataTemplate> dataTemplateMap = getEffectiveDictDataTemplateMap(toVer);
        for (DictDataTemplate dataTemplate : dataTemplateMap.values()) {
            Dict dict = dictByCode.get(dataTemplate.getDictCode());
            if (Objects.isNull(dict)) {
                fail++;
                continue;
            }
            if (upsertCompanyDictData(companyId, dict.getId(), dataTemplate, now)) {
                success++;
            } else {
                fail++;
            }
        }

        return new SyncCounter(success, fail);
    }

    private Map<String, DictTemplate> getEffectiveDictTemplateMap(Integer toVer) {
        List<DictTemplate> templateList = dictTemplateRepo.list(new LambdaQueryWrapper<DictTemplate>()
            .le(DictTemplate::getVer, toVer)
            .eq(DictTemplate::getEnabled, true)
            .orderByAsc(DictTemplate::getVer)
            .orderByAsc(DictTemplate::getUpdateTime));

        Map<String, DictTemplate> templateMap = new LinkedHashMap<>();
        templateList.forEach(item -> templateMap.put(item.getDictCode(), item));
        return templateMap;
    }

    private Map<String, DictDataTemplate> getEffectiveDictDataTemplateMap(Integer toVer) {
        List<DictDataTemplate> templateList = dictDataTemplateRepo.list(new LambdaQueryWrapper<DictDataTemplate>()
            .le(DictDataTemplate::getVer, toVer)
            .eq(DictDataTemplate::getEnabled, true)
            .orderByAsc(DictDataTemplate::getVer)
            .orderByAsc(DictDataTemplate::getUpdateTime));

        Map<String, DictDataTemplate> templateMap = new LinkedHashMap<>();
        templateList.forEach(item -> templateMap.put(item.getDictCode() + "#" + item.getValue(), item));
        return templateMap;
    }

    private boolean upsertCompanyDict(Long companyId, Long parentId, DictTemplate template, Map<String, Dict> dictByCode, Date now) {
        Dict exist = dictByCode.get(template.getDictCode());
        if (Objects.isNull(exist)) {
            Dict dict = new Dict();
            dict.setCompanyId(companyId);
            dict.setParentId(parentId);
            dict.setDictCode(template.getDictCode());
            dict.setDictName(template.getDictName());
            dict.setSortOrder(template.getSortOrder());
            dict.setStatus(template.getStatus());
            dict.setHidden(template.getHidden());
            dict.setRemark(template.getRemark());
            dict.setFromTemplate(Boolean.TRUE);
            dict.setLocked(Boolean.FALSE);
            dict.setTemplateVer(template.getVer());
            dict.setSyncTime(now);
            boolean saved = dictRepo.save(dict);
            if (saved) {
                dictByCode.put(dict.getDictCode(), dict);
            }
            return saved;
        }

        if (Boolean.TRUE.equals(exist.getFromTemplate()) && !Boolean.TRUE.equals(exist.getLocked())) {
            exist.setParentId(parentId);
            exist.setDictName(template.getDictName());
            exist.setSortOrder(template.getSortOrder());
            exist.setStatus(template.getStatus());
            exist.setHidden(template.getHidden());
            exist.setRemark(template.getRemark());
            exist.setTemplateVer(template.getVer());
            exist.setSyncTime(now);
            boolean updated = dictRepo.updateById(exist);
            if (updated) {
                dictByCode.put(exist.getDictCode(), exist);
            }
            return updated;
        }

        return true;
    }

    private boolean upsertCompanyDictData(Long companyId, Long dictId, DictDataTemplate template, Date now) {
        DictData exist = dictDataRepo.getOne(new LambdaQueryWrapper<DictData>()
            .eq(DictData::getCompanyId, companyId)
            .eq(DictData::getDictId, dictId)
            .eq(DictData::getValue, template.getValue())
            .last("limit 1"), false);
        if (Objects.isNull(exist)) {
            DictData dictData = new DictData();
            dictData.setCompanyId(companyId);
            dictData.setDictId(dictId);
            dictData.setName(template.getName());
            dictData.setValue(template.getValue());
            dictData.setSortOrder(template.getSortOrder());
            dictData.setColor(template.getColor());
            dictData.setStatus(template.getStatus());
            dictData.setDeletable(template.getDeletable());
            dictData.setRemark(template.getRemark());
            dictData.setFromTemplate(Boolean.TRUE);
            dictData.setLocked(Boolean.FALSE);
            dictData.setTemplateVer(template.getVer());
            dictData.setSyncTime(now);
            return dictDataRepo.save(dictData);
        }

        if (Boolean.TRUE.equals(exist.getFromTemplate()) && !Boolean.TRUE.equals(exist.getLocked())) {
            exist.setName(template.getName());
            exist.setSortOrder(template.getSortOrder());
            exist.setColor(template.getColor());
            exist.setStatus(template.getStatus());
            exist.setDeletable(template.getDeletable());
            exist.setRemark(template.getRemark());
            exist.setTemplateVer(template.getVer());
            exist.setSyncTime(now);
            return dictDataRepo.updateById(exist);
        }

        return true;
    }

    private static class SyncCounter {
        private final int success;
        private final int fail;

        private SyncCounter(int success, int fail) {
            this.success = success;
            this.fail = fail;
        }

        public int getSuccess() {
            return success;
        }

        public int getFail() {
            return fail;
        }
    }
}
