package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.Owner;
import com.homi.model.dao.mapper.OwnerMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerRepo extends ServiceImpl<OwnerMapper, Owner> {
}
