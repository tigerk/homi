package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dashboard.vo.WelcomeOverdueBucketVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 租客账单表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Mapper
public interface LeaseBillMapper extends BaseMapper<LeaseBill> {
    List<WelcomeOverdueBucketVO> selectWelcomeOverdueBuckets();
}
