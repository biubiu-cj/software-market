package software_market.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software_market.config.security.JwtProvider;
import software_market.dto.response.TokenPair;
import software_market.entity.RefreshToken;
import software_market.entity.User;
import software_market.exception.BizException;
import software_market.mapper.RefreshTokenMapper;
import software_market.mapper.UserMapper;

import java.time.LocalDateTime;

/**
 * 令牌签发与刷新。
 *
 * 登录 / 注册 / 微信登录 / 刷新 都走这里，保证 Refresh Token 一定入库、
 * 刷新时一定先原子消费旧令牌再签发新令牌。
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final int REFRESH_TOKEN_EXPIRE_DAYS = 30;

    private final JwtProvider jwtProvider;
    private final RefreshTokenMapper refreshTokenMapper;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    public TokenPair issueTokenPair(User user) {
        Integer userId = user.getUserId();
        String accessToken = jwtProvider.generateAccessToken(userId.toString());
        String refreshToken = jwtProvider.generateRefreshToken(userId.toString());

        RefreshToken record = new RefreshToken();
        record.setUserId(userId);
        record.setToken(refreshToken);
        record.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS));
        record.setIsUsed(false);
        record.setCreatedAt(LocalDateTime.now());
        refreshTokenMapper.insert(record);

        return new TokenPair(accessToken, refreshToken);
    }

    public String issueAccessToken(User user) {
        return jwtProvider.generateAccessToken(user.getUserId().toString());
    }

    /**
     * 刷新流程：
     * 1. 完整校验 Refresh Token（签名/过期/issuer/subject/tokenType）
     * 2. 取出 userId
     * 3. 原子消费旧令牌（一次性，并发下最多一个成功）
     * 4. 校验用户仍然存在后签发新令牌
     */
    public TokenPair refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BizException("refresh token不能为空");
        }

        Claims claims;
        try {
            claims = jwtProvider.parseRefreshToken(rawRefreshToken);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new BizException("refresh token无效或已过期");
        }
        Integer userId = jwtProvider.getUserId(claims);

        refreshTokenService.consume(rawRefreshToken, userId);

        User user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException("用户不存在");
        }
        return issueTokenPair(user);
    }

    public void revokeAllForUser(Integer userId) {
        refreshTokenService.revokeAllForUser(userId);
    }
}
