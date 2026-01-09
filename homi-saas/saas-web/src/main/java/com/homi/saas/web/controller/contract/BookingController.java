package com.homi.saas.web.controller.contract;

import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.booking.dto.BookingCancelDTO;
import com.homi.model.booking.dto.BookingCreateDTO;
import com.homi.model.booking.dto.BookingIdDTO;
import com.homi.model.booking.dto.BookingQueryDTO;
import com.homi.model.booking.vo.BookingListVO;
import com.homi.model.booking.vo.BookingTotalItemVO;
import com.homi.model.booking.vo.BookingTotalVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.booking.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/saas/contract/booking")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/create")
    @Log(title = "创建/修改预定合同", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> createBooking(@RequestBody BookingCreateDTO createDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        createDTO.setCreateBy(loginUser.getId());
        createDTO.setCompanyId(loginUser.getCurCompanyId());

        if (Objects.nonNull(createDTO.getId())) {
            createDTO.setUpdateBy(loginUser.getId());
            return ResponseResult.ok(bookingService.updateBooking(createDTO));
        } else {
            return ResponseResult.ok(bookingService.addBooking(createDTO));
        }
    }

    @PostMapping("/total")
    public ResponseResult<BookingTotalVO> getTenantTotal(@RequestBody BookingQueryDTO query) {
        List<BookingTotalItemVO> tenantStatusTotal = bookingService.getBookingStatusTotal(query);

        BookingTotalVO bookingTotalVO = new BookingTotalVO();
        bookingTotalVO.setStatusList(tenantStatusTotal);

        return ResponseResult.ok(bookingTotalVO);
    }

    @PostMapping("/list")
    public ResponseResult<PageVO<BookingListVO>> getBookingList(@RequestBody BookingQueryDTO query) {
        return ResponseResult.ok(bookingService.getBookingList(query));
    }

    @PostMapping("/get")
    public ResponseResult<BookingListVO> getBookingDetail(@RequestBody BookingIdDTO dto) {
        return ResponseResult.ok(bookingService.getBookingDetail(dto.getBookingId()));
    }

    @PostMapping("/cancel")
    @Log(title = "取消预定合同", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Boolean> cancelBooking(@RequestBody BookingCancelDTO query, @AuthenticationPrincipal UserLoginVO loginUser) {
        query.setUpdateBy(loginUser.getId());
        return ResponseResult.ok(bookingService.cancelBooking(query));
    }
}
