package software_market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import software_market.config.security.UserContext;
import software_market.dto.request.oauth2.AuthorizeDTO;
import software_market.dto.request.oauth2.CheckAppDTO;
import software_market.service.Oauth2Service;
import software_market.utils.Result;
import software_market.vo.oauth2.*;

@RequestMapping("/api/auth/connect")
@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    private final Oauth2Service oauth2Service;

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

    @PostMapping("/userinfo")
    public Result<Oauth2UserInfoVO> userinfo() {
        Integer userId = UserContext.getUserId();
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
