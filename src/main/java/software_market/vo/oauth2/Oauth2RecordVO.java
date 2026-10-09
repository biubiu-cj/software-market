package software_market.vo.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Oauth2RecordVO {
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("app_name")
    private String appName;

    @JsonProperty("app_icon")
    private String appIcon;

    private String scope;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
