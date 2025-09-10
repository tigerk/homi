package com.homi.model.repo;

import com.homi.model.entity.CompanyUser;
import com.homi.model.mapper.CompanyUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 公司用户表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-09-10
 */
@Service
public class CompanyUserRepo extends ServiceImpl<CompanyUserMapper, CompanyUser> {

}
