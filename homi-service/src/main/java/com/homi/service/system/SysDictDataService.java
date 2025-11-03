package com.homi.service.system;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.dict.data.DictDataQueryDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.vo.dict.DictWithDataVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysDictData;
import com.homi.model.mapper.SysDictDataMapper;
import com.homi.model.repo.SysDictDataRepo;
import com.homi.model.repo.SysDictRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 字典数据表(SysDictData)表服务实现类
 *
 * @author sjh
 * @since 2024-04-24 10:35:56
 */
@Service
@RequiredArgsConstructor
public class SysDictDataService {

    private final SysDictDataMapper sysDictDataMapper;

    private final SysDictDataRepo sysDictDataRepo;

    private final SysDictRepo sysDictRepo;

    /**
     * 创建字典
     *
     * @param sysDictData 字典数据项对象
     * @return 字典数据项ID
     */
    public Long createDictData(SysDictData sysDictData) {
        if (CharSequenceUtil.isBlank(sysDictData.getValue())) {
            String cleaned = sysDictData.getName().replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "");
            sysDictData.setValue(PinyinUtil.getPinyin(cleaned, CharSequenceUtil.EMPTY));
        }
        // 转换后进行校验，value 不能为空
        if (CharSequenceUtil.isBlank(sysDictData.getValue())) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "字典数据项值不能为空");
        }

        validateDictDataUniqueness(null, sysDictData.getName(), sysDictData.getValue(), sysDictData.getDictId());
        sysDictData.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        sysDictData.setCreateTime(DateUtil.date());
        sysDictDataMapper.insert(sysDictData);
        return sysDictData.getId();
    }

    /**
     * 更新字典数据项
     *
     * @param sysDictData 字典数据项对象
     * @return 字典数据项ID
     */
    public Long updateDictData(SysDictData sysDictData) {
        SysDictData exists = sysDictDataMapper.selectById(sysDictData.getId());
        if (exists == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "修改的字典不存在");
        }
        validateDictDataUniqueness(sysDictData.getId(), sysDictData.getName(), sysDictData.getValue(), sysDictData.getDictId());
        sysDictData.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        sysDictData.setUpdateTime(DateUtil.date());
        sysDictDataMapper.updateById(sysDictData);

        return sysDictData.getId();
    }

    /**
     * 校验字典数据项名称和值的唯一性
     *
     * @param id    字典数据项ID，用于排除自身
     * @param name  字典数据项名称
     * @param value 字典数据项值
     */
    private void validateDictDataUniqueness(Long id, String name, String value, Long dictId) {
        SysDictData sysDictDataName = sysDictDataMapper.selectOne(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getName, name).eq(SysDictData::getDictId, dictId));
        if (sysDictDataName != null && !sysDictDataName.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "字典数据项名称不能重复");
        }
        SysDictData sysDictDataCode = sysDictDataMapper.selectOne(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getValue, value).eq(SysDictData::getDictId, dictId));
        if (sysDictDataCode != null && !sysDictDataCode.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "字典数据项值不能重复");
        }
    }

    public long getCountByDictId(Long id) {
        return sysDictDataRepo.count(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getDictId, id));
    }

    public PageVO<SysDictData> list(DictDataQueryDTO queryDTO) {
        LambdaQueryWrapper<SysDictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictData::getDictId, queryDTO.getDictId())
                .like(CharSequenceUtil.isNotEmpty(queryDTO.getName()), SysDictData::getName, queryDTO.getName())
                .like(CharSequenceUtil.isNotEmpty(queryDTO.getValue()), SysDictData::getValue, queryDTO.getValue())
                .eq(Objects.nonNull(queryDTO.getStatus()), SysDictData::getStatus, queryDTO.getStatus())
                .orderByAsc(SysDictData::getSort);

        Page<SysDictData> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getPageSize());

        Page<SysDictData> sysDictDataPage = sysDictDataRepo.page(page, queryWrapper);

        PageVO<SysDictData> pageVO = new PageVO<>();
        pageVO.setTotal(sysDictDataPage.getTotal());
        pageVO.setList(sysDictDataPage.getRecords());
        pageVO.setCurrentPage(sysDictDataPage.getCurrent());
        pageVO.setPageSize(sysDictDataPage.getSize());
        pageVO.setPages(sysDictDataPage.getPages());

        return pageVO;
    }

    public SysDictData getDictDataById(Long id) {
        return sysDictDataRepo.getById(id);
    }

    public Boolean deleteByIds(List<Long> idList) {
        return sysDictDataRepo.removeByIds(idList);
    }

    /**
     * 使用字典id查询数据项
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/13 19:57
     *
     * @param dictId 参数说明
     * @return java.util.List<com.homi.model.entity.SysDictData>
     */
    public List<SysDictData> listByDictId(Long dictId) {
        LambdaQueryWrapper<SysDictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictData::getDictId, dictId)
                .orderByAsc(SysDictData::getSort);

        return sysDictDataRepo.list(queryWrapper);
    }

    public List<DictWithDataVO> listByParentCode(Long parentId) {
        return sysDictRepo.getBaseMapper().listDictListWithData(parentId);
    }
}

