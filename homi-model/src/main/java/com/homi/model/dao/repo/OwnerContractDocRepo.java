package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerContractDoc;
import com.homi.model.dao.mapper.OwnerContractDocMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerContractDocRepo extends ServiceImpl<OwnerContractDocMapper, OwnerContractDoc> {

    public List<OwnerContractDoc> listByOwnerContractId(Long ownerContractId) {
        return list(new LambdaQueryWrapper<OwnerContractDoc>()
            .eq(OwnerContractDoc::getOwnerContractId, ownerContractId)
            .orderByDesc(OwnerContractDoc::getCreateAt)
            .orderByDesc(OwnerContractDoc::getId));
    }
}
