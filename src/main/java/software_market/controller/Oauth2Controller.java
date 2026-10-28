package software_market.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import software_market.config.security.JwtProvider;
import software_market.config.security.UserContext;
import software_market.dto.request.oauth2.AuthorizeDTO;
import software_market.dto.request.oauth2.CheckAppDTO;
import software_market.exception.UnauthorizedException;
import software_market.service.Oauth2Service;
import software_market.utils.Result;
import software_market.vo.oauth2.*;

@RequestMapping("/api/auth/connect")
@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    private final Oauth2Service oauth2Service;
    private final JwtProvider jwtProvider;

    @PostMapping("/check")
    public Result<Oauth2AppVO> check(@RequestBody CheckAppDTO dto) {
        Oauth2AppVO vo = oauth2Service.checkApp(dto);
        return Result.success(vo, "应用验证成功");
    }

    @PostMapping("/authorize")
    public Result<AuthorizeVO> authorize(@RequestBody AuthorizeDTO dto) {
        AuthorizeVO vo = oauth2Service.authorize(dto);
        return Result.success(vo, "授权成功");
    }

    @PostMapping("/token")
    public Result<TokenVO> token(
            @RequestParam String code,
            @RequestParam("app_id") String appId,
            @RequestParam("app_secret") String appSecret) {
        TokenVO vo = oauth2Service.exchangeToken(code, appId, appSecret);
        return Result.success(vo, "令牌获取成功");
    }

    /**
     * 第三方使用 OAuth2 access_token 调用（不是本站用户的 JWT），
     * 因此该端点不经过 JwtAuthenticationFilter 的认证，由这里自行校验令牌类型。
     */
    @PostMapping("/userinfo")
    public Result<Oauth2UserInfoVO> userinfo(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("缺少 OAuth2 access_token");
        }

        Integer userId;
        try {
            Claims claims = jwtProvider.parseOAuth2AccessToken(authorization.substring(7));
            userId = jwtProvider.getUserId(claims);
        } catch (Exception e) {
            throw new UnauthorizedException("OAuth2 access_token 无效");
        }

        Oauth2UserInfoVO vo = oauth2Service.userInfo(userId);
        return Result.success(vo);
    }

    @GetMapping("/records")
    public Result<RecordsPageVO> records(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer page_size) {
        Integer userId = UserContext.getUserId();
        RecordsPageVO vo = oauth2Service.records(userId, page, page_size);
        return Result.success(vo);
    }
}
