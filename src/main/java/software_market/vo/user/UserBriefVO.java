package software_market.vo.user;

import lombok.Data;

@Data
public class UserBriefVO {

    private Integer userId;

    private String name;

    private String avatarUrl;

    private String identity;

    private Integer score;
}
