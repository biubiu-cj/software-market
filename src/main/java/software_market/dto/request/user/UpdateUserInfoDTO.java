package software_market.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserInfoDTO {

    // 必填（更新必须知道是谁）
    @NotNull(message = "userID不能为空")
    private Integer userID;

    // 可选字段（更新接口一般不强制）
    private String phone;
    private String email;
    private String password;

    private String name;

    private Integer num;

    private String intro;

    private String avatarURL;
}