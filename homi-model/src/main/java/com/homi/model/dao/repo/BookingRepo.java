package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.booking.dto.BookingQueryDTO;
import com.homi.model.dao.entity.Booking;
import com.homi.model.dao.mapper.BookingMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <p>
 * 预定/定金表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-01-09
 */
@Service
public class BookingRepo extends ServiceImpl<BookingMapper, Booking> {
    /**
     * 查询租客预定列表
     *
     * @param query 查询参数
     * @return 租客预定列表
     */
    public Page<Booking> queryBookingList(BookingQueryDTO query) {
        Page<Booking> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNoneBlank(query.getTenantName())) {
            wrapper.eq(Booking::getTenantName, query.getTenantName());
        }

        if (StringUtils.isNoneBlank(query.getTenantPhone())) {
            wrapper.eq(Booking::getTenantPhone, query.getTenantPhone());
        }

        if (Objects.nonNull(query.getBookingStatus())) {
            wrapper.eq(Booking::getBookingStatus, query.getBookingStatus());
        }

        wrapper.orderByDesc(Booking::getId);

        return getBaseMapper().selectPage(page, wrapper);

    }
}
