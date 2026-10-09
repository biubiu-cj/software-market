package software_market.dto.request.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CheckAppDTO {
    @JsonProperty("app_id")
    private String appId;
}
