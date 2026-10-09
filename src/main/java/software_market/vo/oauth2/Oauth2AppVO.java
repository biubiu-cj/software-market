package software_market.vo.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Oauth2AppVO {
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("app_name")
    private String appName;

    @JsonProperty("app_icon")
    private String appIcon;

    private String description;
}
