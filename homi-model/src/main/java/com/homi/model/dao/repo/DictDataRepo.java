package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.DictData;
import com.homi.model.dao.mapper.DictDataMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 字典数据表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class DictDataRepo extends ServiceImpl<DictDataMapper, DictData> {

    /**
     * 根据字典值列表获取字典数据列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/21 22:05
     *
     * @param values 参数说明
     * @return java.util.List<com.homi.model.dao.entity.DictData>
     */
    public List<DictData> getDictDataListByCodes(List<String> values) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DictData::getValue, values);

        return list(queryWrapper);
    }
}
