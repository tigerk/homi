package com.homi.saas.service.service.system;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.model.dto.dict.DictQueryDTO;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.model.vo.dict.DictWithDataVO;
import com.homi.model.vo.dict.DictVO;
import com.homi.common.lib.exception.BizException;
import com.homi.model.dao.entity.Dict;
import com.homi.model.dao.mapper.DictMapper;
import com.homi.model.dao.repo.DictRepo;
import com.homi.common.lib.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字典表(Dict)表服务实现类
 *
 * @author sjh
 * @since 2024-04-24 10:35:55
 */
@Service
@RequiredArgsConstructor
public class DictService {

    private final DictMapper dictMapper;

    private final DictRepo dictRepo;

    /**
     * 创建字典
     *
     * @param dict 字典对象
     * @return 字典ID
     */
    public Long createDict(Dict dict) {
        validateDictUniqueness(null, dict.getDictName(), dict.getDictCode());
        dict.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        dictMapper.insert(dict);
        return dict.getId();
    }

    /**
     * 更新字典
     *
     * @param dict 字典对象
     * @return 字典ID
     */

    public Long updateDict(Dict dict) {
        Dict exists = dictMapper.selectById(dict.getId());
        if (exists == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "修改的字典不存在");
        }
        validateDictUniqueness(dict.getId(), dict.getDictName(), dict.getDictCode());
        dict.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        dictMapper.updateById(dict);
        return dict.getId();
    }


    public List<DictWithDataVO> listAllDictAndData() {
        return dictMapper.listAllDictWithData();
    }

    /**
     * 校验字典名称和编码的唯一性
     *
     * @param id   字典ID，用于排除自身
     * @param name 字典名称
     * @param code 字典编码
     */
    private void validateDictUniqueness(Long id, String name, String code) {
        Dict dictName = dictMapper.selectOne(new LambdaQueryWrapper<Dict>().eq(Dict::getDictName, name));
        if (dictName != null && !dictName.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "字典名称不能重复");
        }
        Dict dictCode = dictMapper.selectOne(new LambdaQueryWrapper<Dict>().eq(Dict::getDictCode, code));
        if (dictCode != null && !dictCode.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "字典编码不能重复");
        }
    }


    public List<DictVO> list(DictQueryDTO queryDTO) {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(CharSequenceUtil.isNotEmpty(queryDTO.getDictName()), Dict::getDictName, queryDTO.getDictName())
                .like(CharSequenceUtil.isNotEmpty(queryDTO.getDictCode()), Dict::getDictCode, queryDTO.getDictCode())
                .eq(Dict::getHidden, Boolean.FALSE)
                .eq(Objects.nonNull(queryDTO.getStatus()), Dict::getStatus, queryDTO.getStatus());

        queryWrapper.orderByAsc(Dict::getSortOrder);

        List<Dict> list = dictRepo.list(queryWrapper);

        Map<Long, DictVO> dictMap = list.stream().filter(dict -> dict.getParentId() == 0)
                .map(dict -> {
                    DictVO dictVO = new DictVO();
                    BeanUtils.copyProperties(dict, dictVO);
                    dictVO.setChildren(new ArrayList<>());
                    return dictVO;
                })
                .collect(Collectors.toMap(DictVO::getId, Function.identity()));
        list.forEach(dict -> {
            DictVO parentDict = dictMap.get(dict.getParentId());
            if (parentDict != null) {
                DictVO dictVO = BeanCopyUtils.copyBean(dict, DictVO.class);
                parentDict.getChildren().add(dictVO);
            }
        });

        return new ArrayList<>(dictMap.values());
    }

    public Boolean removeDictById(Long id) {
        return dictRepo.removeById(id);
    }

    public Dict getDictByCode(String dictCode) {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dict::getDictCode, dictCode);
        queryWrapper.eq(Dict::getStatus, StatusEnum.ACTIVE.getValue());
        return dictRepo.getOne(queryWrapper);
    }
}

