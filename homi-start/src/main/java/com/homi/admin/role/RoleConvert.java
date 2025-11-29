package com.homi.admin.role;

import com.homi.admin.auth.vo.account.AccountRoleVO;
import com.homi.domain.vo.role.RoleSimpleVO;
import com.homi.model.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);

    List<RoleSimpleVO> convertSimpleList(List<Role> roleList);

    List<AccountRoleVO> convertAccountRoleList(List<Role> roleList);
}
