package software_market.dto.request.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostAnalysisRequestDTO {

    private Integer postId;

    private String title;

    private String content;

    private String category;

    private String tag;
}
