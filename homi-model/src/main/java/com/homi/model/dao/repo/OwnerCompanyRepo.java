package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.OwnerCompany;
import com.homi.model.dao.mapper.OwnerCompanyMapper;
import org.springframework.stereotype.Service;

@Service
public class OwnerCompanyRepo extends ServiceImpl<OwnerCompanyMapper, OwnerCompany> {
}
