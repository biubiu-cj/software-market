package software_market.config.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtProviderTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("software-market-unit-test-secret-key-0123456789".getBytes(StandardCharsets.UTF_8));

    private final JwtProvider jwtProvider = new JwtProvider(SECRET, "7d", "30d", "1h");

    @Test
    void accessTokenContainsUserIdAndType() {
        String token = jwtProvider.generateAccessToken("42");

        Claims claims = jwtProvider.parseAccessToken(token);

        assertEquals("42", claims.get("userId", String.class));
        assertEquals(JwtProvider.TOKEN_TYPE_ACCESS, claims.get("tokenType", String.class));
        assertEquals(JwtProvider.ISSUER, claims.getIssuer());
        assertEquals(JwtProvider.SUBJECT_ACCESS, claims.getSubject());
        assertEquals(42, jwtProvider.getUserId(claims));
    }

    @Test
    void refreshTokenCannotBeUsedAsAccessToken() {
        String refreshToken = jwtProvider.generateRefreshToken("42");

        assertThrows(Exception.class, () -> jwtProvider.parseAccessToken(refreshToken));
    }

    @Test
    void accessTokenCannotBeUsedAsRefreshToken() {
        String accessToken = jwtProvider.generateAccessToken("42");

        assertThrows(Exception.class, () -> jwtProvider.parseRefreshToken(accessToken));
    }

    @Test
    void oauth2TokenCannotBeUsedAsAccessToken() {
        String oauth2Token = jwtProvider.generateOAuth2AccessToken(42, "app", "read_basic");

        assertThrows(Exception.class, () -> jwtProvider.parseAccessToken(oauth2Token));
        assertEquals(42, jwtProvider.getUserId(jwtProvider.parseOAuth2AccessToken(oauth2Token)));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtProvider expiredProvider = new JwtProvider(SECRET, "-1s", "30d", "1h");
        String token = expiredProvider.generateAccessToken("42");

        assertThrows(Exception.class, () -> jwtProvider.parseAccessToken(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtProvider.generateAccessToken("42");
        String tampered = token.substring(0, token.length() - 2)
                + (token.endsWith("AA") ? "BB" : "AA");

        assertThrows(Exception.class, () -> jwtProvider.parseAccessToken(tampered));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String otherSecret = Base64.getEncoder()
                .encodeToString("a-totally-different-secret-key-0123456789".getBytes(StandardCharsets.UTF_8));
        JwtProvider otherProvider = new JwtProvider(otherSecret, "7d", "30d", "1h");

        String token = otherProvider.generateAccessToken("42");

        assertThrows(Exception.class, () -> jwtProvider.parseAccessToken(token));
    }
}
