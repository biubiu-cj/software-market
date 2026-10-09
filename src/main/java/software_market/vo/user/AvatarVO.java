package software_market.vo.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AvatarVO {

    private String phone;

    @JsonProperty("avatarURL")
    private String avatarUrl;
}
