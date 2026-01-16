package com.homi.saas.web.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.enums.booking.BookingStatusEnum;
import com.homi.model.booking.dto.BookingCancelDTO;
import com.homi.model.dao.entity.Booking;
import com.homi.model.dao.repo.BookingRepo;
import com.homi.service.service.booking.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/20
 */

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class BookingJob {
    private final BookingService bookingService;
    private final BookingRepo bookingRepo;

    /**
     * 自动取消过期未签合同
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/1/9 10:47
     */
    // 5分钟执行一次
    @Scheduled(cron = "0 */5 * * * ?")
    public void bookingAutoCancelTask() {
        DateTime now = DateUtil.date();
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getBookingStatus, BookingStatusEnum.BOOKING.getCode());
        queryWrapper.le(Booking::getExpiryTime, now);
        List<Booking> bookings = bookingRepo.list(queryWrapper);
        bookings.forEach(booking -> {
            try {
                BookingCancelDTO query = new BookingCancelDTO();
                query.setId(booking.getId());
                query.setUpdateBy(booking.getSalesmanId());
                query.setCancelReason("[系统] 预定到期未签合同，自动设为过期");

                bookingService.cancelBooking(query);
                log.info("自动取消预约: booking={}", booking);
            } catch (Exception e) {
                log.error("自动取消预约失败: booking={}", booking, e);
            }
        });
    }
}
