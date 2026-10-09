package software_market.dto.request.admin;

import lombok.Data;

@Data
public class UpdateAdminInfoDTO {
    private String account;
    private String name;
    private String password1;
    private String password2;
}
