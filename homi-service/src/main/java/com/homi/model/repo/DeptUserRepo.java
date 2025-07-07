package com.homi.model.repo;

import com.homi.model.entity.DeptUser;
import com.homi.model.mapper.DeptUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 部门和用户关联表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class DeptUserRepo extends ServiceImpl<DeptUserMapper, DeptUser> {

}
