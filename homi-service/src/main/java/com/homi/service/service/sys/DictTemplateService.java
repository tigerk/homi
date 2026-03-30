package com.homi.service.service.sys;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.dict.template.dto.DictDataTemplateQueryDTO;
import com.homi.model.dict.template.dto.DictDataTemplateSaveDTO;
import com.homi.model.dict.template.dto.DictTemplateSaveDTO;
import com.homi.model.dict.template.vo.DictTemplateListVO;
import com.homi.model.dict.template.vo.DictTemplateSyncVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DictTemplateService {

    private static final String ROOT_PARENT_CODE = "0";
    private static final int MAX_SYNC_ROUNDS = 10;

    private final DictTemplateRepo dictTemplateRepo;
    private final DictDataTemplateRepo dictDataTemplateRepo;
    private final DictRepo dictRepo;
    private final DictDataRepo dictDataRepo;
    private final CompanyRepo companyRepo;
    private final CompanyDictSyncLogRepo companyDictSyncLogRepo;

    // ----------------------------------------------------------------
    // Query
    // ----------------------------------------------------------------

    public List<DictTemplateListVO> listTemplateTree() {
        List<DictTemplate> templateList = dictTemplateRepo.list(
            new LambdaQueryWrapper<DictTemplate>()
                .orderByAsc(DictTemplate::getSortOrder)
                .orderByAsc(DictTemplate::getId));

        Map<String, DictTemplateListVO> voMap = new LinkedHashMap<>();
        templateList.stream()
            .map(item -> BeanCopyUtils.copyBean(item, DictTemplateListVO.class))
            .filter(Objects::nonNull)
            .forEach(vo -> voMap.put(vo.getDictCode(), vo));

        List<DictTemplateListVO> roots = new ArrayList<>();
        voMap.values().forEach(vo -> {
            if (ROOT_PARENT_CODE.equals(vo.getParentCode()) || !voMap.containsKey(vo.getParentCode())) {
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

        Optional.ofNullable(queryDTO)
            .map(DictDataTemplateQueryDTO::getDictCode)
            .filter(StringUtils::hasText)
            .ifPresent(code -> queryWrapper.eq(DictDataTemplate::getDictCode, code));

        return dictDataTemplateRepo.list(queryWrapper);
    }

    // ----------------------------------------------------------------
    // Template CRUD
    // ----------------------------------------------------------------

    public Boolean saveTemplate(DictTemplateSaveDTO saveDTO) {
        if (!StringUtils.hasText(saveDTO.getDictCode())) {
            throw new BizException("字典编码不能为空");
        }

        boolean codeConflict = dictTemplateRepo.exists(
            new LambdaQueryWrapper<DictTemplate>()
                .eq(DictTemplate::getDictCode, saveDTO.getDictCode())
                .eq(DictTemplate::getVer, saveDTO.getVer())
                .ne(Objects.nonNull(saveDTO.getId()), DictTemplate::getId, saveDTO.getId()));
        if (codeConflict) {
            throw new BizException("同版本下字典编码已存在");
        }

        DictTemplate template = Optional.ofNullable(BeanCopyUtils.copyBean(saveDTO, DictTemplate.class))
            .orElseThrow(() -> new BizException("参数错误"));

        applyTemplateDefaults(template);
        return dictTemplateRepo.saveOrUpdate(template);
    }

    private void applyTemplateDefaults(DictTemplate template) {
        if (!StringUtils.hasText(template.getParentCode())) {
            template.setParentCode(ROOT_PARENT_CODE);
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
    }

    public Boolean deleteTemplate(Long id) {
        DictTemplate template = Optional.ofNullable(dictTemplateRepo.getById(id))
            .orElseThrow(() -> new BizException("模板不存在"));

        dictDataTemplateRepo.remove(new LambdaQueryWrapper<DictDataTemplate>()
            .eq(DictDataTemplate::getDictCode, template.getDictCode())
            .eq(DictDataTemplate::getVer, template.getVer()));
        return dictTemplateRepo.removeById(id);
    }

    // ----------------------------------------------------------------
    // DictData Template CRUD
    // ----------------------------------------------------------------

    public Boolean saveDataTemplate(DictDataTemplateSaveDTO saveDTO) {
        if (!StringUtils.hasText(saveDTO.getDictCode())) {
            throw new BizException("字典编码不能为空");
        }
        if (!StringUtils.hasText(saveDTO.getValue())) {
            throw new BizException("数据项值不能为空");
        }

        boolean valueConflict = dictDataTemplateRepo.exists(
            new LambdaQueryWrapper<DictDataTemplate>()
                .eq(DictDataTemplate::getDictCode, saveDTO.getDictCode())
                .eq(DictDataTemplate::getValue, saveDTO.getValue())
                .eq(DictDataTemplate::getVer, saveDTO.getVer())
                .ne(Objects.nonNull(saveDTO.getId()), DictDataTemplate::getId, saveDTO.getId()));
        if (valueConflict) {
            throw new BizException("同版本下该数据项值已存在");
        }

        DictDataTemplate template = Optional.ofNullable(BeanCopyUtils.copyBean(saveDTO, DictDataTemplate.class))
            .orElseThrow(() -> new BizException("参数错误"));

        applyDataTemplateDefaults(template);
        return dictDataTemplateRepo.saveOrUpdate(template);
    }

    private void applyDataTemplateDefaults(DictDataTemplate template) {
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
    }

    public Boolean deleteDataTemplate(Long id) {
        return dictDataTemplateRepo.removeById(id);
    }

    // ----------------------------------------------------------------
    // Sync
    // ----------------------------------------------------------------

    @Transactional(rollbackFor = Exception.class)
    public DictTemplateSyncVO syncAllCompanyDict() {
        int toVer = getLatestTemplateVersion();
        if (toVer <= 0) {
            throw new BizException("未配置任何模板版本，无法同步");
        }

        List<Company> companyList = companyRepo.list();
        int successCount = 0;
        int failCount = 0;

        for (Company company : companyList) {
            int fromVer = Objects.nonNull(company.getDictVer()) ? company.getDictVer() : 0;
            if (fromVer >= toVer) {
                successCount++;
                continue;
            }

            CompanyDictSyncLog syncLog = buildSyncLog(company.getId(), fromVer, toVer);
            companyDictSyncLogRepo.save(syncLog);

            try {
                // 同步核心方法
                SyncCounter counter = syncCompanyDictByTemplate(company.getId(), toVer);
                syncLog.setStatus(1);
                syncLog.setSuccessCount(counter.success());
                syncLog.setFailCount(counter.fail());
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

    @Transactional(rollbackFor = Exception.class)
    public void syncCompanyDictByLatestTemplate(Long companyId) {
        int toVer = getLatestTemplateVersion();
        if (toVer <= 0) {
            return;
        }

        Company company = Optional.ofNullable(companyRepo.getById(companyId))
            .orElseThrow(() -> new BizException("公司不存在"));

        syncCompanyDictByTemplate(companyId, toVer);
        company.setDictVer(toVer);
        company.setDictSyncTime(DateUtil.date());
        companyRepo.updateById(company);
    }

    private CompanyDictSyncLog buildSyncLog(Long companyId, int fromVer, int toVer) {
        CompanyDictSyncLog syncLog = new CompanyDictSyncLog();
        syncLog.setCompanyId(companyId);
        syncLog.setFromVer(fromVer);
        syncLog.setToVer(toVer);
        syncLog.setStatus(0);
        syncLog.setSuccessCount(0);
        syncLog.setFailCount(0);
        syncLog.setStartTime(DateUtil.date());
        return syncLog;
    }

    /**
     * 获取最新的模板版本
     */
    private int getLatestTemplateVersion() {
        int dictVer = Optional.ofNullable(dictTemplateRepo.getOne(
                new LambdaQueryWrapper<DictTemplate>().select(DictTemplate::getVer).orderByDesc(DictTemplate::getVer).last("limit 1")))
            .map(DictTemplate::getVer)
            .orElse(0);
        int dataVer = Optional.ofNullable(dictDataTemplateRepo.getOne(
                new LambdaQueryWrapper<DictDataTemplate>().select(DictDataTemplate::getVer).orderByDesc(DictDataTemplate::getVer).last("limit 1")))
            .map(DictDataTemplate::getVer)
            .orElse(0);
        return Math.max(dictVer, dataVer);
    }

    // ----------------------------------------------------------------
    // Core sync — cognitive complexity ≤ 15
    // ----------------------------------------------------------------
    private SyncCounter syncCompanyDictByTemplate(Long companyId, Integer toVer) {
        Date now = DateUtil.date();
        Map<String, Dict> dictByCode = loadCompanyDictMap(companyId);

        int[] counter = {0, 0}; // [success, fail]
        syncDictTemplates(companyId, getEffectiveDictTemplateMap(toVer), dictByCode, now, counter);
        syncDictDataTemplates(companyId, getEffectiveDictDataTemplateMap(toVer), dictByCode, now, counter);

        return new SyncCounter(counter[0], counter[1]);
    }

    private Map<String, Dict> loadCompanyDictMap(Long companyId) {
        Map<String, Dict> dictByCode = new HashMap<>();
        dictRepo.list(new LambdaQueryWrapper<Dict>().eq(Dict::getCompanyId, companyId))
            .forEach(item -> dictByCode.put(item.getDictCode(), item));
        return dictByCode;
    }

    /**
     * 同步字典模板
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/3 16:43
     *
     * @param companyId       公司 ID
     * @param dictTemplateMap 字典模板 Map
     * @param dictByCode      字典 Map，key 为字典编码
     * @param now             当前时间
     * @param counter         同步计数器，[success, fail]
     */
    private void syncDictTemplates(Long companyId, Map<String, DictTemplate> dictTemplateMap, Map<String, Dict> dictByCode, Date now, int[] counter) {
        List<DictTemplate> rootTemplates = new ArrayList<>();
        List<DictTemplate> pendingTemplates = new ArrayList<>();
        dictTemplateMap.values().forEach(item -> {
            if (ROOT_PARENT_CODE.equals(item.getParentCode())) {
                rootTemplates.add(item);
            } else {
                pendingTemplates.add(item);
            }
        });

        for (DictTemplate template : rootTemplates) {
            accumulate(counter, upsertCompanyDict(companyId, 0L, template, dictByCode, now));
        }
        syncChildDictTemplates(companyId, pendingTemplates, dictByCode, now, counter);
    }

    /**
     * 递归同步子字典模板
     */
    private void syncChildDictTemplates(Long companyId, List<DictTemplate> pendingTemplates, Map<String, Dict> dictByCode, Date now, int[] counter) {
        int rounds = 0;
        while (!pendingTemplates.isEmpty() && rounds < MAX_SYNC_ROUNDS) {
            rounds++;
            boolean progressed = processOneBatchOfPending(companyId, pendingTemplates, dictByCode, now, counter);
            if (!progressed) {
                break;
            }
        }
        counter[1] += pendingTemplates.size(); // unresolved orphans → fail
    }

    private boolean processOneBatchOfPending(Long companyId, List<DictTemplate> pendingTemplates,
                                             Map<String, Dict> dictByCode, Date now, int[] counter) {
        boolean progressed = false;
        Iterator<DictTemplate> iterator = pendingTemplates.iterator();
        while (iterator.hasNext()) {
            DictTemplate template = iterator.next();
            Dict parent = dictByCode.get(template.getParentCode());
            if (Objects.isNull(parent)) {
                continue;
            }
            accumulate(counter, upsertCompanyDict(companyId, parent.getId(), template, dictByCode, now));
            iterator.remove();
            progressed = true;
        }
        return progressed;
    }

    private void syncDictDataTemplates(Long companyId, Map<String, DictDataTemplate> dataTemplateMap,
                                       Map<String, Dict> dictByCode, Date now, int[] counter) {
        for (DictDataTemplate dataTemplate : dataTemplateMap.values()) {
            Dict dict = dictByCode.get(dataTemplate.getDictCode());
            if (Objects.isNull(dict)) {
                counter[1]++;
                continue;
            }
            accumulate(counter, upsertCompanyDictData(companyId, dict.getId(), dataTemplate, now));
        }
    }

    private void accumulate(int[] counter, boolean success) {
        counter[success ? 0 : 1]++;
    }

    // ----------------------------------------------------------------
    // Template map loaders
    // ----------------------------------------------------------------

    private Map<String, DictTemplate> getEffectiveDictTemplateMap(Integer toVer) {
        Map<String, DictTemplate> templateMap = new LinkedHashMap<>();
        dictTemplateRepo.list(new LambdaQueryWrapper<DictTemplate>()
                .le(DictTemplate::getVer, toVer)
                .eq(DictTemplate::getEnabled, true)
                .orderByAsc(DictTemplate::getVer)
                .orderByAsc(DictTemplate::getUpdateTime))
            .forEach(item -> templateMap.put(item.getDictCode(), item));
        return templateMap;
    }

    private Map<String, DictDataTemplate> getEffectiveDictDataTemplateMap(Integer toVer) {
        Map<String, DictDataTemplate> templateMap = new LinkedHashMap<>();
        dictDataTemplateRepo.list(new LambdaQueryWrapper<DictDataTemplate>()
                .le(DictDataTemplate::getVer, toVer)
                .eq(DictDataTemplate::getEnabled, true)
                .orderByAsc(DictDataTemplate::getVer)
                .orderByAsc(DictDataTemplate::getUpdateTime))
            .forEach(item -> templateMap.put(item.getDictCode() + "#" + item.getValue(), item));
        return templateMap;
    }

    // ----------------------------------------------------------------
    // Upsert helpers
    // ----------------------------------------------------------------

    private boolean upsertCompanyDict(Long companyId, Long parentId, DictTemplate template,
                                      Map<String, Dict> dictByCode, Date now) {
        Dict exist = dictByCode.get(template.getDictCode());
        if (Objects.isNull(exist)) {
            return insertCompanyDict(companyId, parentId, template, dictByCode, now);
        }
        if (Boolean.TRUE.equals(exist.getFromTemplate()) && !Boolean.TRUE.equals(exist.getLocked())) {
            return updateCompanyDict(exist, parentId, template, dictByCode, now);
        }
        return true;
    }

    private boolean insertCompanyDict(Long companyId, Long parentId, DictTemplate template,
                                      Map<String, Dict> dictByCode, Date now) {
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

    /**
     * 更新公司字典
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/3 16:52

     * @param exist 存在的字典
     * @param parentId 父字典ID
     * @param template 参数
     * @param dictByCode 字典缓存
     * @param now 当前时间
     * @return boolean
     */
    private boolean updateCompanyDict(Dict exist, Long parentId, DictTemplate template, Map<String, Dict> dictByCode, Date now) {
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

    private boolean upsertCompanyDictData(Long companyId, Long dictId, DictDataTemplate template, Date now) {
        DictData exist = dictDataRepo.getOne(new LambdaQueryWrapper<DictData>()
            .eq(DictData::getCompanyId, companyId)
            .eq(DictData::getDictId, dictId)
            .eq(DictData::getValue, template.getValue())
            .last("limit 1"), false);

        if (Objects.isNull(exist)) {
            return insertCompanyDictData(companyId, dictId, template, now);
        }
        if (Boolean.TRUE.equals(exist.getFromTemplate()) && !Boolean.TRUE.equals(exist.getLocked())) {
            return updateCompanyDictData(exist, template, now);
        }
        return true;
    }

    private boolean insertCompanyDictData(Long companyId, Long dictId, DictDataTemplate template, Date now) {
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

    private boolean updateCompanyDictData(DictData exist, DictDataTemplate template, Date now) {
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

    // ----------------------------------------------------------------
    // Inner types
    // ----------------------------------------------------------------

    private record SyncCounter(int success, int fail) {
    }
}
