package software_market.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import software_market.entity.RefreshToken;
import software_market.exception.BizException;
import software_market.mapper.RefreshTokenMapper;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Refresh Token 的一次性消费。
 *
 * 消费必须在独立事务中提交（REQUIRES_NEW）：即使后续签发新令牌失败回滚，
 * 旧 Refresh Token 也已经作废，避免被重放。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;

    /**
     * 校验并原子消费 Refresh Token。
     *
     * @param token          原始 refresh token 字符串
     * @param expectedUserId 从 JWT 中解析出的 userId，必须与数据库记录一致
     * @throws BizException 记录不存在 / 已使用 / 已过期 / userId 不一致 / 并发已消费
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void consume(String token, Integer expectedUserId) {
        RefreshToken record = refreshTokenMapper.selectByToken(token);
        if (record == null) {
            throw new BizException("refresh token无效");
        }
        if (Boolean.TRUE.equals(record.getIsUsed())) {
            throw new BizException("refresh token已使用");
        }
        if (record.getExpiresAt() != null && LocalDateTime.now().isAfter(record.getExpiresAt())) {
            throw new BizException("refresh token已过期");
        }
        if (!Objects.equals(record.getUserId(), expectedUserId)) {
            throw new BizException("refresh token无效");
        }

        // 条件更新是并发控制的关键：只有把 is_used 从 0 改成 1 的那一个请求能成功
        int updated = refreshTokenMapper.markUsedIfUnused(token);
        if (updated == 0) {
            log.warn("Refresh Token 并发重复使用被拒绝, userId={}", expectedUserId);
            throw new BizException("refresh token已使用");
        }
    }

    public void revokeAllForUser(Integer userId) {
        refreshTokenMapper.deleteByUserId(userId);
    }
}
