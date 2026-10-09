package software_market.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {
    private final SecretKey secretKey;
    private final long accessTokenExpireMs;
    private final long refreshTokenExpireMs;
    private final long oauth2AccessTokenExpireMs;
    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expire}") String accessTokenExpireMs,
            @Value("${jwt.refresh-token-expire}") String refreshTokenExpireMs,
            @Value("${jwt.oauth2-access-token-expire}") String oauth2AccessTokenExpireMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpireMs = parseDuration(accessTokenExpireMs);
        this.refreshTokenExpireMs = parseDuration(refreshTokenExpireMs);
        this.oauth2AccessTokenExpireMs = parseDuration(oauth2AccessTokenExpireMs);
    }

    /**
     * 生成Access Token 7天
     * @param userId
     * @return
     */
    public String generateAccessToken(String userId) {
        return Jwts.builder().
                claim("userId", userId).
                subject("user token").
                issuer("loginTest").
                issuedAt(new Date()).
                expiration(new Date(System.currentTimeMillis() + accessTokenExpireMs)).
                signWith(secretKey, Jwts.SIG.HS256).
                compact();

    }


    /**
     * 生成Refresh Token 30天
     * @param userId
     * @return
     */
    public String generateRefreshToken(String userId) {
        return Jwts.builder().
                claim("userId", userId).
                subject("refresh token").
                issuer("loginTest").
                issuedAt(new Date()).
                expiration(new Date(System.currentTimeMillis() + refreshTokenExpireMs)).
                signWith(secretKey, Jwts.SIG.HS256).
                compact();
    }

    /**
     * 解析Access Token
     * @param token
     * @return
     */

    public Claims parseAccessToken(String token) {
        return Jwts.parser().
                verifyWith(secretKey).
                requireSubject("user token")
                .requireIssuer("loginTest")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 解析Refresh Token
     * @param token
     * @return
     */
    public Claims parseRefreshToken(String token) {
        return Jwts.parser().
                verifyWith(secretKey).
                requireSubject("refresh token")
                .requireIssuer("loginTest")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取用户ID
     * @param token
     * @return
     */
    public Integer getUserId(String token){
        Claims claims = Jwts.parser().
                verifyWith(secretKey).
                build().
                parseSignedClaims(token).
                getPayload();
        return Integer.parseInt(claims.get("userId", String.class));
    }

    public String generateOAuth2AccessToken(Integer userId, String appId, String scope) {
        return Jwts.builder()
                .claim("userId", userId.toString())
                .claim("appId", appId)
                .claim("scope", scope)
                .subject("oauth2 access token")
                .issuer("loginTest")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + oauth2AccessTokenExpireMs))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseOAuth2AccessToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireSubject("oauth2 access token")
                .requireIssuer("loginTest")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     *
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }




    /**
     * 解析时间
     * @param duration
     * @return
     */

    public long parseDuration(String duration) {
        duration = duration.trim().toLowerCase();
        long multiplier;
        if (duration.endsWith("d")) {
            multiplier = 24L * 60 * 60 * 1000;
            duration = duration.substring(0, duration.length() - 1);
        } else if (duration.endsWith("h")) {
            multiplier = 60L * 60 * 1000;
            duration = duration.substring(0, duration.length() - 1);
        } else if (duration.endsWith("m")) {
            multiplier = 60L * 1000;
            duration = duration.substring(0, duration.length() - 1);
        } else if (duration.endsWith("s")) {
            multiplier = 1000L;
            duration = duration.substring(0, duration.length() - 1);
        } else {
            multiplier = 1L;
        }
        return Long.parseLong(duration) * multiplier;
    }

}
