package software_market.vo.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadAvatarVO {

    /**
     * 头像访问地址
     */
    @JsonProperty("avatarURL")
    private String avatarUrl;

}