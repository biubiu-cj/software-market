package software_market.vo.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CurrentUserVO {

    @JsonProperty("userID")
    private Integer userId;

    private String name;

    private String phone;

    private String email;

    @JsonProperty("avatarURL")
    private String avatarUrl;

    private String intro;

    private Integer score;

    private String identity;

    private LocalDateTime banend;

    private Integer punishNum;

    private Boolean emailNotificationBlocked;
}
