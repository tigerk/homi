package com.homi.model.repo;

import com.homi.model.entity.Customer;
import com.homi.model.mapper.CustomerMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 客户表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Service
public class CustomerRepo extends ServiceImpl<CustomerMapper, Customer> {

}
