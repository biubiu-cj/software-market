package software_market.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileVO {

    private Integer userId;

    private String name;

    private String phone;

    private String avatarUrl;

    private String intro;

    private Integer score;

    private String identity;

    private LocalDateTime banend;

    private Integer punishNum;

    private String email;
}
