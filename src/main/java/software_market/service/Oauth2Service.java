package software_market.service;

import software_market.dto.request.oauth2.AuthorizeDTO;
import software_market.dto.request.oauth2.CheckAppDTO;
import software_market.vo.oauth2.*;

public interface Oauth2Service {
    Oauth2AppVO checkApp(CheckAppDTO dto);
    AuthorizeVO authorize(AuthorizeDTO dto);
    TokenVO exchangeToken(String code, String appId, String appSecret);
    Oauth2UserInfoVO userInfo(Integer userId);
    RecordsPageVO records(Integer userId, Integer page, Integer pageSize);
}
