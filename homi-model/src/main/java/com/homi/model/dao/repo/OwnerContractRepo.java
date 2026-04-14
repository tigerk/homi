package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerContract;
import com.homi.model.dao.mapper.OwnerContractMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class OwnerContractRepo extends ServiceImpl<OwnerContractMapper, OwnerContract> {

    /**
     * 根据合作模式code获取合同ID列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/4/14 16:13
     *
     * @param cooperationModeCode 合作模式code
     * @return java.util.List<java.lang.Long>
     */
    public List<Long> getContractIdsByCooperationMode(String cooperationModeCode) {
        if (cooperationModeCode == null) {
            return null;
        }
        return list(new LambdaQueryWrapper<OwnerContract>()
            .eq(OwnerContract::getCooperationMode, cooperationModeCode)
            .select(OwnerContract::getId))
            .stream()
            .map(OwnerContract::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }
}
