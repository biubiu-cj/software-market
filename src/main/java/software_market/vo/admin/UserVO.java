package software_market.vo.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Integer userId;
    private String phone;
    private String email;
    private String name;
    private String intro;
    private String avatarUrl;
    private String identity;
    private Integer score;
    private Integer punishNum;
    private LocalDateTime banend;
    private LocalDateTime createdAt;
}
