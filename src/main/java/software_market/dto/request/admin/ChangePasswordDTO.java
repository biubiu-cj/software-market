package software_market.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordDTO {
    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "新密码不能为空")
    private String password1;

    @NotBlank(message = "确认密码不能为空")
    private String password2;
}
