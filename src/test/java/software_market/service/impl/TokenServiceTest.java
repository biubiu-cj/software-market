package software_market.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software_market.config.security.JwtProvider;
import software_market.dto.response.TokenPair;
import software_market.entity.RefreshToken;
import software_market.entity.User;
import software_market.exception.BizException;
import software_market.mapper.RefreshTokenMapper;
import software_market.mapper.UserMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("software-market-unit-test-secret-key-0123456789".getBytes(StandardCharsets.UTF_8));

    private final JwtProvider jwtProvider = new JwtProvider(SECRET, "7d", "30d", "1h");
    private final RefreshTokenMapper refreshTokenMapper = mock(RefreshTokenMapper.class);
    private final RefreshTokenService refreshTokenService = new RefreshTokenService(refreshTokenMapper);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final TokenService tokenService =
            new TokenService(jwtProvider, refreshTokenMapper, refreshTokenService, userMapper);

    @Test
    void issuingTokenPairProducesValidAccessAndRefreshTokens() {
        User user = user(7);

        TokenPair pair = tokenService.issueTokenPair(user);

        assertEquals(7, jwtProvider.getUserId(jwtProvider.parseAccessToken(pair.getToken())));
        assertEquals(7, jwtProvider.getUserId(jwtProvider.parseRefreshToken(pair.getRefreshToken())));

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenMapper).insert(captor.capture());
        RefreshToken saved = captor.getValue();
        assertEquals(7, saved.getUserId());
        assertEquals(pair.getRefreshToken(), saved.getToken());
        assertFalse(saved.getIsUsed());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void refreshConsumesOldTokenAndIssuesNewPair() {
        User user = user(7);
        String oldRefresh = jwtProvider.generateRefreshToken("7");
        when(refreshTokenMapper.selectByToken(oldRefresh))
                .thenReturn(refreshRecord(oldRefresh, false, LocalDateTime.now().plusDays(1)));
        when(refreshTokenMapper.markUsedIfUnused(oldRefresh)).thenReturn(1);
        when(userMapper.selectById(7)).thenReturn(user);

        TokenPair pair = tokenService.refresh(oldRefresh);

        assertNotNull(pair.getToken());
        assertEquals(7, jwtProvider.getUserId(jwtProvider.parseAccessToken(pair.getToken())));
        verify(refreshTokenMapper).markUsedIfUnused(oldRefresh);
    }

    @Test
    void refreshFailsWhenOldTokenAlreadyUsed() {
        String oldRefresh = jwtProvider.generateRefreshToken("7");
        when(refreshTokenMapper.selectByToken(oldRefresh))
                .thenReturn(refreshRecord(oldRefresh, true, LocalDateTime.now().plusDays(1)));

        assertThrows(BizException.class, () -> tokenService.refresh(oldRefresh));
    }

    @Test
    void refreshFailsWhenUserNoLongerExists() {
        String oldRefresh = jwtProvider.generateRefreshToken("7");
        when(refreshTokenMapper.selectByToken(oldRefresh))
                .thenReturn(refreshRecord(oldRefresh, false, LocalDateTime.now().plusDays(1)));
        when(refreshTokenMapper.markUsedIfUnused(oldRefresh)).thenReturn(1);
        when(userMapper.selectById(7)).thenReturn(null);

        assertThrows(BizException.class, () -> tokenService.refresh(oldRefresh));
    }

    @Test
    void refreshRejectsAccessToken() {
        String accessToken = jwtProvider.generateAccessToken("7");

        assertThrows(Exception.class, () -> tokenService.refresh(accessToken));
    }

    private User user(int id) {
        User user = new User();
        user.setUserId(id);
        user.setEmail("user" + id + "@example.com");
        user.setIdentity("student");
        user.setDeleted(false);
        return user;
    }

    private RefreshToken refreshRecord(String token, boolean used, LocalDateTime expiresAt) {
        RefreshToken record = new RefreshToken();
        record.setUserId(7);
        record.setToken(token);
        record.setIsUsed(used);
        record.setExpiresAt(expiresAt);
        return record;
    }
}
