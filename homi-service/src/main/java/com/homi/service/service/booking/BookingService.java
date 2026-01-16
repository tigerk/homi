package com.homi.service.service.booking;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.booking.BookingStatusEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.booking.dto.BookingCancelDTO;
import com.homi.model.booking.dto.BookingCreateDTO;
import com.homi.model.booking.dto.BookingQueryDTO;
import com.homi.model.booking.vo.BookingListVO;
import com.homi.model.booking.vo.BookingTotalItemVO;
import com.homi.model.dao.entity.Booking;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.repo.BookingRepo;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.model.room.vo.LeaseInfoVO;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 租客预定
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepo bookingRepo;
    private final RoomRepo roomRepo;

    private final RoomService roomService;
    private final UserRepo userRepo;

    /**
     * 获取租客预定列表
     *
     * @param query 查询参数
     * @return 租客预定列表
     */
    public PageVO<BookingListVO> getBookingList(BookingQueryDTO query) {
        Page<Booking> bookingList = bookingRepo.queryBookingList(query);

        PageVO<BookingListVO> pageResult = new PageVO<>();
        pageResult.setTotal(bookingList.getTotal());
        pageResult.setList(bookingList.getRecords().stream()
            .map(booking -> {
                BookingListVO vo = BeanCopyUtils.copyBean(booking, BookingListVO.class);
                assert vo != null;
                vo.setBookingStatusName(Objects.requireNonNull(BookingStatusEnum.getEnum(booking.getBookingStatus())).getName());
                vo.setRoomList(roomService.getRoomListByRoomIds(JSONUtil.toList(booking.getRoomIds(), Long.class)));

                vo.setRoomIds(JSONUtil.toList(booking.getRoomIds(), Long.class));
                return vo;
            })
            .toList());
        pageResult.setCurrentPage(bookingList.getCurrent());
        pageResult.setPageSize(bookingList.getSize());
        pageResult.setPages(bookingList.getPages());

        return pageResult;
    }

    /**
     * 获取租客预定详情
     *
     * @param id 租客预定ID
     * @return 租客预定详情
     */
    public BookingListVO getBookingDetail(Long id) {
        Booking booking = bookingRepo.getById(id);
        if (booking == null) {
            throw new BizException("预定记录不存在");
        }

        BookingListVO vo = BeanCopyUtils.copyBean(booking, BookingListVO.class);
        assert vo != null;
        vo.setBookingStatusName(Objects.requireNonNull(BookingStatusEnum.getEnum(booking.getBookingStatus())).getName());
        vo.setRoomIds(JSONUtil.toList(booking.getRoomIds(), Long.class));
        vo.setRoomList(roomService.getRoomListByRoomIds(JSONUtil.toList(booking.getRoomIds(), Long.class)));

        User userRepoById = userRepo.getById(vo.getSalesmanId());
        vo.setSalesmanName(userRepoById.getNickname());

        return vo;
    }

    /**
     * 添加租客预定
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/1/9 10:47
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addBooking(BookingCreateDTO createDTO) {
        Booking booking = BeanCopyUtils.copyBean(createDTO, Booking.class);
        assert booking != null;
        booking.setBookingStatus(BookingStatusEnum.BOOKING.getCode());
        booking.setRoomIds(JSONUtil.toJsonStr(createDTO.getRoomIds()));

        // 把房间修改为已出租状态，但是没有租客信息；
        Boolean updateRoomStatusBatch = roomRepo.updateRoomStatusBatch(createDTO.getRoomIds(), RoomStatusEnum.BOOKED.getCode());
        if (Boolean.FALSE.equals(updateRoomStatusBatch)) {
            log.error("修改房间为预定状态失败，roomIds: {}", createDTO.getRoomIds());
        }

        bookingRepo.saveOrUpdate(booking);
        return booking.getId();
    }

    /**
     * 更新租客预定
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/1/9 10:47
     */
    @Transactional(rollbackFor = Exception.class)
    public Long updateBooking(BookingCreateDTO createDTO) {
        Booking booking = bookingRepo.getById(createDTO.getId());
        if (booking == null) {
            throw new BizException("预定记录不存在");
        }

        List<Long> oldIds = JSONUtil.toList(booking.getRoomIds(), Long.class);
        List<Long> newIds = createDTO.getRoomIds();

        // 1. 识别变化的房间
        // 那些原来在预定里，现在被剔除的房间（需变回 AVAILABLE）
        List<Long> toRelease = oldIds.stream().filter(id -> !newIds.contains(id)).collect(Collectors.toList());
        // 那些新加入预定的房间（需变为 BOOKED）
        List<Long> toBook = newIds.stream().filter(id -> !oldIds.contains(id)).collect(Collectors.toList());

        // 2. 批量合并更新（减少网络 IO）
        if (!toRelease.isEmpty() || !toBook.isEmpty()) {
            // 调用一个合并更新的方法
            roomRepo.batchUpdateRoomStatusMixed(toRelease, toBook);
        }

        // 3. 更新预定单信息
        BeanUtils.copyProperties(createDTO, booking);
        booking.setRoomIds(JSONUtil.toJsonStr(newIds));
        bookingRepo.saveOrUpdate(booking);

        return booking.getId();
    }

    public List<BookingTotalItemVO> getBookingStatusTotal(BookingQueryDTO query) {
        @NotNull Map<Integer, BookingTotalItemVO> result = initBookingStatusMap();

        List<BookingTotalItemVO> statusTotal = bookingRepo.getBaseMapper().getStatusTotal(query);
        statusTotal.forEach(tenantTotalItemVO -> {
            BookingTotalItemVO orDefault = result.getOrDefault(tenantTotalItemVO.getStatus(), tenantTotalItemVO);
            orDefault.setTotal(tenantTotalItemVO.getTotal());
        });

        return result.values().stream().toList().stream().sorted(Comparator.comparingInt(BookingTotalItemVO::getSortOrder)).collect(Collectors.toList());
    }

    /**
     * 获取房间状态枚举映射
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/7 19:12
     *
     * @return java.util.Map<java.lang.Integer, com.homi.domain.vo.room.RoomTotalItemVO>
     */
    private @NotNull Map<Integer, BookingTotalItemVO> initBookingStatusMap() {
        Map<Integer, BookingTotalItemVO> result = new HashMap<>();
        BookingStatusEnum[] values = BookingStatusEnum.values();
        for (BookingStatusEnum contractStatusEnum : values) {
            BookingTotalItemVO bookingTotalItemVO = new BookingTotalItemVO();
            bookingTotalItemVO.setStatus(contractStatusEnum.getCode());
            bookingTotalItemVO.setStatusName(contractStatusEnum.getName());
            bookingTotalItemVO.setSortOrder(contractStatusEnum.getSortOrder());
            bookingTotalItemVO.setTotal(0);
            result.put(contractStatusEnum.getCode(), bookingTotalItemVO);
        }
        return result;
    }

    /**
     * 取消租客预定
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/1/9 10:47
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelBooking(BookingCancelDTO query) {
        Booking booking = bookingRepo.getById(query.getId());
        assert booking != null;
        booking.setBookingStatus(BookingStatusEnum.CANCELLED_EXPIRED.getCode());
        booking.setUpdateBy(query.getUpdateBy());
        booking.setCancelReason(query.getCancelReason());
        booking.setCancelTime(DateUtil.date());

        // 把房间修改为已出租状态，但是没有租客信息；
        Boolean updateRoomStatusBatch = roomRepo.updateRoomStatusBatch(JSONUtil.toList(booking.getRoomIds(), Long.class), RoomStatusEnum.AVAILABLE.getCode());
        if (Boolean.FALSE.equals(updateRoomStatusBatch)) {
            log.error("释放预定房间失败，roomIds: {}", JSONUtil.toList(booking.getRoomIds(), Long.class));
        }

        return bookingRepo.saveOrUpdate(booking);
    }
}
