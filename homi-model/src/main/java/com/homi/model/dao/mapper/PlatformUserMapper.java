package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.model.dao.entity.PlatformUser;
import com.homi.model.dto.user.UserQueryDTO;
import com.homi.model.platform.vo.PlatformUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Mapper
public interface PlatformUserMapper extends BaseMapper<PlatformUser> {

    /**
     * 自定义分页查询
     *
     * @param page  分页对象
     * @param query 查询对象
     * @return 查询结果
     */
    IPage<PlatformUserVO> selectUserList(IPage<PlatformUserVO> page, @Param("query") UserQueryDTO query);
}
