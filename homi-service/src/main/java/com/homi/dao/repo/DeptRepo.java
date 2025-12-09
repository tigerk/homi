package com.homi.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.dao.entity.Dept;
import com.homi.dao.mapper.DeptMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class DeptRepo extends ServiceImpl<DeptMapper, Dept> {

}
