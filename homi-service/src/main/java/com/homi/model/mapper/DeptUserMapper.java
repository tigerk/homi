package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.DeptUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 部门和用户关联表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-06-30
 */
@Mapper
public interface DeptUserMapper extends BaseMapper<DeptUser> {

}
