package software_market.vo.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Oauth2UserInfoVO {
    @JsonProperty("user_id")
    private Integer userId;

    private String name;
    private String email;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String intro;
    private String identity;
}
