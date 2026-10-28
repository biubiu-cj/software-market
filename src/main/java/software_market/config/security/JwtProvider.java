package software_market.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 签发与校验。
 *
 * 三类令牌共用同一密钥，但通过 subject + tokenType 双重区分，
 * 任何一类令牌都不能被当作另一类使用（例如 Refresh Token 不能访问受保护接口）。
 */
@Component
public class JwtProvider {

    public static final String ISSUER = "loginTest";

    public static final String SUBJECT_ACCESS = "user token";
    public static final String SUBJECT_REFRESH = "refresh token";
    public static final String SUBJECT_OAUTH2 = "oauth2 access token";

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    public static final String TOKEN_TYPE_OAUTH2 = "oauth2";

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
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("jwt.secret 未配置");
        }
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpireMs = parseDuration(accessTokenExpireMs);
        this.refreshTokenExpireMs = parseDuration(refreshTokenExpireMs);
        this.oauth2AccessTokenExpireMs = parseDuration(oauth2AccessTokenExpireMs);
    }

    /**
     * 生成 Access Token（默认 7 天）
     */
    public String generateAccessToken(String userId) {
        return buildToken(userId, SUBJECT_ACCESS, TOKEN_TYPE_ACCESS, accessTokenExpireMs);
    }

    /**
     * 生成 Refresh Token（默认 30 天）
     */
    public String generateRefreshToken(String userId) {
        return buildToken(userId, SUBJECT_REFRESH, TOKEN_TYPE_REFRESH, refreshTokenExpireMs);
    }

    /**
     * 生成 OAuth2 授权访问令牌（默认 1 小时），供第三方应用调用 /api/auth/connect/** 使用
     */
    public String generateOAuth2AccessToken(Integer userId, String appId, String scope) {
        return Jwts.builder()
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_OAUTH2)
                .claim("appId", appId)
                .claim("scope", scope)
                .subject(SUBJECT_OAUTH2)
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + oauth2AccessTokenExpireMs))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private String buildToken(String userId, String subject, String tokenType, long expireMs) {
        return Jwts.builder()
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .subject(subject)
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析并完整校验 Access Token：签名、过期时间、issuer、subject、tokenType。
     */
    public Claims parseAccessToken(String token) {
        return parse(token, SUBJECT_ACCESS, TOKEN_TYPE_ACCESS);
    }

    /**
     * 解析并完整校验 Refresh Token：签名、过期时间、issuer、subject、tokenType。
     */
    public Claims parseRefreshToken(String token) {
        return parse(token, SUBJECT_REFRESH, TOKEN_TYPE_REFRESH);
    }

    /**
     * 解析并完整校验 OAuth2 访问令牌。
     */
    public Claims parseOAuth2AccessToken(String token) {
        return parse(token, SUBJECT_OAUTH2, TOKEN_TYPE_OAUTH2);
    }

    private Claims parse(String token, String expectedSubject, String expectedTokenType) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(ISSUER)
                .requireSubject(expectedSubject)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
        if (!expectedTokenType.equals(tokenType)) {
            throw new JwtException("token 类型不匹配");
        }
        return claims;
    }

    /**
     * 从已校验的 Claims 中读取用户 ID
     */
    public Integer getUserId(Claims claims) {
        String raw = claims.get(CLAIM_USER_ID, String.class);
        if (!StringUtils.hasText(raw)) {
            throw new JwtException("token 缺少 userId");
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new JwtException("token 中 userId 非法");
        }
    }

    /**
     * 解析时长，支持 d/h/m/s 后缀
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
