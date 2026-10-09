package software_market.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenPair {

    //访问令牌
    private String token;

    //刷新令牌
    private String refreshToken;
}
