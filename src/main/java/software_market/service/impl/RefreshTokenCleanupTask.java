package software_market.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software_market.mapper.RefreshTokenMapper;

import java.time.LocalDateTime;

/**
 * Refresh Token 定期清理。
 *
 * 每次登录、每次刷新都会向 refresh_token 表插入一条记录，旧记录只标记
 * is_used=1 而不删除；用户反复登录/退出后表会持续膨胀。本任务每天凌晨清理：
 * 1. 已过期记录（无论是否使用过）；
 * 2. 已使用且签发超过保留期的记录（保留一段时间便于排查重放）。
 *
 * 未使用且未过期的记录代表其他设备的活跃会话，不能删除。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupTask {

    /** 已使用 Token 的保留天数，用于保留重放排查窗口 */
    private static final int USED_TOKEN_RETENTION_DAYS = 7;

    private final RefreshTokenMapper refreshTokenMapper;

    @Scheduled(cron = "0 30 3 * * *")
    public void cleanup() {
        LocalDateTime usedBefore = LocalDateTime.now().minusDays(USED_TOKEN_RETENTION_DAYS);
        int deleted = refreshTokenMapper.deleteExpiredOrUsedBefore(usedBefore);
        if (deleted > 0) {
            log.info("Refresh Token 清理完成，删除 {} 条过期/已使用记录", deleted);
        }
    }
}