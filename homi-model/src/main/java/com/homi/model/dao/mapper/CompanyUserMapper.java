package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.model.dto.user.UserQueryDTO;
import com.homi.model.vo.company.user.UserVO;
import com.homi.model.dao.entity.CompanyUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    /**
     * 自定义分页查询
     *
     * @param page  分页对象
     * @param query 查询对象
     * @return 查询结果
     */
    IPage<UserVO> selectUserList(IPage<UserVO> page, @Param("query") UserQueryDTO query);
}
