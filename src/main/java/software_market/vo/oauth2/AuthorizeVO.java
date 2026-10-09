package software_market.vo.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorizeVO {
    @JsonProperty("redirect_url")
    private String redirectUrl;
}
