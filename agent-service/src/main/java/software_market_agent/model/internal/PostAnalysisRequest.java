package software_market_agent.model.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostAnalysisRequest {

    @NotNull
    private Integer postId;

    @NotBlank
    private String title;

    private String content;

    private String category;

    private String tag;
}
