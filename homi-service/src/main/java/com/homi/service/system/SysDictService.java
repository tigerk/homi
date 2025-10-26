package com.homi.service.system;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.dto.dict.DictQueryDTO;
import com.homi.domain.dto.dict.DictWithDataVO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.vo.dict.SysDictVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysDict;
import com.homi.model.mapper.SysDictMapper;
import com.homi.model.repo.SysDictRepo;
import com.homi.utils.BeanCopyUtils;
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
 * 字典表(SysDict)表服务实现类
 *
 * @author sjh
 * @since 2024-04-24 10:35:55
 */
@Service
@RequiredArgsConstructor
public class SysDictService {

    private final SysDictMapper sysDictMapper;

    private final SysDictRepo sysDictRepo;

    /**
     * 创建字典
     *
     * @param sysDict 字典对象
     * @return 字典ID
     */
    public Long createDict(SysDict sysDict) {
        validateDictUniqueness(null, sysDict.getDictName(), sysDict.getDictCode());
        sysDict.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        sysDictMapper.insert(sysDict);
        return sysDict.getId();
    }

    /**
     * 更新字典
     *
     * @param sysDict 字典对象
     * @return 字典ID
     */

    public Long updateDict(SysDict sysDict) {
        SysDict exists = sysDictMapper.selectById(sysDict.getId());
        if (exists == null) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "修改的字典不存在");
        }
        validateDictUniqueness(sysDict.getId(), sysDict.getDictName(), sysDict.getDictCode());
        sysDict.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        sysDictMapper.updateById(sysDict);
        return sysDict.getId();
    }


    public List<DictWithDataVO> listAllDictAndData() {
        return sysDictMapper.listAllDictWithData();
    }

    /**
     * 校验字典名称和编码的唯一性
     *
     * @param id   字典ID，用于排除自身
     * @param name 字典名称
     * @param code 字典编码
     */
    private void validateDictUniqueness(Long id, String name, String code) {
        SysDict sysDictName = sysDictMapper.selectOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictName, name));
        if (sysDictName != null && !sysDictName.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "字典名称不能重复");
        }
        SysDict sysDictCode = sysDictMapper.selectOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode, code));
        if (sysDictCode != null && !sysDictCode.getId().equals(id)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "字典编码不能重复");
        }
    }


    public List<SysDictVO> list(DictQueryDTO queryDTO) {
        LambdaQueryWrapper<SysDict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(CharSequenceUtil.isNotEmpty(queryDTO.getDictName()), SysDict::getDictName, queryDTO.getDictName())
                .like(CharSequenceUtil.isNotEmpty(queryDTO.getDictCode()), SysDict::getDictCode, queryDTO.getDictCode())
                .eq(SysDict::getHidden, Boolean.FALSE)
                .eq(Objects.nonNull(queryDTO.getStatus()), SysDict::getStatus, queryDTO.getStatus());

        queryWrapper.orderByAsc(SysDict::getSort);

        List<SysDict> list = sysDictRepo.list(queryWrapper);

        Map<Long, SysDictVO> dictMap = list.stream().filter(dict -> dict.getParentId() == 0)
                .map(dict -> {
                    SysDictVO sysDictVO = new SysDictVO();
                    BeanUtils.copyProperties(dict, sysDictVO);
                    sysDictVO.setChildren(new ArrayList<>());
                    return sysDictVO;
                })
                .collect(Collectors.toMap(SysDictVO::getId, Function.identity()));
        list.forEach(dict -> {
            SysDictVO parentDict = dictMap.get(dict.getParentId());
            if (parentDict != null) {
                SysDictVO sysDictVO = BeanCopyUtils.copyBean(dict, SysDictVO.class);
                parentDict.getChildren().add(sysDictVO);
            }
        });

        return new ArrayList<>(dictMap.values());
    }

    public Boolean removeDictById(Long id) {
        return sysDictRepo.removeById(id);
    }

    public SysDict getDictByCode(String dictCode) {
        LambdaQueryWrapper<SysDict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDict::getDictCode, dictCode);
        queryWrapper.eq(SysDict::getStatus, StatusEnum.ACTIVE.getValue());
        return sysDictRepo.getOne(queryWrapper);
    }
}

