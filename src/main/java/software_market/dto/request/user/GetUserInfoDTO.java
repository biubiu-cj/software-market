package software_market.dto.request.user;

import lombok.Data;

@Data
public class GetUserInfoDTO {

    private String phone;

    private Integer userId;
}
