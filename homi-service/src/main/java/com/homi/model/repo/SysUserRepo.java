package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.SysUser;
import com.homi.model.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Service
public class SysUserRepo extends ServiceImpl<SysUserMapper, SysUser> {

}
