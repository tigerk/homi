package com.homi.model.dao.repo;

import com.homi.model.dao.entity.CompanyOrder;
import com.homi.model.dao.mapper.CompanyOrderMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 企业购买订单表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-03-04
 */
@Service
public class CompanyOrderRepo extends ServiceImpl<CompanyOrderMapper, CompanyOrder> {

}
