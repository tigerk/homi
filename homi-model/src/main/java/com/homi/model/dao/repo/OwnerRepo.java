package com.homi.model.dao.repo;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.Owner;
import com.homi.model.dao.mapper.OwnerMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerRepo extends ServiceImpl<OwnerMapper, Owner> {
    /**
     * 根据业主名称获取业主ID列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/4/14 16:29
     *
     * @param ownerName 参数说明
     * @return java.util.List<java.lang.Long>
     */
    public List<Long> getOwnerIdsByOwnerName(String ownerName) {
        if (StrUtil.isBlank(ownerName)) {
            return null;
        }
        return list(new LambdaQueryWrapper<Owner>().like(Owner::getOwnerName, ownerName)).stream().map(Owner::getId).toList();
    }
}
