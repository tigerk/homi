package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.House;
import com.homi.model.mapper.HouseMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 房源表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Service
public class HouseRepo extends ServiceImpl<HouseMapper, House> {

}
