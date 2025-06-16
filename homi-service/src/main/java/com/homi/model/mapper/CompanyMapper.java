package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.Company;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 公司表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-06-12
 */
@Mapper
public interface CompanyMapper extends BaseMapper<Company> {

}
