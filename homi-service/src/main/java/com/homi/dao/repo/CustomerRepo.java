package com.homi.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.dao.entity.Customer;
import com.homi.dao.mapper.CustomerMapper;
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
