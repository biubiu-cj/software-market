package software_market.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software_market.dto.response.TokenPair;
import software_market.service.impl.WechatServiceImpl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequestMapping("/api/auth/wechat")
@RestController
@RequiredArgsConstructor
@Slf4j
public class WechatController {

    private final WechatServiceImpl wechatService;

    @Value("${wechat.frontend-url}")
    private String frontendUrl;

    /**
     * 微信登录入口：重定向到微信二维码授权页
     */
    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        String authUrl = wechatService.buildAuthUrl();
        response.sendRedirect(authUrl);
    }

    /**
     * 微信回调：code 换 token → 获取用户信息 → 登录/注册 → 签发 JWT → 跳转回前端
     */
    @GetMapping("/callback")
    public void callback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {

        String frontendCallbackUrl;
        try {
            TokenPair tokenPair = wechatService.wechatCallback(code, state);
            String token = URLEncoder.encode(tokenPair.getToken(), StandardCharsets.UTF_8);
            String refreshToken = URLEncoder.encode(tokenPair.getRefreshToken(), StandardCharsets.UTF_8);
            frontendCallbackUrl = frontendUrl
                    + "/auth/wechat-callback?token=" + token
                    + "&refreshToken=" + refreshToken;
        } catch (Exception e) {
            log.error("微信登录回调处理失败", e);
            frontendCallbackUrl = frontendUrl
                    + "/auth/login?error=" + URLEncoder.encode("微信登录失败，请重试", StandardCharsets.UTF_8);
        }

        response.sendRedirect(frontendCallbackUrl);
    }
}
