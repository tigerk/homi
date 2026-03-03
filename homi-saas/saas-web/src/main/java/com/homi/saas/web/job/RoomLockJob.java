package com.homi.saas.web.job;

import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class RoomLockJob {
    private final RoomService roomService;

    /**
     * 自动解锁到期的临时锁房（lockReason=2）
     * 每60分钟执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void unlockExpiredTimedLocksTask() {
        Integer unlockedCount = roomService.unlockExpiredTimedLocks();
        if (unlockedCount > 0) {
            log.info("自动解锁到期锁房成功，数量={}", unlockedCount);
        }
    }
}

