package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.domain.dto.role.RoleQueryDTO;
import com.homi.domain.dto.role.SysRoleVO;
import com.homi.model.entity.SysRole;
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
public interface SysRoleMapper extends BaseMapper<SysRole> {

    IPage<SysRoleVO> selectRolePage(IPage<SysRoleVO> page, @Param("query") RoleQueryDTO query);
}
