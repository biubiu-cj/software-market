package software_market.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import software_market.config.security.JwtProvider;
import software_market.dto.response.TokenPair;
import software_market.entity.RefreshToken;
import software_market.entity.User;
import software_market.mapper.RefreshTokenMapper;
import software_market.mapper.UserMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class WechatServiceImpl {

    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;
    private final RefreshTokenMapper refreshTokenMapper;

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    @Value("${wechat.redirect-uri}")
    private String redirectUri;

    @Value("${wechat.frontend-url}")
    private String frontendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String WECHAT_AUTH_URL = "https://open.weixin.qq.com/connect/qrconnect";
    private static final String WECHAT_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private static final String WECHAT_USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo";

    public String buildAuthUrl() {
        String state = UUID.randomUUID().toString().replace("-", "");
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return WECHAT_AUTH_URL
                + "?appid=" + appid
                + "&redirect_uri=" + encodedRedirect
                + "&response_type=code"
                + "&scope=snsapi_login"
                + "&state=" + state
                + "#wechat_redirect";
    }

    public TokenPair wechatCallback(String code, String state) {
        // 1. 用 code 换取 access_token
        String tokenUrl = WECHAT_TOKEN_URL
                + "?appid=" + appid
                + "&secret=" + secret
                + "&code=" + code
                + "&grant_type=authorization_code";

        WechatTokenResponse tokenResp;
        try {
            tokenResp = restTemplate.getForObject(tokenUrl, WechatTokenResponse.class);
        } catch (Exception e) {
            log.error("微信 token 请求失败", e);
            throw new RuntimeException("微信授权失败，请重试");
        }

        if (tokenResp == null || tokenResp.getOpenid() == null) {
            log.error("微信 token 响应异常: {}", tokenResp);
            throw new RuntimeException("微信授权失败，请重试");
        }

        String openid = tokenResp.getOpenid();
        // TODO: 启用微信登录时，取消注释 User.java 中的 wechatOpenid/wechatUnionid 字段，恢复下面被注释的代码
        // String unionid = tokenResp.getUnionid();
        String accessToken = tokenResp.getAccessToken();

        // 2. 查找或创建用户
        // TODO: 数据库加 wechat_openid 列后，取消下面注释以支持已有微信用户直接登录
        // User user = userMapper.selectOne(new QueryWrapper<User>().eq("wechat_openid", openid));
        User user = null;
        if (user == null) {
            // 3. 获取微信用户信息
            String userinfoUrl = WECHAT_USERINFO_URL
                    + "?access_token=" + accessToken
                    + "&openid=" + openid;

            WechatUserInfo userInfo = null;
            try {
                userInfo = restTemplate.getForObject(userinfoUrl, WechatUserInfo.class);
            } catch (Exception e) {
                log.error("微信 userinfo 请求失败", e);
            }

            // 4. 创建新用户
            user = new User();
            // TODO: user.setWechatOpenid(openid);
            // TODO: user.setWechatUnionid(unionid);
            user.setIdentity("student");
            user.setScore(0);
            user.setPunishNum(0);
            user.setDeleted(false);
            user.setEmailNotificationBlocked(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            if (userInfo != null) {
                user.setName(userInfo.getNickname());
                user.setAvatarUrl(userInfo.getHeadimgurl());
            } else {
                user.setName("微信用户" + UUID.randomUUID().toString().substring(0, 6));
            }

            userMapper.insert(user);
        }
        // TODO: 数据库加 wechat_unionid 列后，取消下面注释以同步 unionid
        // else if (user.getWechatUnionid() == null && unionid != null) {
        //     user.setWechatUnionid(unionid);
        //     user.setUpdatedAt(LocalDateTime.now());
        //     userMapper.updateById(user);
        // }

        // 5. 签发 JWT
        return issueTokenPair(user);
    }

    private TokenPair issueTokenPair(User user) {
        Integer userId = user.getUserId();
        String accessToken = jwtProvider.generateAccessToken(userId.toString());
        String refreshToken = jwtProvider.generateRefreshToken(userId.toString());

        RefreshToken record = new RefreshToken();
        record.setUserId(user.getUserId());
        record.setToken(refreshToken);
        record.setExpiresAt(LocalDateTime.now().plusDays(30));
        record.setIsUsed(false);
        record.setCreatedAt(LocalDateTime.now());
        refreshTokenMapper.insert(record);

        return new TokenPair(accessToken, refreshToken);
    }

    // ====== 微信 API 响应 DTO ======

    @Data
    public static class WechatTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private Integer expiresIn;

        @JsonProperty("refresh_token")
        private String refreshToken;

        private String openid;
        private String scope;
        private String unionid;

        @JsonProperty("errcode")
        private Integer errcode;

        @JsonProperty("errmsg")
        private String errmsg;
    }

    @Data
    public static class WechatUserInfo {
        private String openid;
        private String nickname;
        private Integer sex;
        private String province;
        private String city;
        private String country;
        private String headimgurl;
        private String unionid;

        @JsonProperty("errcode")
        private Integer errcode;

        @JsonProperty("errmsg")
        private String errmsg;
    }
}
