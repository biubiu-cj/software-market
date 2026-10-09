package software_market.dto.request.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthorizeDTO {
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("response_type")
    private String responseType;

    private String scope;
    private String state;
}
