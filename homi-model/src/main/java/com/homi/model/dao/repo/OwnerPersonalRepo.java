package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerPersonal;
import com.homi.model.dao.mapper.OwnerPersonalMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerPersonalRepo extends ServiceImpl<OwnerPersonalMapper, OwnerPersonal> {
}
