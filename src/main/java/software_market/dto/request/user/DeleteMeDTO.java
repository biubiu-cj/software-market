package software_market.dto.request.user;

import lombok.Data;

@Data
public class DeleteMeDTO {
    private String phone;
    private String email;
    private String vailCode;
}
