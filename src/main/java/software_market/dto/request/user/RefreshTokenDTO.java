package software_market.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenDTO {

    @NotBlank(message = "refresh_token 不能为空")
    private String refreshToken;
}
