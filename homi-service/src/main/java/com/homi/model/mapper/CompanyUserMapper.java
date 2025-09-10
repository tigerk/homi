package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.CompanyUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 公司用户表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-09-10
 */
@Mapper
public interface CompanyUserMapper extends BaseMapper<CompanyUser> {

}
