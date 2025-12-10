package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.model.dto.role.RoleQueryDTO;
import com.homi.model.vo.role.RoleVO;
import com.homi.model.dao.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 角色信息表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    IPage<RoleVO> selectRolePage(IPage<RoleVO> page, @Param("query") RoleQueryDTO query);
}
