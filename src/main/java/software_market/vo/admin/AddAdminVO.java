package software_market.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddAdminVO {
    private Integer adminId;
    private String account;
}
