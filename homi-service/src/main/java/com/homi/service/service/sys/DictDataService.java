package com.homi.service.service.sys;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.DictData;
import com.homi.model.dao.mapper.DictDataMapper;
import com.homi.model.dao.repo.DictDataRepo;
import com.homi.model.dao.repo.DictRepo;
import com.homi.model.dict.data.dto.DictDataQueryDTO;
import com.homi.model.dict.data.dto.DictDataUpdateDTO;
import com.homi.model.dict.vo.DictWithDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 字典数据表(DictData)表服务实现类
 *
 * @author tigerk
 * @since 2024-04-24 10:35:56
 */
@Service
@RequiredArgsConstructor
public class DictDataService {

    private final DictDataMapper dictDataMapper;

    private final DictDataRepo dictDataRepo;

    private final DictRepo dictRepo;

    /**
     * 创建字典
     *
     * @param dictData 字典数据项对象
     * @return 字典数据项ID
     */
    public Long createDictData(DictData dictData) {
        if (CharSequenceUtil.isBlank(dictData.getValue())) {
            String cleaned = dictData.getName().replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "");
            dictData.setValue(PinyinUtil.getPinyin(cleaned, CharSequenceUtil.EMPTY));
        }
        // 转换后进行校验，value 不能为空
        if (CharSequenceUtil.isBlank(dictData.getValue())) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "字典数据项值不能为空");
        }

        validateDictDataUniqueness(null, dictData.getName(), dictData.getValue(), dictData.getDictId());
        dictData.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        dictData.setCreateTime(DateUtil.date());
        dictDataMapper.insert(dictData);
        return dictData.getId();
    }

    /**
     * 更新字典数据项
     *
     * @param dictData 字典数据项对象
     * @return 字典数据项ID
     */
    public Long updateDictData(DictData dictData) {
        DictData exists = dictDataMapper.selectById(dictData.getId());
        if (exists == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "修改的字典不存在");
        }
        validateDictDataUniqueness(dictData.getId(), dictData.getName(), dictData.getValue(), dictData.getDictId());
        dictData.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        dictData.setUpdateTime(DateUtil.date());
        dictDataMapper.updateById(dictData);

        return dictData.getId();
    }

    /**
     * 校验字典数据项名称和值的唯一性
     *
     * @param id    字典数据项ID，用于排除自身
     * @param name  字典数据项名称
     * @param value 字典数据项值
     */
    private void validateDictDataUniqueness(Long id, String name, String value, Long dictId) {
        DictData dictDataName = dictDataMapper.selectOne(new LambdaQueryWrapper<DictData>().eq(DictData::getName, name).eq(DictData::getDictId, dictId));
        if (dictDataName != null && !dictDataName.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "字典数据项名称不能重复");
        }
        DictData dictDataCode = dictDataMapper.selectOne(new LambdaQueryWrapper<DictData>().eq(DictData::getValue, value).eq(DictData::getDictId, dictId));
        if (dictDataCode != null && !dictDataCode.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "字典数据项值不能重复");
        }
    }

    public long getCountByDictId(Long id) {
        return dictDataRepo.count(new LambdaQueryWrapper<DictData>().eq(DictData::getDictId, id));
    }

    public PageVO<DictData> list(DictDataQueryDTO queryDTO) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictData::getDictId, queryDTO.getDictId())
            .like(CharSequenceUtil.isNotEmpty(queryDTO.getName()), DictData::getName, queryDTO.getName())
            .like(CharSequenceUtil.isNotEmpty(queryDTO.getValue()), DictData::getValue, queryDTO.getValue())
            .eq(Objects.nonNull(queryDTO.getStatus()), DictData::getStatus, queryDTO.getStatus())
            .orderByAsc(DictData::getSortOrder);

        Page<DictData> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getPageSize());

        Page<DictData> sysDictDataPage = dictDataRepo.page(page, queryWrapper);

        PageVO<DictData> pageVO = new PageVO<>();
        pageVO.setTotal(sysDictDataPage.getTotal());
        pageVO.setList(sysDictDataPage.getRecords());
        pageVO.setCurrentPage(sysDictDataPage.getCurrent());
        pageVO.setPageSize(sysDictDataPage.getSize());
        pageVO.setPages(sysDictDataPage.getPages());

        return pageVO;
    }

    public DictData getDictDataById(Long id) {
        return dictDataRepo.getById(id);
    }

    public List<DictData> getDictDataByIds(List<Long> idList) {
        return dictDataRepo.listByIds(idList);
    }

    public Boolean deleteByIds(List<Long> idList) {
        return dictDataRepo.removeByIds(idList);
    }

    /**
     * 使用字典id查询数据项
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/13 19:57
     *
     * @param dictId 参数说明
     * @return java.util.List<com.homi.model.entity.DictData>
     */
    public List<DictData> listByDictId(Long dictId) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictData::getDictId, dictId)
            .orderByAsc(DictData::getSortOrder);

        return dictDataRepo.list(queryWrapper);
    }

    public List<DictWithDataVO> listByParentCode(Long parentId) {
        return dictRepo.getBaseMapper().listDictListWithData(parentId);
    }

    public Boolean updateDictDataStatus(DictDataUpdateDTO dictData) {
        DictData update = new DictData();
        update.setId(dictData.getId());
        update.setStatus(dictData.getStatus());
        update.setUpdateBy(dictData.getUpdateBy());
        update.setUpdateTime(DateUtil.date());

        return dictDataRepo.updateById(update);
    }
}
